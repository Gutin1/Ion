package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.proxy.annotations.VelocityListener
import net.kyori.adventure.text.minimessage.MiniMessage

@VelocityListener
@Suppress("Unused")
class PlayerResourcePackStatusListener {
	@Subscribe(order = PostOrder.LAST)
	fun onPlayerResourcePackStatusListener(event: PlayerResourcePackStatusEvent): EventTask = EventTask.async {
		if (event.status != PlayerResourcePackStatusEvent.Status.ACCEPTED) return@async

		event.player.sendMessage(
			MiniMessage.miniMessage().deserialize(
				"<${FeedbackType.USER_ERROR.colour}>Please consider downloading the resource pack for better login times! <click:open_url:'https://github.com/HorizonsEndMC/ResourcePack'>https://github.com/HorizonsEndMC/ResourcePack</click>"
			)
		)
	}
}