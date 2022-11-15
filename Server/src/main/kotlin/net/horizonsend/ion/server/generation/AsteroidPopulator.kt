package net.horizonsend.ion.server.generation

import java.util.Random
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer
import net.minecraft.core.BlockPos
import org.bukkit.Material
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.SimplexOctaveGenerator

class AsteroidPopulator: BlockPopulator() {
	// default asteroid configuration values
	private val configuration: AsteroidConfiguration =
		loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_configuration.conf")

	// features (e.g. asteroid belts)
	private val features: AsteroidFeatures =
		loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_features.conf")

	override fun populate(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, limitedRegion: LimitedRegion) {
		val noise = SimplexOctaveGenerator(Random(worldInfo.seed), 1)
		val worldX = chunkX * 16
		val worldZ = chunkZ * 16

		val chunkDensity = (1 * ceil(parseDensity(worldInfo, worldX.toDouble(), worldInfo.maxHeight / 2.0, worldZ.toDouble())).toInt())

		// Generate a number of random asteroids in a chunk, proportional to the density in a portion of the chunk. Allows densities of X>1 asteroid per chunk.
		for (count in 0..chunkDensity) {
			val asteroidRandom = Random(chunkX + (chunkZ * count) + worldInfo.seed)

			//Random coordinate generation.
			val asteroidX = asteroidRandom.nextInt(0, 15) + worldX
			val asteroidZ = asteroidRandom.nextInt(0, 15) + worldZ
			val asteroidY = asteroidRandom.nextInt(worldInfo.minHeight, worldInfo.maxHeight)

			val asteroidLoc = BlockPos(asteroidX, asteroidY, asteroidZ)

			// random number out of 100, chance of asteroid's generation. For use in selection.
			val chance = abs((abs(BlockPos(asteroidX, asteroidY, asteroidZ).hashCode()) % 200.0) - 100.0)

			// Selects some asteroids that are generated. Allows for densities of 0<X<1 asteroids per chunk.
			if (chance < (chunkDensity * 10)
			) {
				val asteroid = generateAsteroid(asteroidLoc, asteroidRandom)
				val palette = materialWeights(asteroid.palette)

				if (asteroid.size + asteroidY > worldInfo.maxHeight) continue

				for (x in worldX - limitedRegion.buffer..worldX + 15 + limitedRegion.buffer) {
					for (z in worldZ - limitedRegion.buffer..worldZ + 15 + limitedRegion.buffer) {
						for (y in (asteroidY - (2 * asteroid.size)).toInt() until (asteroidY + (2 * asteroid.size)).toInt()) {
							noise.setScale(0.15)
							var fullNoise = 0.0

							val xDouble = x.toDouble()
							val yDouble = y.toDouble()
							val zDouble = z.toDouble()

							for (octave in 0..asteroid.octaves) {
								noise.setScale((0.5 + octave) / sqrt((asteroid.size * 3).pow(2)))
								val offset = abs(noise.noise(xDouble, yDouble, zDouble, 0.0, 1.0, false)
										* (asteroid.size / (1 + octave)))
								fullNoise += offset
							}

							if (((xDouble - asteroidX).pow(2) + (yDouble - asteroidY).pow(2) + (zDouble - asteroidZ).pow(2) < (fullNoise).pow(2))) {
								if (limitedRegion.isInRegion(x, y, z)) {
									val paletteSample = (((noise.noise(worldX + xDouble, yDouble, worldZ + zDouble, 1.0, 1.0, true) + 1) / 2) * (palette.size - 1)).toInt()
									val material = palette[paletteSample]

									limitedRegion.setType(x, y ,z, material)
								} else continue
							} else continue
						}
					}
				}
			}
		}
	}

	private fun generateAsteroid(location: BlockPos, random: Random): Asteroid {
		// Get material palette
		val palette = random.nextInt(configuration.blockPalettes.keys.sum())
		val blockPalette: Map<Material, Int> = paletteWeights()[palette]

		val size = random.nextDouble(5.0, configuration.baseAsteroidSize)
		val ores = configuration.oreWeights
		val octaves = configuration.baseAsteroidRoughness

		return Asteroid(location, blockPalette, size, octaves, ores)
	}

	private fun parseDensity(worldInfo: WorldInfo, x: Double, y: Double, z: Double): Double {
		val densities = mutableSetOf<Double>()
		densities.add(configuration.baseAsteroidDensity)

		for (feature in features.features) {
			if (feature.worldName != worldInfo.name) continue
			if ((sqrt((x - feature.x).pow(2) + (z - feature.z).pow(2)) - feature.tubeSize).pow(2) + (y - feature.y).pow(2) < feature.tubeRadius.pow(2)
			) { densities.add(feature.baseDensity) }
		}

		return densities.max()
	}

	private fun paletteWeights(): List<Map<Material, Int>> {
		val weightedList = mutableListOf<Map<Material, Int>>()

		for (Palette in configuration.blockPalettes) {
			for (occurrence in Palette.key downTo 0) {
				weightedList.add(Palette.value)
			}
		}

		return weightedList
	}

	private fun materialWeights(palette: Map<Material, Int>): List<Material> {
		val weightedList = mutableListOf<Material>()

		for (Material in palette) {
			for (occurrence in Material.value downTo 0) {
				weightedList.add(Material.key)
			}
		}

		return weightedList
	}
	data class Asteroid(
		val location: BlockPos,
		val palette: Map<Material, Int>,
		val size: Double,
		val octaves: Int,
		val ores: Map<Material, Int>,
	)
}