package xyz.themis.discordSyncSource.bot

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import xyz.themis.discordSyncSource.Main
import java.util.Locale
import java.util.Locale.getDefault

class ModalListener(val plugin: Main) : ListenerAdapter() {
    override fun onModalInteraction(event: ModalInteractionEvent) {
        try {
            event.deferReply(true).queue()
            val id = event.modalId

            if (id == "sync-modal") {
                val code: String = event.getValue("sync-code")?.asString!!.uppercase()
                val discordId: String = event.user.id
                val success: Boolean = plugin.syncManager.verifyCode(code, discordId)
                if (!success) {
                    event.hook.sendMessage(plugin.configManager.getConfig().getString("discord.messages.sync_error")!!).setEphemeral(true).queue()
                    return;
                }

                event.hook.sendMessage(plugin.configManager.getConfig().getString("discord.messages.sync_successful")!!).setEphemeral(true).queue()
            }
        }
        catch (e: Exception) {
            event.reply(plugin.configManager.getConfig().getString("discord.messages.error")!!.replace("%error_message%", e.message.toString(), false)).setEphemeral(true).queue()
        }

    }
}