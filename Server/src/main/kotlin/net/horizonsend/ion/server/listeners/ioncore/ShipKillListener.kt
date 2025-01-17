package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.ShipKillEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("unused")
class ShipKillListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onShipKill(event: ShipKillEvent) {
		event.player.rewardAchievement(Achievement.KILL_SHIP)
	}
}