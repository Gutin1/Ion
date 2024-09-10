package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.common.database.schema.misc.SLPlayerStatistic
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.server.features.ai.faction.AIFaction
import net.horizonsend.ion.server.features.ai.module.misc.FactionManagerModule
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.modules.RewardsProvider
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId

class AIAchievementRewardsProvider(val controller: AIController) : RewardsProvider {
	override fun triggerReward() {
		val players = controller.starship.damagers.mapNotNull filter@{ (damager, data) ->
			if (damager !is PlayerDamager) return@filter null
			if (data.lastDamaged < ShipKillXP.damagerExpiration) return@filter null

			// require they be online to get xp
			// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
			if (damager.player.hasPermission("starships.noxp")) return@filter null

			damager.player
		}

		Tasks.async {
			players.forEach { player ->
				SLPlayerStatistic.incStatistic(player.slPlayerId, SLPlayerStatistic::aiShipsKilled, 1)
			}

			controller.modules["faction"]?.let {
				it as FactionManagerModule

				val id = it.faction.identifier
				val achievement = runCatching { Achievement.valueOf("SINK_$id") }.getOrNull()

				players.forEach { player ->
					achievement?.rewardAdvancement(player)

					val new = SLPlayerStatistic.incrementFactionKIll(player.slPlayerId, id)

					if (new.keys.size == AIFaction.factions.size) {
						Achievement.SINK_EACH_AI_SHIP.rewardAdvancement(player)
					}
				}
			}
		}
	}
}
