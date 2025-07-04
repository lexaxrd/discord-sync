package xyz.themis.discordSyncSource.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.themis.discordSyncSource.Main

class SendMessageCommand(val plugin: Main) : CommandExecutor {
    override fun onCommand(s: CommandSender, cmd: Command, label: String, args: Array<out String>?): Boolean {
        if (cmd.name.equals("esle-mesaj-gonder", ignoreCase = true)) {
            if (s !is Player) {
                s.sendMessage("${ChatColor.RED} ${plugin.configManager.getLanguageMessage("general.only-players")}")
                return true;
            }

            if (!s.hasPermission("roselia.sendsyncmessage")) {
                s.sendMessage("${ChatColor.RED} ${plugin.configManager.getLanguageMessage("general.no-permission")}")
                return true;
            }

            plugin.botManager.sendAccountSyncEmbed()
            s.sendMessage("${ChatColor.GREEN} ${plugin.configManager.getLanguageMessage("bot.embed-sent")}")
            return true;
        }
        return false;
    }
}