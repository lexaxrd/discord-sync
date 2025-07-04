package xyz.themis.discordSyncSource.bot

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.GatewayIntent
import xyz.themis.discordSyncSource.Main
import java.awt.Color
import java.util.logging.Logger

class BotManager(val plugin: Main) {
    val logger: Logger
    private var jda = null as net.dv8tion.jda.api.JDA?
    init {
        logger = plugin.logger
    }

    fun startBot() {
        logger.info(plugin.configManager.getLanguageMessage("bot.starting-message"))

        val token = plugin.configManager.getConfig().getString("discord.bot.token")

        require(!token.isNullOrBlank()) {
            logger.severe(plugin.configManager.getLanguageMessage("bot.token-required"))
            return
        }

        try {
            jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(BotListener(plugin), ButtonListener(plugin), ModalListener(plugin))
                .build();
            jda?.awaitReady()

            //registerSlashCommands()
        } catch (e: Exception) {
            logger.severe(plugin.configManager.getLanguageMessage("bot.failed-start-message").replace("%error_message%", e.message.toString()))
            e.printStackTrace()
        }
    }

    fun registerSlashCommands() {
        logger.info(plugin.configManager.getLanguageMessage("bot.slash-commands-registering"))
        jda?.upsertCommand(
            Commands.slash(plugin.configManager.getConfig().getString("discord.bot.slash1.name")!!, plugin.configManager.getConfig().getString("discord.bot.slash1.desc")!!)
                .addOption(OptionType.STRING, plugin.configManager.getConfig().getString("discord.bot.slash1.options.option1.name")!!, plugin.configManager.getConfig().getString("discord.bot.slash1.options.option1.desc")!!)
        )?.queue()
        logger.info(plugin.configManager.getLanguageMessage("bot.slash-commands-registered"))
    }

    fun sendAccountSyncEmbed() {
        val channelId: String? = plugin.configManager.getConfig().getString("discord.bot.channel_id") ?: null
        if (channelId == null) {
            logger.info(plugin.configManager.getLanguageMessage("bot.channel-id-required"))
            return;
        }

        val embed = EmbedBuilder()
        embed.setTitle(plugin.configManager.getConfig().getString("discord.bot.embed.name"))
        embed.setDescription(plugin.configManager.getConfig().getString("discord.bot.embed.desc"))
        embed.setColor(Color(
            plugin.configManager.getConfig().getInt("discord.bot.embed.color.r"),
            plugin.configManager.getConfig().getInt("discord.bot.embed.color.g"),
            plugin.configManager.getConfig().getInt("discord.bot.embed.color.b")
        ))
        val footer: String? = plugin.configManager.getConfig().getString("discord.bot.embed.footer")
        if (footer != null) embed.setFooter(footer)

        val syncButton: Button = Button.primary("sync-account", plugin.configManager.getConfig().getString("discord.bot.embed.button-name")!!)
        jda?.getTextChannelById(channelId)
            ?.sendMessageEmbeds(embed.build())
            ?.setActionRow(syncButton)
            ?.queue()
    }

    fun getJDA() = jda
}