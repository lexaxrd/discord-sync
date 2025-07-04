package xyz.themis.discordSyncSource.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import xyz.themis.discordSyncSource.Main

class PlayerQuitListener(val plugin: Main) : Listener {
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val playerId = player.uniqueId
        val discordId: String? = plugin.syncManager.getDiscordIdByUUID(playerId)
        if (discordId == null) return
        plugin.syncManager.checkUserRole(playerId, discordId)
    }
}