package net.horizonsend.ion.server.generation.configuration

import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * @param baseAsteroidDensity: Roughly a base level of the number of asteroids per chunk
 * @param maxAsteroidSize: Maximum Size for an Asteroid
 * @param maxAsteroidOctaves: Maximum number of octaves for noise generation
 * @param blockPalettes: list of Palettes use for the asteroid materials
 * @param oreWeights:  list of Palettes used for ore placement
 * @see Palette
 */
@ConfigSerializable
data class AsteroidConfiguration(
	val baseAsteroidDensity: Double = 0.25,
	val maxAsteroidSize: Double = 14.0,
	val maxAsteroidOctaves: Int = 4,
	val blockPalettes: ArrayList<Palette> = arrayListOf(Palette(1,mapOf(Material.STONE to 1, Material.ANDESITE to 1))),
	val oreWeights: ArrayList<Palette>
)

/**
 * @param weight: Number of rolls for this Palette
 * @param materials: Map of Materials to their Weight
 *
 * Each Palette is a set of materials, and their weights that might make up an asteroid. Asteroids may pick from a list of Palettes.
 */
@ConfigSerializable
data class Palette(
	val weight: Int,
	val materials: Map<Material, Int>,
)