package net.horizonsend.ion.server.generation

import java.util.Random
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer
import net.starlegacy.util.add
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo


class OrePopulator: BlockPopulator() {
	private val configuration: AsteroidConfiguration = loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_configuration.conf")

	private val OFFSETS = arrayOf(
		BlockFace.NORTH,
		BlockFace.EAST,
		BlockFace.SOUTH,
		BlockFace.WEST,
		BlockFace.UP,
		BlockFace.DOWN,
		BlockFace.NORTH_EAST,
		BlockFace.NORTH_WEST,
		BlockFace.SOUTH_EAST,
		BlockFace.SOUTH_WEST)

	// need to redo this
	override fun populate(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, limitedRegion: LimitedRegion, ) {
		for (count in 1..128) {
			val worldRandom = Random(chunkX + (chunkZ * count) + worldInfo.seed)
			val startX: Double = worldRandom.nextInt(16) + chunkX * 16.0
			val startY: Double = worldRandom.nextInt(worldInfo.maxHeight - 10) + worldInfo.minHeight.toDouble() + 10
			val startZ: Double = worldRandom.nextInt(16) + chunkZ * 16.0
			val location = Location(null, startX, startY, startZ)

			if (!limitedRegion.isInRegion(location)) continue
			if (limitedRegion.getType(location) == Material.AIR) continue

			//println(location)

			for (seize in worldRandom.nextInt(128) downTo 1) {
				val startMaterial: Material = limitedRegion.getType(location)
				val ore = oreWeights()[worldRandom.nextInt(oreWeights().size)]

				if (!configuration.blockPalettes.any { it.value.keys.contains(startMaterial) }) {
					continue
				}

				if (limitedRegion.isInRegion(location)) {
					limitedRegion.setType(location, ore)
				}
				val blockFace = OFFSETS[worldRandom.nextInt(OFFSETS.size)]
				location.add(blockFace.modX, blockFace.modY, blockFace.modZ)
			}
		}
	}

	private fun oreWeights(): List<Material> {
		val weightedList = mutableListOf<Material>()

		for (ore in configuration.oreWeights) {
			for (occurrence in ore.value downTo 0)
				weightedList.add(ore.key)
		}

		return weightedList
	}
}