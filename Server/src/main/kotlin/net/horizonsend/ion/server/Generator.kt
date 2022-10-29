package net.horizonsend.ion.server

import java.util.Random
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sin
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.minecraft.core.BlockPos
import net.starlegacy.util.distance
import org.bukkit.Material
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import org.bukkit.util.Vector
import org.bukkit.util.noise.PerlinOctaveGenerator

class Generator : ChunkGenerator() {
	private val configuration: AsteroidConfiguration = loadConfiguration(Ion.dataFolder.resolve("asteroids"), "asteroidconfiguration.conf")
	private val features: AsteroidFeature = loadConfiguration(Ion.dataFolder.resolve("asteroids"), "asteroidfeatures.conf")

	// Generates the asteroids based on the coordinates supplied.
	// This is the first step in world generation. It only needs to generate the canvas which features are applied to.
	override fun generateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
		val asteroids = mutableSetOf<Asteroid>()
		val worldX = chunkX * 16
		val worldZ = chunkZ * 16
		val searchRange = 2
		val thread = Thread().start()

		//while ((asteroids.isEmpty())) { // causes problems (Loses the benefits of searching multiple chunks if one is found in the same chunk)
		// Searches a 3x3 radius centered on the chunk for asteroids. Multiple chunks searched to avoid cutoffs. There might be a better way of doing this.
		for (originX in chunkX - searchRange..chunkX + searchRange) {
			for (originZ in chunkZ - searchRange.. chunkZ + searchRange) {
				val chunkAsteroids = thread.run { generateAsteroids(worldInfo, originX, originZ) }

				if (chunkAsteroids.isEmpty()) continue

				chunkAsteroids.forEach { asteroids.add(it) }
			}
			//}
			//searchRange++
		}

		if (asteroids.isEmpty()) return
		//println(asteroids)

		// Iterate through world
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in worldInfo.minHeight..worldInfo.maxHeight) {
					val closestAsteroid = thread.run { containsAsteroid(worldX + x, y, worldZ + z, asteroids) }

					//Place asteroid
					if (closestAsteroid.second) { // TODO: Block selector logic (smooth noise (sawtooth wave?)), ore population
						val asteroid = closestAsteroid.first
						val noise = asteroid.noise
						noise.setScale(0.75)
						val paletteSample = (((noise.noise(worldX + x.toDouble(), y.toDouble(), worldZ + z.toDouble(), 1.0, 1.0, true) + 1) / 2) * asteroid.palette.size).toInt()
						val material = asteroid.palette[paletteSample].keys.random()

						chunkData.setBlock(x, y, z, material)

					} else chunkData.setBlock(x, y, z, Material.AIR)
				}
			}
		}
	}

	//TODO later
//	override fun generateSurface(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
//		super.generateSurface(worldInfo, random, chunkX, chunkZ, chunkData)
//	}

//	override fun generateBedrock(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
//		if (chunkData.minHeight == worldInfo.minHeight) {
//			for (x in 0..15) {
//				for (z in 0..15) {
//					chunkData.setBlock(x, chunkData.minHeight, z, org.bukkit.Material.AIR)
//				}
//			}
//		}
//	}

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

	// Takes the features .
	private fun parseDensity(x: Int, y: Int, z: Int): Double { // TODO

		val finalDensity = configuration.baseAsteroidDensity
		return finalDensity
	}

	// Gets the asteroid density for each feature at a coordinate, and finds the max.
