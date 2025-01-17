package net.horizonsend.ion.server.utilities

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.common.database.PlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.starlegacy.SETTINGS
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.progression.SLXP
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

fun Player.rewardAchievement(achievement: Achievement) {
	ionCore { if (!SETTINGS.master) return }

	val playerData = transaction { PlayerData.findById(this@rewardAchievement.uniqueId) } ?: return
	if (playerData.achievements.contains(achievement)) return

	transaction { playerData.addAchievement(achievement) }

	vaultEconomy { it.depositPlayer(this, achievement.creditReward.toDouble()) }
	ionCore {
		SLXP.addAsync(this, achievement.experienceReward, false)
		if (achievement.chetheriteReward > 0) {
			inventory.addItem(CustomItems.MINERAL_CHETHERITE.itemStack(achievement.chetheriteReward))
		}
	}

	showTitle(
		Title.title(
			Component.text(achievement.title).color(NamedTextColor.GOLD),
			Component.text("Achievement Granted: ${achievement.description}").color(NamedTextColor.GRAY)
		)
	)

	sendRichMessage(
		"""
			<gold>${achievement.title}
			<gray>Achievement Granted: ${achievement.description}<reset>
			Credits: ${achievement.creditReward}
			Experience: ${achievement.experienceReward}
		""".trimIndent() + if (achievement.chetheriteReward != 0) "\nChetherite: ${achievement.chetheriteReward}" else ""
	)
}