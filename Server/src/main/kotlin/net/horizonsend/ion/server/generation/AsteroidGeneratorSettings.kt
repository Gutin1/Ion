package net.minecraft.world.level.levelgen.flat

import com.google.common.collect.Lists
import com.mojang.datafixers.util.Function6
import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import java.util.function.Function
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.RegistryCodecs
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.resources.RegistryOps
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets
import net.minecraft.world.level.levelgen.structure.StructureSet

class AsteroidGeneratorSettings(
	private val structureOverrides: Optional<HolderSet<StructureSet?>?>,
	private val biomes: Registry<Biome>,
) {
	val layersInfo: MutableList<FlatLayerInfo> = Lists.newArrayList()
	private var biome: Holder<Biome>
	val layers: MutableList<BlockState?>
	private var voidGen = false
	private var decoration = false
	private var addLakes = false

	private constructor(
		biomeRegistry: Registry<Biome>,
		structureOverrides: Optional<HolderSet<StructureSet?>?>,
		layers: List<FlatLayerInfo>,
		hasLakes: Boolean,
		hasFeatures: Boolean,
		biome: Optional<Holder<Biome>>,
	) : this(structureOverrides, biomeRegistry) {
		if (hasLakes) {
			setAddLakes()
		}
		if (hasFeatures) {
			setDecoration()
		}
		layersInfo.addAll(layers)
		updateLayers()
		if (biome.isEmpty) {
			LOGGER.error("Unknown biome, defaulting to plains")
			this.biome = biomeRegistry.getOrCreateHolderOrThrow(Biomes.PLAINS)
		} else {
			this.biome = biome.get()
		}
	}

	init {
		biome = biomes.getOrCreateHolderOrThrow(Biomes.PLAINS)
		layers = Lists.newArrayList()
	}

	fun withLayers(
		layers: List<FlatLayerInfo>,
		structureOverrides: Optional<HolderSet<StructureSet?>?>,
	): FlatLevelGeneratorSettings {
		val flatLevelGeneratorSettings = FlatLevelGeneratorSettings(structureOverrides,
			biomes)
		for (flatLayerInfo in layers) {
			flatLevelGeneratorSettings.layersInfo.add(FlatLayerInfo(flatLayerInfo.height,
				flatLayerInfo.blockState.block))
			flatLevelGeneratorSettings.updateLayers()
		}
		flatLevelGeneratorSettings.setBiome(biome)
		if (decoration) {
			flatLevelGeneratorSettings.setDecoration()
		}
		if (addLakes) {
			flatLevelGeneratorSettings.setAddLakes()
		}
		return flatLevelGeneratorSettings
	}

	fun setDecoration() {
		decoration = true
	}

	fun setAddLakes() {
		addLakes = true
	}

	fun adjustGenerationSettings(biomeEntry: Holder<Biome>): BiomeGenerationSettings {
		return if (biomeEntry != biome) {
			biomeEntry.value().generationSettings
		} else {
			val biomeGenerationSettings = getBiome().value().generationSettings
			val builder = BiomeGenerationSettings.Builder()
			if (addLakes) {
				builder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND)
				builder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_SURFACE)
			}
			val bl = (!voidGen || biomeEntry.`is`(Biomes.THE_VOID)) && decoration
			if (bl) {
				val list = biomeGenerationSettings.features()
				for (i in list.indices) {
					if (i != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal && i != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal) {
						for (holder in list[i]) {
							builder.addFeature(i, holder)
						}
					}
				}
			}
			val list2 = layers
			for (j in list2.indices) {
				val blockState = list2[j]
				if (!Heightmap.Types.MOTION_BLOCKING.isOpaque.test(blockState)) {
					list2[j] = null as BlockState?
					builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PlacementUtils.inlinePlaced(
						Feature.FILL_LAYER, LayerConfiguration(j, blockState)))
				}
			}
			builder.build()
		}
	}

	fun structureOverrides(): Optional<HolderSet<StructureSet?>?> {
		return structureOverrides
	}

	fun getBiome(): Holder<Biome> {
		return biome
	}

	fun setBiome(biome: Holder<Biome>) {
		this.biome = biome
	}

	fun updateLayers() {
		layers.clear()
		for (flatLayerInfo in layersInfo) {
			for (i in 0 until flatLayerInfo.height) {
				layers.add(flatLayerInfo.blockState)
			}
		}
		voidGen = layers.stream().allMatch { state: BlockState? ->
			state!!.`is`(Blocks.AIR)
		}
	}

	companion object {
		private val LOGGER = LogUtils.getLogger()
		val CODEC: Codec<FlatLevelGeneratorSettings> =
			RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<Any> ->
				instance.group(RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY)
					.forGetter(
						Function { flatLevelGeneratorSettings: Any -> flatLevelGeneratorSettings.biomes }),
					RegistryCodecs.homogeneousList(Registry.STRUCTURE_SET_REGISTRY)
						.optionalFieldOf("structure_overrides").forGetter(
							Function { flatLevelGeneratorSettings: Any -> flatLevelGeneratorSettings.structureOverrides }),
					FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(
						Function<FlatLevelGeneratorSettings, List<FlatLayerInfo>> { obj: FlatLevelGeneratorSettings -> obj.layersInfo }),
					Codec.BOOL.fieldOf("lakes").orElse(false).forGetter(
						Function { flatLevelGeneratorSettings: Any -> flatLevelGeneratorSettings.addLakes }),
					Codec.BOOL.fieldOf("features").orElse(false).forGetter(
						Function { flatLevelGeneratorSettings: Any -> flatLevelGeneratorSettings.decoration }),
					Biome.CODEC.optionalFieldOf("biome").orElseGet { Optional.empty() }
						.forGetter(
							Function { flatLevelGeneratorSettings: Any ->
								Optional.of<Any>(flatLevelGeneratorSettings.biome)
							})).apply<Any>(instance,
					Function6<T1, T2, T3, T4, T5, T6, Any> { FlatLevelGeneratorSettings() })
			}.comapFlatMap(
				Function<Any, DataResult<out S?>> { config: Any ->
					validateHeight(config)
				}, Function.identity()).stable()

		private fun validateHeight(config: FlatLevelGeneratorSettings): DataResult<FlatLevelGeneratorSettings?> {
			val i = config.layersInfo.stream().mapToInt { obj: FlatLayerInfo -> obj.height }.sum()
			return if (i > DimensionType.Y_SIZE) DataResult.error("Sum of layer heights is > " + DimensionType.Y_SIZE,
				config) else DataResult.success(config)
		}

		fun getDefault(
			biomeRegistry: Registry<Biome>,
			structureSetRegistry: Registry<StructureSet?>,
		): AsteroidGeneratorSettings {
			val holderSet: HolderSet<StructureSet?> =
				HolderSet.direct(structureSetRegistry.getHolderOrThrow(BuiltinStructureSets.STRONGHOLDS),
					structureSetRegistry.getHolderOrThrow(BuiltinStructureSets.VILLAGES))
			val asteroidGeneratorSettings = AsteroidGeneratorSettings(Optional.of(holderSet), biomeRegistry)
			asteroidGeneratorSettings.biome = biomeRegistry.getOrCreateHolderOrThrow(Biomes.PLAINS)
			asteroidGeneratorSettings.layersInfo.add(FlatLayerInfo(1, Blocks.BEDROCK))
			asteroidGeneratorSettings.layersInfo.add(FlatLayerInfo(2, Blocks.DIRT))
			asteroidGeneratorSettings.layersInfo.add(FlatLayerInfo(1, Blocks.GRASS_BLOCK))
			asteroidGeneratorSettings.updateLayers()
			return asteroidGeneratorSettings
		}
	}
}