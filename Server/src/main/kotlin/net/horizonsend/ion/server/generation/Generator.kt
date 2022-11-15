package net.horizonsend.ion.server.generation

import net.minecraft.world.level.levelgen.XoroshiroRandomSource
import net.minecraft.world.level.levelgen.XoroshiroRandomSource.XoroshiroPositionalRandomFactory
import org.bukkit.World
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo

class Generator : ChunkGenerator() {
	override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider {
		return CustomBiomeProvider()
	}

	override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> {
		return mutableListOf(/*OrePopulator(), */AsteroidPopulator(),
			StructurePopulator())
	}

	override fun shouldGenerateSurface(): Boolean {
		return true
	}

	override fun shouldGenerateCaves(): Boolean {
		return true
	}

	override fun shouldGenerateMobs(): Boolean {
		return true
	}

	override fun shouldGenerateDecorations(): Boolean {
		return true
	}

	override fun shouldGenerateStructures(): Boolean {
		return true
	}

	override fun shouldGenerateNoise(): Boolean {
		return false
	}
}