package xyz.themis.discordSyncSource.bot

import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import xyz.themis.discordSyncSource.Main
import java.util.logging.Logger

class BotListener(val plugin: Main) : ListenerAdapter() {
    val logger: Logger

    init {
        logger = plugin.logger
    }
    override fun onReady(event: ReadyEvent) {
        logger.info(plugin.configManager.getLanguageMessage("bot.login-message").replace("%bot_name%", event.jda.selfUser.name))
    }
}