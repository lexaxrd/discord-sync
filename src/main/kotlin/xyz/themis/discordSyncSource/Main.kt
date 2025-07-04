package xyz.themis.discordSyncSource

import org.bukkit.plugin.java.JavaPlugin
import xyz.themis.discordSyncSource.bot.BotManager
import xyz.themis.discordSyncSource.commands.DiscordSyncCommand
import xyz.themis.discordSyncSource.commands.SendMessageCommand
import xyz.themis.discordSyncSource.config.ConfigManager
import xyz.themis.discordSyncSource.events.PlayerJoinListener
import xyz.themis.discordSyncSource.events.PlayerQuitListener
import xyz.themis.discordSyncSource.sync.SyncManager

class Main : JavaPlugin() {

    lateinit var configManager: ConfigManager
    lateinit var botManager: BotManager
    lateinit var syncManager: SyncManager
    override fun onEnable() {
        logger.info("DiscordSync aktif!")

        configManager = ConfigManager(this)
        botManager = BotManager(this)
        syncManager = SyncManager(this)
        server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            syncManager.cleanExpired()
        }, 0L, 1200L) // 1200 tick = 60 second

        getCommand("esle-mesaj-gonder")?.setExecutor(SendMessageCommand(this))
        getCommand("discord-esle")?.setExecutor(DiscordSyncCommand(this))

        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
        server.pluginManager.registerEvents(PlayerQuitListener(this), this)
        botManager.startBot()
    }

    override fun onDisable() {
        logger.info("DiscordSync devre dışı!")
    }
}
