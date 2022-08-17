package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.Default
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.PlayerDataTable
import net.horizonsend.ion.proxy.annotations.CommandMeta
import net.horizonsend.ion.proxy.proxyConfiguration
import net.horizonsend.ion.proxy.messageEmbed
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.transactions.transaction

@CommandMeta("resync", "Resync all roles")
class ResyncCommand {
	@Default
	@Suppress("Unused")
	fun onResyncCommand(event: SlashCommandInteractionEvent) {
		if (event.user.idLong != 521031433972744193) {
			event.replyEmbeds(messageEmbed("You do not have permission to use this command.", color = 0xff8844))
				.setEphemeral(true)
				.queue()
			return
		}

		event.deferReply(true).queue()

		val players = transaction { PlayerData.find(PlayerDataTable.discordUUID.isNotNull()).toList() }

		val guild = event.jda.getGuildById(proxyConfiguration.discordServer)

		if (guild == null) {
			event.hook.editOriginalEmbeds(messageEmbed("Guild is not set.", color = 0xff8844))
				.queue()
			return
		}

		val linkedRole = guild.getRoleById(proxyConfiguration.linkedRole)

		if (linkedRole == null) {
			event.hook.editOriginalEmbeds(messageEmbed("Guild is not set.", color = 0xff8844))
				.queue()
			return
		}

		for (player in players) {
			val user = guild.getMemberById(player.discordUUID!!) ?: continue

			guild.addRoleToMember(user, linkedRole)
		}

		event.hook.editOriginalEmbeds(messageEmbed("Done", color = 0x7fff7f))
			.queue()
	}
}