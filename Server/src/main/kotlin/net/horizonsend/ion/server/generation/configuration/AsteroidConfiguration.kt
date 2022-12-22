package net.horizonsend.ion.server.generation.configuration

import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class AsteroidConfiguration(
	val baseAsteroidDensity: Double = 0.25, // Roughly a base level of the number of asteroids per chunk
	val baseAsteroidSize: Double = 14.0,
	val baseAsteroidRoughness: Int = 4,
	val blockPalettes: MutableMap<Int, MutableMap<Material, Int>> = mutableMapOf(1 to mutableMapOf(Material.STONE to 1, Material.ANDESITE to 2), 3 to mutableMapOf(Material.MAGMA_BLOCK to 1, Material.NETHERRACK to 1)), // Int is the number of rolls for this material / palette.
	val oreWeights: Map<Material, Int> = mapOf(Material.STONE to 1, Material.SCULK to 1), // Int is the number of rolls for this material.
)