//	private fun finalAsteroidDensity(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Double {
//		val densities = mutableSetOf<Double>()
//		val baseDensity = parseDensity(configuration.baseAsteroidDistribution,
//			configuration.baseAsteroidDensity,
//			configuration.baseAsteroidFalloff,
//			x,
//			y,
//			z)
//		densities.add(baseDensity)
//
//		for (feature in asteroidFeatures) {
//			val featuresDensities = parseFeatureDensity(feature, x, y, z)
//			densities.add(featuresDensities)
//		}
//
//		return densities.maxOrNull() ?: configuration.baseAsteroidDensity
//	}


	// Finds asteroid locations in a chunk. Run 9 times for the surrounding chunks to avoid cutoffs.
	private fun generateAsteroids(worldInfo: WorldInfo, chunkX: Int, chunkZ: Int): Set<Asteroid> {
		val asteroids: MutableSet<Asteroid> = mutableSetOf()
		val worldX = chunkX * 16
		val worldZ = chunkZ * 16
		val random = Random(worldInfo.seed)

		val asteroidLocations: MutableMap<BlockPos, Double> = mutableMapOf()

		// Base asteroid density is 0.25 asteroids per chunk.
		for (count in 0..(1 * ceil(parseDensity(worldX, worldInfo.maxHeight / 2, worldZ)).toInt())) {
			val x = abs(((worldX + 115249 + count) % 30) - 15) + worldX // random value between 0-15 added to the chunk location to get real location
			val z = abs(((worldZ + 115249 + count) % 30) - 15) + worldZ // random value between 0-15 added to the chunk location to get real location
			val y = (abs(((x * z + 115249 + count) % ((worldInfo.maxHeight + worldInfo.minHeight) * 2)) - worldInfo.maxHeight)) - worldInfo.minHeight // random value between 0-build limit added to the chunk location to get real location

			val chanceSeed = BlockPos(x, y, z).hashCode() + count
			val chance = abs((chanceSeed % 200) - 100.0) // random number out of 100

			asteroidLocations[BlockPos(x, y, z)] = chance

			//println("x: $x, y: $y, z: $z, chance: $chance, density: ${parseDensity(x, y, z) * 10}")
		}
		for (asteroidLocation in asteroidLocations) {
			val location = asteroidLocation.key
			if (asteroidLocation.value < (parseDensity(location.x, location.y, location.z) * 10)) { // if asteroid chance is less than the percentage of asteroids which should contain a chunk

				val noise = PerlinOctaveGenerator(location.hashCode().toLong(), 1)
				noise.setScale(0.05)

				// Get material palette 115249
				val blockPalette: List<Map<Material, Double>> = configuration.blockPalettes[abs(((location.hashCode()) % (configuration.blockPalettes.size * 2)) - configuration.blockPalettes.size)]

				val size = random.nextDouble(5.0, (configuration.blockPalettes.size * 2.0))
				val special = false //TODO
				val ores = configuration.oreWeights
				val octaves = ((sin(location.hashCode().toDouble() + size) + 1) / 2) * configuration.baseAsteroidRoughness

				if (location.y - size > worldInfo.minHeight && location.y + size < worldInfo.maxHeight)
					asteroids.add(Asteroid(location, blockPalette, size, special, octaves.toInt(), ores, noise))
			}
		}
		if (asteroids.isEmpty()) return emptySet()
		return asteroids
	}


	// determines if the block specified contains an asteroid.
	private fun containsAsteroid(x: Int, y: Int, z: Int, asteroids: Set<Asteroid>): Pair<Asteroid, Boolean> {
		val distances: MutableMap<Asteroid, Double> = mutableMapOf()

		for (asteroid in asteroids) {
			val location = asteroid.location
			if (!asteroid.special) {
				val vector = Vector(x, y, z).subtract(Vector(asteroid.location.x, asteroid.location.y, asteroid.location.z)).normalize()
				asteroid.noise.setScale(0.65 / asteroid.size)
				val offset = asteroid.noise.noise(x.toDouble(), y.toDouble(), z.toDouble(), 0.0, 1.0, false) * asteroid.size * 2
				val size = vector.multiply(offset).length()
				val distanceToSurface = distance(x, y, z, location.x, location.y, location.z) - size

				//TODO: Ore selection

				distances[asteroid] = distanceToSurface
			}
		}
		val lowestEntry = distances.minBy { it.value }

		return Pair(lowestEntry.key, lowestEntry.value < 0)
	}

	data class Asteroid(
		val location: BlockPos,
		val palette: List<Map<Material, Double>>,
		val size: Double,
		val special: Boolean, // Determines if it will have a special features
		val octaves: Int,
		val ores: List<Map<Material, Double>>,
		val noise: PerlinOctaveGenerator,
	)
}