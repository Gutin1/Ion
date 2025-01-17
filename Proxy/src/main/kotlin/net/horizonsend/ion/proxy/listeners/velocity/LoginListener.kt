package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import net.dv8tion.jda.api.JDA
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.annotations.VelocityListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.jetbrains.exposed.sql.transactions.transaction

@VelocityListener
@Suppress("Unused")
class LoginListener(private val configuration: ProxyConfiguration, private val jda: JDA?) {
	@Subscribe(order = PostOrder.LAST)
	fun onLoginEvent(event: LoginEvent): EventTask = EventTask.async {
		var headerComponent =
			Component.text().append(Component.text("\nHorizon's End\n", TextColor.color(0xff7f3f), TextDecoration.BOLD))

		if (configuration.tablistHeaderMessage.isNotEmpty()) {
			headerComponent = headerComponent
				.append(Component.text("\n"))
				.append(MiniMessage.miniMessage().deserialize(configuration.tablistHeaderMessage))
				.append(Component.text("\n"))
		}

		event.player.sendPlayerListHeader(headerComponent)

		jda?.let{
			val memberId = transaction {
				PlayerData.getOrCreate(event.player.uniqueId, event.player.username).discordUUID
			} ?: return@async
			val guild = jda.getGuildById(configuration.discordServer) ?: return@async

			guild.addRoleToMember(
				guild.getMemberById(memberId) ?: return@async,
				guild.getRoleById(configuration.onlineRole) ?: return@async
			).queue()
		}
	}
}