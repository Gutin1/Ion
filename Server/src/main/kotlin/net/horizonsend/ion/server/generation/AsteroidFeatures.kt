package net.horizonsend.ion.server.generation

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class AsteroidFeatures(
	val features: List<AsteroidFeature> = listOf(AsteroidFeature("Example", "ExampleName",1.0, 100.0, 10.0, 420, 100, 69000))
)

@ConfigSerializable
data class AsteroidFeature(
	val name: String = "", // unused, for ease in configuring
	val worldName: String = "",
	val baseDensity: Double = 1.0,
	val tubeSize: Double = 0.0, // Distance from the center of the tube to the center of the taurus
	val tubeRadius: Double = 0.0, // Radius of the tube
	val x: Int,
	val y: Int,
	val z: Int
)