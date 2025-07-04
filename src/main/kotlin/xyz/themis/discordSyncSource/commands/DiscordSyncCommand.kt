package xyz.themis.discordSyncSource.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.themis.discordSyncSource.Main

class DiscordSyncCommand(val plugin: Main) : CommandExecutor {
    override fun onCommand(s: CommandSender, cmd: Command, lbl: String, args: Array<out String>?): Boolean {
        if (cmd.name.equals("discord-esle", ignoreCase = true)) {
            if (s !is Player) {
                s.sendMessage("${ChatColor.RED} ${plugin.configManager.getLanguageMessage("general.only-players")}")
                return true
            }

            if (plugin.syncManager.isAlreadySynced(s.player!!.uniqueId)) {
                s.sendMessage("${ChatColor.RED} ${plugin.configManager.getLanguageMessage("discord.already-synced")}")
                return true;
            }

            val code = plugin.syncManager.generateCode(s.player!!.uniqueId)
            s.sendMessage("${ChatColor.GREEN} ${plugin.configManager.getLanguageMessage("discord.sync-code").replace("%code%", code, false)}")

            return true
        }
        return false
    }
}