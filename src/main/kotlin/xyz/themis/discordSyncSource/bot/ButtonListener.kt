package xyz.themis.discordSyncSource.bot

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import xyz.themis.discordSyncSource.Main

class ButtonListener(val plugin: Main) : ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        try {
            val id = event.componentId

            // If that's the button that was clicked
            if (id == "sync-account") {
                val textInput: TextInput = TextInput.create(
                    "sync-code",
                    plugin.configManager.getConfig().getString("discord.bot.embed.text-input-name")!!,
                    TextInputStyle.SHORT
                )
                    .setPlaceholder(
                        plugin.configManager.getConfig().getString("discord.bot.embed.text-input-placeholder")
                    )
                    .setMinLength(6).setMaxLength(6).build()

                val modal: Modal = Modal.create(
                    "sync-modal",
                    plugin.configManager.getConfig().getString("discord.bot.embed.modal-name")!!
                )
                    .addActionRow(textInput)
                    .build()

                event.replyModal(modal).queue();
            }
        }
        catch(e: Exception) {
            event.reply(plugin.configManager.getConfig().getString("discord.messages.error")!!.replace("%error_message%", e.message.toString(), false)).setEphemeral(true).queue()
        }
    }
}