package net.horizonsend.ion.server

import net.minecraft.core.BlockPos
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class AsteroidFeature(
	val name: String = "",
	val baseDensity: Double = 1.0,
	val densityFalloff: Double = 1.0,
	val outerRadius: Double = 0.0,
	val innerRadius: Double = 0.0,
//	val center: BlockPos,
)