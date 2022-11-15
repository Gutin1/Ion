package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.messageEmbed

@CommandAlias("vote")
@Description("Lists voting websites.")
class DiscordVoteCommand(private val configuration: ProxyConfiguration) {

//	fun onVoteCommand(event: SlashCommandInteractionEvent) {
//		event.replyEmbeds(
//			messageEmbed(
//				fields = configuration.voteSites.keys
//				proxy.serversCopy.values
//					.filter { it.players.isNotEmpty() }
//					.map { server ->
//						val serverName = server.name.replaceFirstChar { it.uppercase() }
//
//						MessageEmbed.Field(
//							"$serverName *(${server.players.size} online)*",
//							server.players.joinToString("\n", "", "") {
//								it.name.replace("_", "\\_")
//							},
//							true
//						)
//					}
//					.ifEmpty { null },
//				description = if (proxy.onlineCount == 0) "*No players online*" else null
//			)
//		).setEphemeral(true).queue()
//
//	}
}