package net.horizonsend.ion.server.generation

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import java.util.Random
import kotlin.io.path.listDirectoryEntries
import net.starlegacy.util.Vec3i
import net.starlegacy.util.nms
import net.starlegacy.util.placeSchematicEfficiently
import net.starlegacy.util.readSchematic
import net.starlegacy.util.toBukkitBlockData
import org.bukkit.Bukkit.getWorld

class StructurePopulator: BlockPopulator() {

	override fun populate(
		worldInfo: WorldInfo,
		random: Random,
		chunkX: Int,
		chunkZ: Int,
		limitedRegion: LimitedRegion
	) {
		val worldX = chunkX * 16
		val worldZ = chunkZ * 16

		val chunkRandom = Random(worldInfo.seed + (chunkZ * chunkX))

		val file = WorldEdit.getInstance().getWorkingDirectoryPath("schematics/asteroids").listDirectoryEntries()
		val index = chunkRandom.nextInt(file.size)
		val schematic: Clipboard = readSchematic(file[index].toFile()) ?: return

		if (chunkRandom.nextInt(10) == 5) {
			val dimensions = schematic.dimensions

			println(dimensions)

			val xWidth = dimensions.x
			val yWidth = dimensions.y
			val zWidth = dimensions.z

			val originX = chunkRandom.nextInt(15) + worldX
			val originY = chunkRandom.nextInt(worldInfo.maxHeight)
			val originZ = chunkRandom.nextInt(15) + worldZ

			placeSchematicEfficiently(schematic, getWorld(worldInfo.uid)!!, Vec3i(originX, originY, originZ), true)


//			for (x in schematic.minimumPoint.x ..schematic.maximumPoint.x) {
//				for (z in schematic.minimumPoint.z..schematic.maximumPoint.z) {
//					for (y in schematic.minimumPoint.y..schematic.maximumPoint.y) {
//
//						val location = BlockVector3.at(x, y, z)
//
//						val blockState = schematic.getBlock(location)
//						val material = blockState.toBukkitBlockData().material
//
//						println("$blockState, $material, $location")
//
//						if (material.isAir) continue
//
//						if (!limitedRegion.isInRegion(x + originX, y + originY,z + originZ)) continue
//
//						limitedRegion.setType(x + originX, y + originY, z + originZ, material)
//					}
//				}
//			}
		}
	}
}