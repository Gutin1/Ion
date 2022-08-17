package net.horizonsend.ion.proxy.commands.velocity

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage
import net.horizonsend.ion.proxy.jda
import net.horizonsend.ion.proxy.managers.LinkManager
import net.horizonsend.ion.proxy.proxyConfiguration
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("Unused")
@CommandAlias("account")
@Description("Manage the link between your Minecraft and Discord account.")
class VelocityAccountCommand : BaseCommand() {
	@Subcommand("status")
	@Description("Check linked Discord account.")
	fun onStatusCommand(sender: Player) {
		val playerData = transaction { PlayerData.findById(sender.uniqueId) }

		if (playerData?.discordUUID == null) {
			sender.sendFeedbackMessage(FeedbackType.INFORMATION, "Your Minecraft account is not linked.")
			return
		}

		jda.retrieveUserById(playerData.discordUUID!!).queue {
			sender.sendFeedbackMessage(FeedbackType.INFORMATION, "Linked to {0} ({1}).", it.asTag, playerData.discordUUID!!)
		}
	}

	@Subcommand("unlink")
	@Description("Unlink Discord account.")
	fun onUnlinkCommand(sender: Player) = transaction {
		val playerData = PlayerData.findById(sender.uniqueId)

		if (playerData?.discordUUID == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Your account is not linked.")
			return@transaction
		}

		jda.getGuildById(proxyConfiguration.discordServer)!!.apply {
			getMemberById(playerData.discordUUID!!)?.let { member ->
				removeRoleFromMember(member, getRoleById(proxyConfiguration.linkedRole)!!).queue()
			}
		}

		playerData.discordUUID = null

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Your account is no longer linked.")
	}

	@Subcommand("link")
	@Description("Link Discord account.")
	fun onLinkCommand(sender: Player) {
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"Run /account link ${LinkManager.createLinkCode(sender.uniqueId)} in Discord to link your accounts. The code will expire in 5 minutes.",
		)
	}
}