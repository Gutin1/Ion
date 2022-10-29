package net.horizonsend.ion.server

import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class AsteroidConfiguration(
	val baseAsteroidSize: Double = 14.0,
	val baseAsteroidDensity: Double = 0.25, // Roughly a base level of the number of asteroids per chunk
	val baseAsteroidRoughness: Double = 4.0,
	val blockPalettes: ArrayList<List<Map<Material, Double>>> = arrayListOf(listOf(mapOf(Material.STONE to 1.0))), // Double is the number of rolls for this material.
	val oreWeights: ArrayList<Map<Material, Double>> = arrayListOf(mapOf(Material.STONE to 1.0)), // Double is the number of rolls for this material.
)