package net.horizonsend.ion.server

import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class AsteroidConfiguration(
	val baseAsteroidSize: Double = 10.0,
	val baseAsteroidDensity: Double = 1.0,
	val baseAsteroidFalloff: Double = 10.0,
	val baseAsteroidRoughness: Double = 1.0,
	val blockPalettes: List<List<Map<Material, Double>>> = listOf(listOf(mapOf(Material.STONE to 1.0))), // Double is the number of rolls for this material.
	val oreWeights: List<Map<Material, Double>> = listOf(mapOf(Material.STONE to 1.0)), // Double is the number of rolls for this material.
)