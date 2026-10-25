package com.wcnm.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

/**
 * Vanilla-overworld-style terrain generation, wrapped with the underworld's post processing:
 * sparse lakes, bounded rivers, soul-block surface and the fifty building sites.
 *
 * <p>The heavy terrain/heightmap/cave work is delegated to a real
 * {@link NoiseBasedChunkGenerator} using the vanilla overworld noise router.</p>
 */
public class UnderworldChunkGenerator extends ChunkGenerator {
	public static final MapCodec<UnderworldChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.getBiomeSource()),
			NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
		).apply(instance, instance.stable(UnderworldChunkGenerator::new))
	);

	private final Holder<NoiseGeneratorSettings> settings;
	private final NoiseBasedChunkGenerator delegate;

	public UnderworldChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
		super(biomeSource);
		this.settings = settings;
		this.delegate = new NoiseBasedChunkGenerator(biomeSource, settings);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public CompletableFuture<ChunkAccess> buildTerrain(
		ChunkAccess chunk,
		Blender blender,
		RandomState randomState,
		StructureManager structureManager,
		BiomeManager biomeManager,
		@Nullable WorldGenRegion carverBiomeRegion,
		Set<Holder<Biome>> possibleBiomes
	) {
		return this.delegate
			.buildTerrain(chunk, blender, randomState, structureManager, biomeManager, carverBiomeRegion, possibleBiomes)
			.thenApply(generated -> {
				UnderworldPostProcess.process(generated, randomState, this.delegate, randomState.seed());
				return generated;
			});
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
		return this.delegate.getBaseHeight(x, z, type, heightAccessor, randomState);
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
		return this.delegate.getBaseColumn(x, z, heightAccessor, randomState);
	}

	@Override
	public int getSeaLevel() {
		return this.delegate.getSeaLevel();
	}

	@Override
	public int getMinY() {
		return this.delegate.getMinY();
	}

	@Override
	public int getGenDepth() {
		return this.delegate.getGenDepth();
	}

	@Override
	public int getSpawnHeight(LevelHeightAccessor heightAccessor) {
		return UnderworldPostProcess.SPAWN_Y + 2;
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
		this.delegate.spawnOriginalMobs(worldGenRegion);
	}

	@Override
	public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
		this.delegate.addDebugScreenInfo(result, randomState, feetPos, samplerContext);
	}

	public UnderworldPostProcess.Site findNearestBuilding(ServerLevel level, int x, int z, int radiusBlocks) {
		RandomState randomState = level.getChunkSource().randomState();
		long seed = randomState.seed();
		return UnderworldPostProcess.findNearestSite(seed, x, z, radiusBlocks,
			(bx, bz) -> this.delegate.getBaseHeight(bx, bz, Heightmap.Types.WORLD_SURFACE, level, randomState));
	}

	public UnderworldPostProcess.Site findNearestBuilding(ServerLevel level, int x, int z, int number,
														  int radiusBlocks) {
		RandomState randomState = level.getChunkSource().randomState();
		long seed = randomState.seed();
		return UnderworldPostProcess.findNearestSite(seed, x, z, number, radiusBlocks,
			(bx, bz) -> this.delegate.getBaseHeight(bx, bz, Heightmap.Types.WORLD_SURFACE, level, randomState));
	}

	public List<UnderworldPostProcess.Site> findBuildingsInRadius(ServerLevel level, int x, int z, int radiusBlocks) {
		RandomState randomState = level.getChunkSource().randomState();
		long seed = randomState.seed();
		return UnderworldPostProcess.findSitesInRadius(seed, x, z, radiusBlocks,
			(bx, bz) -> this.delegate.getBaseHeight(bx, bz, Heightmap.Types.WORLD_SURFACE, level, randomState));
	}

	public List<UnderworldPostProcess.Site> findBuildingsInChunk(ServerLevel level, int chunkX, int chunkZ) {
		RandomState randomState = level.getChunkSource().randomState();
		long seed = randomState.seed();
		return UnderworldPostProcess.findSitesInChunk(seed, chunkX, chunkZ,
			(bx, bz) -> this.delegate.getBaseHeight(bx, bz, Heightmap.Types.WORLD_SURFACE, level, randomState));
	}

	public boolean chunkIntersectsLake(ServerLevel level, int chunkX, int chunkZ) {
		long seed = level.getChunkSource().randomState().seed();
		return UnderworldPostProcess.chunkIntersectsLake(seed, chunkX << 4, chunkZ << 4);
	}

	public int siteBaseHeight(LevelHeightAccessor heightAccessor, RandomState randomState, int x, int z) {
		return this.delegate.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE, heightAccessor, randomState);
	}
}
