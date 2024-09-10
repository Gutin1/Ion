package net.horizonsend.ion.server.features.achievements

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.vaultEconomy
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.inventory.ItemStack

object AchievementListeners : IonServerComponent() {

	override fun onEnable() {
		checkBalances()
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerAdvancementDoneEvent(event: PlayerAdvancementDoneEvent){
		/* listener for the final reward of all multi-criterion advancements */

		val key = event.advancement.key.key //String
		when(key){
			Achievement.OBTAIN_ALL_CORES.key -> {
				Achievement.OBTAIN_ALL_CORES.rewardAdvancement(event.player)
			}
			Achievement.OBTAIN_ALL_POWER_ARMOR.key -> {
				Achievement.OBTAIN_ALL_POWER_ARMOR.rewardAdvancement(event.player)
			}
			Achievement.VISIT_ALL_PLANETS.key -> {
				Achievement.VISIT_ALL_PLANETS.rewardAdvancement(event.player)
			}
			Achievement.VISIT_ALL_SYSTEMS.key -> {
				Achievement.VISIT_ALL_SYSTEMS.rewardAdvancement(event.player)
			}
			/*
			Achievement.SINK_EACH_AI_SHIP.key -> {
				Achievement.SINK_EACH_AI_SHIP.rewardAdvancement(event.player)
			}
			*/

		}
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerDeathEvent(event: PlayerDeathEvent) {
		val killer = event.entity.killer ?: return // Only player kills
		if (killer !== event.player) Achievement.KILL_PLAYER.rewardAdvancement(killer)
	}

/* UNUSED EVENT */
//	@EventHandler(priority = EventPriority.MONITOR)
//	@Suppress("Unused")
//	fun onDetectShip(event: StarshipDetectEvent) {
//		Achievement.DETECT_SHIP.rewardAdvancement(event.player)
//	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerChangedWorldEvent(event: PlayerChangedWorldEvent){
		val player = event.player
		if(player.world.ion.hasFlag(WorldFlag.SPACE_WORLD)){
			Achievement.ENTER_SPACE.rewardAdvancement(player)
		}
		val advancement = try{
			Achievement.valueOf("VISIT_${(player.world.name).uppercase()}")
		}catch(_: IllegalArgumentException){ return }
		advancement.rewardAdvancement(player)
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerAttemptPickupItemEvent(event: PlayerInventorySlotChangeEvent) {
		val player = event.player
		val item = event.newItemStack
		detectObtainedItem(player, item)
	}

/*  currently using PlayerChangedWorldEvent, EnterPlanetEvent may be used later */

//	@EventHandler(priority = EventPriority.LOWEST)
//	fun onEnterPlanetEvent(event: EnterPlanetEvent) {
//		val player = (event.controller as? PlayerController)?.player ?: return
//		val playerData = SLPlayer[player.uniqueId]!!
//
//		when (event.newWorld.name.lowercase()) {
//
//			else -> return
//		}
//
//	}

	private fun checkBalances() {
		Tasks.asyncRepeat(20L, 20L){
			for (player in Bukkit.getOnlinePlayers()) {
				val num = vaultEconomy?.getBalance(player) ?: continue
				when {
					num >= 10000000.0 -> Achievement.BAL_10M.rewardAdvancement(player)
					num >= 1000000.0 -> Achievement.BAL_1M.rewardAdvancement(player)
					num >= 500000.0 -> Achievement.BAL_500K.rewardAdvancement(player)
					num >= 100000.0 -> Achievement.BAL_100K.rewardAdvancement(player)
					num >= 50000.0 -> Achievement.BAL_50K.rewardAdvancement(player)
					num >= 10000.0 -> Achievement.BAL_10K.rewardAdvancement(player)
				}
			}
		}
	}

	private fun detectObtainedItem(player: Player, item: ItemStack){
		if (item.type == Material.AIR) return // dropped item

		/* include legacy custom items (like power armor) */
		val legacyCustomItem = CustomItems[item]
		val newCustomItem = item.customItem

		val customItemName: String? = legacyCustomItem?.id?.uppercase() ?: newCustomItem?.identifier?.uppercase()

		//if(customItemName != null) println("picked up custom item: $customItemName")

		//vanilla items
		val itemName = if(customItemName == null) (item.type.name.uppercase()) else null
		//if(itemName != null) println("picked up item: $itemName")

		/* try custom item name, if null then vanilla item name, if null then return (somehow? IDK) */
		val name = customItemName ?: itemName ?: return

		val advancement = try{
			Achievement.valueOf("OBTAIN_${name}")
		}catch(_: IllegalArgumentException){ return } // picked up item doesn't give an achievement
		advancement.rewardAdvancement(player)
	}
}
