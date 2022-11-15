package net.horizonsend.ion.server.generation

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.BiFunction
import java.util.function.Function
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.FlatLevelSource
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.flat.AsteroidGeneratorSettings
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings
import net.minecraft.world.level.levelgen.structure.StructureSet

class NMSChunkGenerator(
	structureSetRegistry: Registry<StructureSet>,
	structureOverrides: Optional<HolderSet<StructureSet>>,
	biomeSource: BiomeSource,
	generationSettingsGetter: Function<Holder<Biome>, BiomeGenerationSettings>,
) : ChunkGenerator(structureSetRegistry, structureOverrides, biomeSource, generationSettingsGetter) {

	val CODEC: Codec<FlatLevelSource?>? =
		RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<FlatLevelSource?> ->
			commonCodec(instance).and(AsteroidGeneratorSettings.CODEC.fieldOf("settings").forGetter { obj: FlatLevelSource -> obj.settings() })
				.apply(instance, instance.stable(
					BiFunction { structureSetRegistry: Registry<StructureSet?>?, config: AsteroidGeneratorSettings? ->
						asteroidGeneratorSource(config)
					}))
		}

	private val AsteroidGeneratorSettings: AsteroidGeneratorSettings? = null

	fun asteroidGeneratorSource(config: AsteroidGeneratorSettings) {
		// CraftBukkit start
		// Optional optional = generatorsettingsflat.structureOverrides();
		// WorldChunkManagerHell worldchunkmanagerhell = new WorldChunkManagerHell(generatorsettingsflat.getBiome());

		// Objects.requireNonNull(generatorsettingsflat);
		this(structureSetRegistry, config, FixedBiomeSource(config.biome))
	}

	override fun codec(): Codec<out ChunkGenerator> {
		return Codec.unit(null)
	}

	override fun applyCarvers(
		chunkRegion: WorldGenRegion,
		seed: Long,
		noiseConfig: RandomState,
		biomeAccess: BiomeManager,
		structureAccessor: StructureManager,
		chunk: ChunkAccess,
		carverStep: GenerationStep.Carving,
	) {
		TODO("Not yet implemented")
	}

	override fun buildSurface(
		region: WorldGenRegion,
		structures: StructureManager,
		noiseConfig: RandomState,
		chunk: ChunkAccess,
	) {
		TODO("Not yet implemented")
	}

	override fun spawnOriginalMobs(region: WorldGenRegion) {
		TODO("Not yet implemented")
	}

	override fun getGenDepth(): Int {
		TODO("Not yet implemented")
	}

	override fun fillFromNoise(
		executor: Executor,
		blender: Blender,
		noiseConfig: RandomState,
		structureAccessor: StructureManager,
		chunk: ChunkAccess,
	): CompletableFuture<ChunkAccess> {
		TODO("Not yet implemented")
	}

	override fun getSeaLevel(): Int {
		TODO("Not yet implemented")
	}

	override fun getMinY(): Int {
		TODO("Not yet implemented")
	}

	override fun getBaseHeight(
		x: Int,
		z: Int,
		heightmap: Heightmap.Types,
		world: LevelHeightAccessor,
		noiseConfig: RandomState,
	): Int {
		TODO("Not yet implemented")
	}

	override fun getBaseColumn(x: Int, z: Int, world: LevelHeightAccessor, noiseConfig: RandomState): NoiseColumn {
		TODO("Not yet implemented")
	}

	override fun addDebugScreenInfo(text: MutableList<String>, noiseConfig: RandomState, pos: BlockPos) {
		TODO("Not yet implemented")
	}

}