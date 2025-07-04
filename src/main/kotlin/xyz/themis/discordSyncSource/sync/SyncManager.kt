package xyz.themis.discordSyncSource.sync

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import xyz.themis.discordSyncSource.Main
import java.util.*

class SyncManager(val plugin: Main) {
    lateinit private var lp: LuckPerms
    private val codes: MutableMap<String, CodeEntry> = mutableMapOf()
    companion object {
        private const val TIMEOUT = 5 * 60 * 1000L // 5 dakika
    }
    init {
        if (plugin.server.pluginManager.isPluginEnabled("LuckPerms")) {
            lp = LuckPermsProvider.get()
        }
    }
    private class CodeEntry(val uuid: UUID, val time: Long = System.currentTimeMillis()) {
        fun expired(): Boolean {
            return System.currentTimeMillis() - time > TIMEOUT
        }
    }

    fun generateCode(uuid: UUID): String {
        val code = generateRandomCode()
        codes[code] = CodeEntry(uuid)
        return code
    }
    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        return (1..6)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }

    fun saveAccount(uuid: UUID, discordId: String) {
        val playersConfig = plugin.configManager.getPlayersConfig()
        playersConfig.set("players.${uuid}", discordId)
        plugin.configManager.savePlayersConfig()
    }

    fun verifyCode(code: String, discordId: String): Boolean {
        val entry = codes[code] ?: return false

        if (entry.expired()) {
            codes.remove(code)
            return false
        }

        saveAccount(entry.uuid, discordId)

        syncAccount(entry.uuid, dcid = discordId)
        if (plugin.configManager.getConfig().getBoolean("discord.guild.update_nickname")) {
            updateNickname(entry.uuid, discordId)
        }
        codes.remove(code)
        return true
    }
    fun syncAccount(uuid: UUID, dcid: String) {
        val player: Player? = plugin.server.getPlayer(uuid)
        if (player != null) {
            player.sendMessage(plugin.configManager.getConfig().getString("discord.messages.sync_successful")!!)
        }
        syncRoles(uuid, dcid)
    }
    fun syncRoles(uuid: UUID, dcid: String) {
        if (lp == null) return


        val user: net.luckperms.api.model.user.User? = lp.userManager.getUser(uuid)
        if (user == null) return

        val group: String = user.primaryGroup

        val sections = plugin.configManager.getConfig().getConfigurationSection("permissions")

        val guildId: String? = plugin.configManager.getConfig().getString("discord.guild.guild_id")
        if (sections == null || guildId == null) return

        for (role in sections.getKeys(false)) {
            val lpRank = sections.getString(role + ".lp_rank")
            val roleId = sections.getString(role + ".role_id")

            if (lpRank != null && roleId != null && group.equals(lpRank, ignoreCase = true)) {
                try {
                    plugin.botManager.getJDA()?.getGuildById(guildId)
                        ?.addRoleToMember(UserSnowflake.fromId(dcid), plugin.botManager.getJDA()?.getRoleById(roleId)!!)
                        ?.queue()

                    plugin.logger.info(
                        plugin.configManager.getConfig().getString("discord.messages.role_given")
                            ?.replace("%msg%", "$roleId -> ${dcid}", false)
                    )
                } catch (e: Exception) {
                    plugin.logger.info(
                        plugin.configManager.getConfig().getString("discord.messages.role_failed")
                            ?.replace("%msg%", "$roleId - ${e.message}", false)
                    )
                }
            }
        }
    }


    fun updateNickname(uuid: UUID, dcid: String) {
        val format = plugin.configManager.getConfig().getString("discord.guild.nickname_format")
        if (format == null) return

        val mcName = plugin.server.getPlayer(uuid)?.name
        val dcName = plugin.botManager.getJDA()?.getUserById(dcid)?.name
        val newName = format.replace("%discord_name%", dcName ?: "Bilinmeyen")
            .replace("%minecraft_name%", mcName ?: "Bilinmeyen")

        val guildId = plugin.configManager.getConfig().getString("discord.guild.guild_id")
        if (guildId == null) return

        try {
            plugin.botManager.getJDA()?.getGuildById(guildId)
                ?.getMemberById(dcid)
                ?.modifyNickname(newName)
                ?.queue()
        }
        catch (e: Exception) {
            plugin.logger.info("Error: ${e.message}")
        }

    }
    fun checkUserRole(uuid: UUID, dcid: String) {
        val jda = plugin.botManager.getJDA() ?: return
        val lpUser = lp.userManager.getUser(uuid) ?: return
        val primaryGroup = lpUser.primaryGroup
        val config = plugin.configManager.getConfig()
        val guildId = config.getString("discord.guild.guild_id") ?: return
        val guild = jda.getGuildById(guildId) ?: return

        guild.retrieveMemberById(dcid).queue({ member ->

            val section = config.getConfigurationSection("permissions")
            if (section == null) return@queue


            for (key in section.getKeys(false)) {
                val lpRank = section.getString("$key.lp_rank")
                val roleId = section.getString("$key.role_id")
                val role = roleId?.let { guild.getRoleById(it) }

                if (lpRank == null || roleId == null || role == null) continue

                if (primaryGroup.equals(lpRank, true)) {
                    if (!member.roles.any { it.id == roleId }) {
                        guild.addRoleToMember(member, role).queue({
                            plugin.logger.info(
                                plugin.configManager.getConfig().getString("discord.messages.role_given")?.replace("%msg%", "$roleId -> $dcid", false)
                            )
                        }, {
                            plugin.logger.warning(plugin.configManager.getConfig().getString("discord.messages.role_failed")?.replace("%msg%", "$roleId -> $dcid | ${it.message}", false))
                        })
                    }
                }
            }

            for (key in section.getKeys(false)) {
                val lpRank = section.getString("$key.lp_rank")
                val roleId = section.getString("$key.role_id")
                val role = roleId?.let { guild.getRoleById(it) }

                if (lpRank == null || roleId == null || role == null) continue

                if (!primaryGroup.equals(lpRank, true)) {
                    if (member.roles.any { it.id == roleId }) {
                        guild.removeRoleFromMember(member, role).queue({
                            plugin.logger.info(plugin.configManager.getConfig().getString("discord.messages.role_removed")?.replace("%msg%", "$roleId -> $dcid", false))
                        }, {
                            plugin.logger.warning(plugin.configManager.getConfig().getString("discord.messages.role_removal_failed")?.replace("%msg%", "$roleId -> $dcid | ${it.message}", false))
                        })
                    }
                }
            }

        })
    }


    fun getUUIDByCode(code: String): UUID? {
        return codes[code]?.uuid
    }


    fun isAlreadySynced(uuid: UUID): Boolean {
        val playersConfig = plugin.configManager.getPlayersConfig()
        return playersConfig.contains("players.$uuid")
    }

    fun getDiscordIdByUUID(uuid: UUID): String? {
        val playersConfig = plugin.configManager.getPlayersConfig()
        return playersConfig.getString("players.$uuid")
    }

    fun cleanExpired() {
        codes.entries.removeIf { it.value.expired() }
    }
}