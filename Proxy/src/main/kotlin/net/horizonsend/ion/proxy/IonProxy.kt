package net.horizonsend.ion.proxy

import co.aikar.commands.BaseCommand
import co.aikar.commands.VelocityCommandManager
import com.google.inject.Inject
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.initializeCommon
import net.horizonsend.ion.common.utilities.loadConfiguration
import net.horizonsend.ion.proxy.annotations.GlobalCommand
import net.horizonsend.ion.proxy.annotations.GuildCommand
import net.horizonsend.ion.proxy.annotations.VelocityListener
import org.reflections.Reflections
import org.reflections.Store
import org.reflections.scanners.Scanners.SubTypes
import org.reflections.scanners.Scanners.TypesAnnotated
import org.reflections.util.QueryFunction
import org.slf4j.Logger

@Suppress("Unused")
@Plugin(id = "ion", name = "Ion") // While we do not use this for generating velocity-plugin.json, ACF requires it.
class IonProxy @Inject constructor(
	private val velocity: ProxyServer,
	private val logger: Logger,
	@DataDirectory
	private val dataDirectory: Path
) {
	private val configuration: ProxyConfiguration = loadConfiguration(dataDirectory)

	private val jda = try {
		JDABuilder.createLight(configuration.discordBotToken)
			.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			.setChunkingFilter(ChunkingFilter.ALL)
			.disableCache(CacheFlag.values().toList())
			.setEnableShutdownHook(false)
			.build()
	}  catch (_: LoginException) {
		logger.warn("Failed to start JDA as it was unable to login to Discord!")
		null
	}

	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyInitializeEvent(event: ProxyInitializeEvent): EventTask = EventTask.async {
		initializeCommon(dataDirectory)

		val reflections = Reflections("net.horizonsend.ion.proxy")

		val commandManager = VelocityCommandManager(velocity, this)

		reflectionsRegister(reflections, TypesAnnotated.of(VelocityListener::class.java), "listeners") {
			velocity.eventManager.register(this, it)
		}

		reflectionsRegister(reflections, SubTypes.of(BaseCommand::class.java), "commands") {
			commandManager.registerCommand(it as BaseCommand)
		}

		jda?.let { jda ->
			val jdaCommandManager = JDACommandManager(jda, configuration)

			reflectionsRegister(reflections, TypesAnnotated.of(GlobalCommand::class.java), "global discord commands") {
				jdaCommandManager.registerGlobalCommand(it)
			}

			reflectionsRegister(reflections, TypesAnnotated.of(GuildCommand::class.java), "guild discord commands") {
				jdaCommandManager.registerGuildCommand(it)
			}

			jdaCommandManager.build()

			velocity.scheduler.buildTask(this, Runnable {
				jda.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("with ${velocity.playerCount} players!"))
			}).repeat(5, TimeUnit.SECONDS).schedule()
		}

		removeOnlineRoleFromEveryone()
	}

	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyShutdownEvent(event: ProxyShutdownEvent): EventTask = EventTask.async {
		removeOnlineRoleFromEveryone()

		jda?.shutdown()
	}

	private fun <T> reflectionsRegister(
		reflections: Reflections,
		scanner: QueryFunction<Store, T>,
		name: String,
		execute: (Any) -> Unit
	) {
		reflections.get(scanner.asClass<T>())
			.map clazzMap@ { clazz ->
				val constructor = clazz.kotlin.constructors.first()

				constructor.javaConstructor?.newInstance(*constructor.parameters.map { when (it.type) {
					ProxyConfiguration::class.createType() -> configuration
					ProxyServer::class.createType() -> velocity
					IonProxy::class.createType() -> this
					JDA::class.createType(nullable = true) -> jda
					JDA::class.createType() -> if (jda != null) jda else {
						logger.error("${clazz.name} has not been loaded as it requires JDA which is unavailable.")
						return@clazzMap null
					}
					else -> {
						logger.error("Unable to provide ${it.type.javaType.typeName} to ${clazz.simpleName}.")
						return@clazzMap null
					}
				}}.toTypedArray())
			}
			.filterNotNull()
			.also { logger.info("Loading ${it.size} $name.") }
			.forEach(execute)
	}

	private fun removeOnlineRoleFromEveryone() = jda?.let {
		val guild = jda.getGuildById(configuration.discordServer) ?: return@let
		val role = guild.getRoleById(configuration.onlineRole) ?: return@let

		guild.getMembersWithRoles(role).forEach { member ->
			guild.removeRoleFromMember(member, role).queue()
		}
	}
}