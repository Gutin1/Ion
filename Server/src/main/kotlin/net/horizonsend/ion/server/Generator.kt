package net.horizonsend.ion.server

import java.util.Random
import kotlin.math.abs
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.minecraft.core.BlockPos
import net.starlegacy.util.d
import net.starlegacy.util.distance
import org.bukkit.Material
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import org.bukkit.util.Vector
import org.bukkit.util.noise.PerlinOctaveGenerator

class Generator : ChunkGenerator() {
	private val configuration: AsteroidConfiguration = loadConfiguration(Ion.dataFolder.resolve("asteroids"), "asteroidconfiguration.conf")
	private val features: AsteroidConfiguration = loadConfiguration(Ion.dataFolder.resolve("asteroids"), "asteroidfeatures.conf")
	private val fileName = "asteroidfeatures.json"

	// Generates the asteroids based on the coordinates supplied.
	// This is the first step in world generation. It only needs to generate the canvas which features are applied to.
	override fun generateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
		val asteroids = mutableSetOf<Asteroid>()
		val worldX = chunkX * 16
		val worldZ = chunkZ * 16
		var searchRange = 1
		val thread = Thread().start()

		//while ((asteroids.isEmpty())) {
			for (originX in chunkX - searchRange..chunkX + searchRange) {
				for (originZ in chunkZ - searchRange.. chunkZ + searchRange) {
					val chunkAsteroids = thread.run { generateAsteroids(worldInfo, originX, originZ) }

					if (chunkAsteroids.isEmpty()) continue

					chunkAsteroids.forEach { asteroids.add(it) }
				}
			}
			//searchRange++
		//}

		if (asteroids.isEmpty()) return
		// Iterate through world
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in worldInfo.minHeight..worldInfo.maxHeight) {
					val closestAsteroid = thread.run { containsAsteroid(worldX + x, y, worldZ + z, asteroids) }

					//Place asteroid
					if (closestAsteroid.second) {

						val blockPalette = "B"

					} else chunkData.setBlock(x, y, z, Material.AIR)
				}
			}
		}
	}

	//TODO later
//	override fun generateSurface(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
//		super.generateSurface(worldInfo, random, chunkX, chunkZ, chunkData)
//	}

	override fun generateBedrock(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
		if (chunkData.minHeight == worldInfo.minHeight) {
			for (x in 0..15) {
				for (z in 0..15) {
					chunkData.setBlock(x, chunkData.minHeight, z, org.bukkit.Material.AIR)
				}
			}
		}
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

	// Takes the features .
//	private fun parseDensity(equation: String, baseDensity: Double, fallOff: Double, x: Int, y: Int, z: Int): Double {
//		val formattedEquation = formatEquation(equation, baseDensity, fallOff, x, y, z)
//
//		val finalDensity = Expressions().eval(formattedEquation).toDouble()
//		val finalDensity = 1.0
//		return finalDensity
//	}

//	private fun formatEquation(
//		equation: String, baseDensity: Double, fallOff: Double, x: Int, y: Int, z: Int,
//	): String {
//		return equation.lowercase()
//			.replace("basedensity", "$baseDensity")
//			.replace("falloff", "$fallOff")
//			.replace("x", "$x")
//			.replace("y", "$y")
//			.replace("z", "$z")
//	}

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

		val asteroidLocations: MutableMap<BlockPos, Double> = mutableMapOf()

		for (count in 0..16) { // generates 16 random points across the chunk (chunkX, chunkZ), selects those with the highest density.
			// Base asteroid density is 0.25 asteroids per chunk.
			val offsetNoise = PerlinOctaveGenerator(worldInfo.seed + count, 1)

			val xSample = offsetNoise.noise((worldX).toDouble(), (worldX).toDouble(), 1.0, 1.0, true)// I hate floating point errors
			val x = (((xSample + 1) / 2).d() * 15.0) + worldX.toDouble() // random value between 0-15 added to the chunk location to get real location

			val zSample = offsetNoise.noise((worldZ).toDouble(), (worldZ).toDouble(), 1.0, 1.0, true)
			val z = (((zSample + 1) / 2).d() * 15.0) + worldZ.toDouble() // random value between 0-15 added to the chunk location to get real location

			val ySample = offsetNoise.noise(x, z, 0.0, 0.5)
			val y = worldInfo.maxHeight * ( ySample + 0.5) // random value between 0-build limit added to the chunk location to get real location


			val perlinOctaveGenerator = PerlinOctaveGenerator(worldInfo.seed, 1)
			perlinOctaveGenerator.setScale(0.001)
			val perlinSample = perlinOctaveGenerator.noise(x * y, y, z * y, 1.0, 1.0, true)

			//println("X Sample: $xSample, X: $x, Y Sample: $ySample, Y: $y, Z Sample: $zSample, Z: $z, perlinSample: $perlinSample")

			asteroidLocations[BlockPos(x.toInt(), y.toInt(), z.toInt())] = perlinSample
		}

		for (asteroidLocation in asteroidLocations) {
			val location = asteroidLocation.key
			if (asteroidLocation.value > /*finalAsteroidDensity(worldInfo, location.blockX, location.blockY, location.blockZ)*/ 0.5) {

				val noise = PerlinOctaveGenerator(location.hashCode().toLong(), 1)
				noise.setScale(0.05)

				// Get material palette
				val paletteSample = noise.noise(location.x.toDouble(), location.y.toDouble(), location.z.toDouble(), true)
				val blockPalette: List<Map<Material, Double>> = configuration.blockPalettes[configuration.blockPalettes.size * ((paletteSample + 1) / 2).toInt()]

				val size = abs(noise.noise(location.x.toDouble(), location.y.toDouble(), location.z.toDouble()) + 1) * configuration.baseAsteroidSize
				val special = false
				val ores = configuration.oreWeights
				val octaves = abs(noise.noise(location.x.toDouble(), location.y.toDouble(), location.z.toDouble()) + 1) * configuration.baseAsteroidRoughness

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
				val offset = asteroid.noise.noise(x.toDouble(), y.toDouble(), z.toDouble(), 0.0, 1.0, false) * asteroid.size * 2
				val size = vector.multiply(offset).length()
				val distanceToSurface = distance(x, y, z, location.x, location.y, location.z) - size

				distances[asteroid] = distanceToSurface
			}
		}
		val lowestEntry = distances.minBy { it.value }

		return Pair(lowestEntry.key, lowestEntry.value < 0)
	}

	data class AsteroidFeature(
		val name: String = "",
		val baseDensity: Double = 1.0,
		val densityFalloff: Double = 1.0,
		val outerRadius: Double = 0.0,
		val innerRadius: Double = 0.0,
		val center: BlockPos,
	)

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