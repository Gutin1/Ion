package net.horizonsend.ion

@CommandAlias("safelogout")
internal class CombatLogging(private val plugin: Plugin): BaseCommand(), Listener {
	val safeLoggingPlayer = mutableSetOf<Player>()

	@Default
	@Description("Log out safely and avoid creating a CombatNPC.")
	fun safeLogout(executor: Player) {
		executor.sendFeedbackActionMessage(FeedbackType.INFORMATION, "You will be automatically logged out in 5 minutes. Do not do anything, otherwise your timer will be cleared.")

		safeLoggingPlayer.add(executor)

		Bukkit.getScheuler().runTaskLater(plugin, Runnable {
			if (safeLoggingPlayer.contains(executor)) {
				// Safe Logout
			}
		})
	}

	private inline fun voidSafeLogout(player: Player) {
		player.sendFeedbackActionMessage(FeedbackType.USER_ERROR, "")
	}

	@EventHandler
	fun onPlayerMoveEvent(event: PlayerMoveEvent) {

	}

	@EventHandler
	fun onDisconnectEvent(event: DisconnectEvent) {
		val playerChunk = event.player.location.chunk

		playerChunk.addPluginChunkTicket(plugin)

		TODO("Create Player NPC")

		Bukkit.getScheduler().runTaskLater(plugin, Runnable {
			playerChunk.removePluginChunkTicket(plugin)
		}, 20 * 60 * 5) // 5 Minutes as Ticks
	}
}