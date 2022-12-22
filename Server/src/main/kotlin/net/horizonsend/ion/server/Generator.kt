package net.horizonsend.ion.server

import java.util.Random
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sin
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.generation.AsteroidPopulator
import net.horizonsend.ion.server.generation.OrePopulator
import net.minecraft.core.BlockPos
import net.starlegacy.util.distance
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import org.bukkit.util.Vector
import org.bukkit.util.noise.PerlinOctaveGenerator

class Generator : ChunkGenerator() {
	private val configuration: AsteroidConfiguration = loadConfiguration(Ion.dataFolder.resolve("asteroids"), "asteroidconfiguration.conf")
	private val features: AsteroidFeature = loadConfiguration(Ion.dataFolder.resolve("asteroids"), "asteroidfeatures.conf")

	override fun generateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
	}

	override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> {
		return mutableListOf(AsteroidPopulator(), OrePopulator())
	}

	override fun shouldGenerateSurface(): Boolean {
		return false
	}

	override fun shouldGenerateCaves(): Boolean {
		return true
	}

	override fun shouldGenerateMobs(): Boolean {
		return false
	}

	override fun shouldGenerateDecorations(): Boolean {
		return false
	}

	override fun shouldGenerateStructures(): Boolean {
		return false
	}

	override fun shouldGenerateNoise(): Boolean {
		return false
	}
}