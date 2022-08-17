package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.Default
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.annotations.CommandMeta
import net.horizonsend.ion.proxy.messageEmbed

@CommandMeta("info", "List of useful links.")
class DiscordInfoCommand {
	@Default
	@Suppress("Unused")
	fun onInfoCommand(event: SlashCommandInteractionEvent) {
		event.replyEmbeds(
			messageEmbed(
				title = "Here are a few links of potential use:", description =
				"[Survival Web Map](https://survival.horizonsend.net)\n" +
					"[Creative Web Map](https://creative.horizonsend.net)\n" +
					"[Discord Server](https://discord.gg/RPvgQsGzKM)\n" +
					"[Resource Pack](https://github.com/HorizonsEndMC/ResourcePack)\n" +
					"[Wiki](https://wiki.horizonsend.net)"
			)
		).queue()
	}
}