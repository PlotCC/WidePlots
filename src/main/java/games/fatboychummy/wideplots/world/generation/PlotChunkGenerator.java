package games.fatboychummy.wideplots.world.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.world.structures.PlotStructures;
import games.fatboychummy.wideplots.world.structures.RoadStructure;
import games.fatboychummy.wideplots.world.structures.RoadStructureManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

record Square(int minX, int minZ, int maxX, int maxZ) {}

/**
 * Custom chunk generator that generates a world of plots separated by roads.
 */
public class PlotChunkGenerator extends ChunkGenerator {
    public static void init() {
        // Register the regenerator tick method.
        ServerTickEvents.START_SERVER_TICK.register(PlotChunkGenerator::tickRegenerator);
    }

    private static final int ticksPerRegen = 5;
    private static int tickCounter = 0;
    public static void tickRegenerator(MinecraftServer server) {
        if (tickCounter < ticksPerRegen) {
            tickCounter++;
            return;
        }
        tickCounter = 0;

        if (server.isRunning()) {
            ChunkRegenData data = ChunkRegenQueue.next();
            if (data != null) {
                ChunkAccess chunk = server.overworld().getChunk(data.chunkX(), data.chunkZ(), ChunkStatus.FULL);

                if (chunk != null) {
                    regenerateChunk(chunk, data.minBlockX(), data.minBlockZ(), data.maxBlockX(), data.maxBlockZ());
                } else {
                    WidePlots.LOGGER.warn("Failed to regenerate chunk at {},{}: chunk not found", data.chunkX(), data.chunkZ());
                }
            }
        }
    }

    public static final MapCodec<PlotChunkGenerator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource)
        ).apply(instance, PlotChunkGenerator::new)
    );

    public static final Codec<PlotChunkGenerator> CODEC = MAP_CODEC.codec();

    // Plot configuration - TODO: Make these configurable
    // Note on the todo: Make based on the structure sizes?
    public static final int PLOT_SIZE = 48; // Size of each plot in blocks
    public static final int ROAD_WIDTH = 8; // Width of roads between plots
    private static final int GROUND_LEVEL = 64; // Y level of the plot floor
    private static final String ROAD_TYPE = "basic"; // Default road type for structure selection

    private final BlockState[] baseColumn = new BlockState[384];
    private boolean baseColumnInitialized = false;

    private final RandomSource structureRandom;

    public PlotChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
        this.structureRandom = RandomSource.create();
    }

    @Override
    @NotNull
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /**
     * Get the road structure manager for this generator.
     */
    public RoadStructureManager getRoadStructureManager() {
        return PlotStructures.ROAD_STRUCTURE_MANAGER;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        // No carvers needed for plot world
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        // We can divide the world into three road grids.
        //   1. 4-way grid. Where the roads all intersect.
        //   2. X-axis grid. Where roads run along the X axis.
        //   3. Z-axis grid. Where roads run along the Z axis.
        // Each grid is based on the same origin points, with the 4-way grid being at 0,0, then the X and Z grids being offset by ROAD_WIDTH in their respective directions.
        // (Possibly also some additional offset depending on how rotations will need be set up).
        // We always generate the roads in the positive X and Z directions from their origin points, so we can check
        // if a chunk is part of a road by checking if it's horizontal (backwards) to any of the grids.
        // Then, we position the road based on the closest grid point (again, backwards from the current position).

        // First, find the closest grid origins for each of the three grids. We can do this by finding the first grid
        // origin that is at or after the minimum X and Z coordinates of the chunk. Then, subtracting one cell size to
        // get the closest grid origin that is before the chunk.

        Square chunkSquare = new Square(
                chunk.getPos().getMinBlockX(),
                chunk.getPos().getMinBlockZ(),
                chunk.getPos().getMaxBlockX(),
                chunk.getPos().getMaxBlockZ()
        );
        BlockPos intersectionPos = getGridPositionInChunk(chunkSquare);

        // Check: Is this chunk part of a road at all?
        // Check if there is a grid origin to the negative X and Z.
        int cell = PLOT_SIZE + ROAD_WIDTH;

        // A grid origin can be within this chunk AND a road leading to it.
        // Thus, we need to compute both the min and max.
        int nearestGridXMin = Math.floorDiv(chunkSquare.minX(), cell) * cell;
        int nearestGridZMin = Math.floorDiv(chunkSquare.minZ(), cell) * cell;
        int nearestGridXMax = Math.floorDiv(chunkSquare.maxX(), cell) * cell;
        int nearestGridZMax = Math.floorDiv(chunkSquare.maxZ(), cell) * cell;

        boolean hasGridXMin =
                nearestGridXMin <= chunkSquare.maxX() &&
                nearestGridXMin >= chunkSquare.minX() - (cell + 15);
        boolean hasGridZMin =
                nearestGridZMin <= chunkSquare.maxZ() &&
                nearestGridZMin >= chunkSquare.minZ() - (cell + 15);
        boolean hasGridXMax =
                nearestGridXMax <= chunkSquare.maxX() &&
                nearestGridXMax >= chunkSquare.minX() - (cell + 15);
        boolean hasGridZMax =
                nearestGridZMax <= chunkSquare.maxZ() &&
                nearestGridZMax >= chunkSquare.minZ() - (cell + 15);

        if (!hasGridXMax && !hasGridZMax && !hasGridXMin && !hasGridZMin) {
            // No grid origins nearby, so this chunk is purely plot and we can skip road generation.
            return;
        }

        if (hasGridXMin) {
            placeRoadSetAtOrigin(
                    level,
                    chunkSquare,
                    nearestGridXMin,
                    nearestGridZMin,
                    true
            );
        }

        if (hasGridZMin) {
            placeRoadSetAtOrigin(
                    level,
                    chunkSquare,
                    nearestGridXMin,
                    nearestGridZMin,
                    false
            );
        }

        if (hasGridXMax) {
            placeRoadSetAtOrigin(
                    level,
                    chunkSquare,
                    nearestGridXMax,
                    nearestGridZMax,
                    true
            );
        }

        if (hasGridZMax) {
            placeRoadSetAtOrigin(
                    level,
                    chunkSquare,
                    nearestGridXMax,
                    nearestGridZMax,
                    false
            );
        }

        if (intersectionPos == null) {
            return; // No direct intersection to handle.
        }

        // Place an intersection at the grid origin if it's within this chunk
        // TODO: What happens if the intersection is part-way into this chunk?
        placeFourWay(level, chunkSquare, intersectionPos.getX(), intersectionPos.getZ());

        // Place roads beside, if the road placements would still be within this chunk.
        BlockPos straightXPos = new BlockPos(intersectionPos.getX() + ROAD_WIDTH, GROUND_LEVEL, intersectionPos.getZ());
        if (straightXPos.getX() <= chunk.getPos().getMaxBlockX() && straightXPos.getX() >= chunk.getPos().getMinBlockX()) {
            placeRoadSetAtOrigin(
                    level,
                    chunkSquare,
                    intersectionPos.getX(),
                    intersectionPos.getZ(),
                    true
            );
        }

        BlockPos straightZPos = new BlockPos(intersectionPos.getX(), GROUND_LEVEL, intersectionPos.getZ() + ROAD_WIDTH);
        if (straightZPos.getZ() <= chunk.getPos().getMaxBlockZ() && straightZPos.getZ() >= chunk.getPos().getMinBlockZ()) {
            placeRoadSetAtOrigin(
                    level,
                    chunkSquare,
                    intersectionPos.getX(),
                    intersectionPos.getZ(),
                    false
            );
        }

        // There was an intersection in this chunk, which means our `max` check will only detect this
        // intersection, but a road *might* be leading into this chunk too, so we need to check from
        // (intersectionX - 1, maxZ and (maxX, intersectionZ - 1).
        BlockPos roadXPos = new BlockPos(
                intersectionPos.getX() - 1,
                GROUND_LEVEL,
                intersectionPos.getZ()
        );
        BlockPos roadZPos = new BlockPos(
                intersectionPos.getX(),
                GROUND_LEVEL,
                intersectionPos.getZ() - 1
        );

        int nearestGridX = Math.floorDiv(roadXPos.getX(), cell) * cell;
        int nearestGridZ = Math.floorDiv(roadZPos.getZ(), cell) * cell;

        boolean hasRoadX =
                nearestGridX <= chunkSquare.maxX() &&
                nearestGridX >= chunkSquare.minX() - (cell + 15);
        boolean hasRoadZ =
                nearestGridZ <= chunkSquare.maxZ() &&
                nearestGridZ >= chunkSquare.minZ() - (cell + 15);

        if (hasRoadX) {
            placeRoadSetAtOrigin(
                    level,
                    chunkSquare,
                    nearestGridX,
                    intersectionPos.getZ(),
                    true
            );
        }

        if (hasRoadZ) {
            placeRoadSetAtOrigin(
                    level,
                    chunkSquare,
                    intersectionPos.getX(),
                    nearestGridZ,
                    false
            );
        }
    }

    /**
     * Gets the grid position that is inside the given chunk, if any.
     * @param chunkSquare the square area of the chunk for bounding.
     * @return the BlockPos of the grid position inside the chunk, or null if there is no grid position inside the chunk
     */
    private BlockPos getGridPositionInChunk(Square chunkSquare) {
        final int cell = PLOT_SIZE + ROAD_WIDTH;

        final int chunkMinX = chunkSquare.minX();
        final int chunkMinZ = chunkSquare.minZ();
        final int chunkMaxX = chunkSquare.maxX();
        final int chunkMaxZ = chunkSquare.maxZ();

        // Find first grid origin that could be inside this chunk.
        int minXi = -Math.floorDiv(-chunkMinX, cell); // Equivalent to ceil(chunkMinX / cell)
        int minZi = -Math.floorDiv(-chunkMinZ, cell); // Equivalent to ceil(chunkMinZ / cell)
        int maxXi = Math.floorDiv(chunkMaxX, cell); // Equivalent to floor(chunkMaxX / cell)
        int maxZi = Math.floorDiv(chunkMaxZ, cell); // Equivalent to floor(chunkMaxZ / cell)

        if (minXi > maxXi || minZi > maxZi) {
            // No grid origin inside this chunk
            return null;
        }

        // There is at least one grid origin inside this chunk. We can just return the first one.
        // Hopefully people aren't making plots smaller than a chunk with roads also small enough to fit two origins...
        int gridX = minXi * cell;
        int gridZ = minZi * cell;
        return new BlockPos(gridX, GROUND_LEVEL, gridZ);
    }

    /**
     * Place a four-way intersection structure at the given grid origin.
     * @param level level from buildSurface
     * @param chunkSquare the square area of the chunk for bounding.
     * @param originX X coordinate of the grid origin (intersection point)
     * @param originZ Z coordinate of the grid origin (intersection point)
     */
    private void placeFourWay(WorldGenRegion level, Square chunkSquare, int originX, int originZ) {
        PlotStructures.ROAD_STRUCTURE_MANAGER.randomSeed(originX, originZ, true); // facingX doesn't matter here, other than it staying constant.
        RoadStructure intersection = PlotStructures.ROAD_STRUCTURE_MANAGER.selectStructure("four_way", ROAD_TYPE);
        if (intersection == null) {
            WidePlots.LOGGER.warn("No four-way intersection structure available for placement at {},{}", originX, originZ);
            return;
        }

        BlockPos intersectionPos = new BlockPos(
                originX,
                GROUND_LEVEL - intersection.getDepth(),
                originZ
        );
        placeRoadStructure(
                level,
                chunkSquare,
                normalizeTemplateId(intersection.getStructureId()),
                intersectionPos,
                Rotation.NONE
        );
    }

    /**
     * Place a straight road structure along either the X or Z axis at the given grid origin.
     * @param level level from buildSurface
     * @param chunkSquare the square area of the chunk for bounding.
     * @param originX X coordinate of the grid origin (intersection point)
     * @param originZ Z coordinate of the grid origin (intersection point)
     * @param horizontalToOriginX true if the road should be placed along the X axis, false for Z axis
     */
    private void placeRoadSetAtOrigin(WorldGenRegion level, Square chunkSquare, int originX, int originZ, boolean horizontalToOriginX) {
        PlotStructures.ROAD_STRUCTURE_MANAGER.randomSeed(originX, originZ, horizontalToOriginX);
        RoadStructure straight = PlotStructures.ROAD_STRUCTURE_MANAGER.selectStructure("straight", ROAD_TYPE);

        if (straight == null) {
            WidePlots.LOGGER.warn("No road structures available for placement at {},{}", originX, originZ);
            return;
        }

        if (horizontalToOriginX) {
            // 1: Straight road along +X axis
            BlockPos straightXPos = new BlockPos(
                    originX + ROAD_WIDTH, // Due to intersection positioning.
                    GROUND_LEVEL - straight.getDepth(),
                    originZ
            );
            placeRoadStructure(
                    level,
                    chunkSquare,
                    normalizeTemplateId(straight.getStructureId()),
                    straightXPos,
                    Rotation.NONE
            );
        } else {
            // 2: Straight road along +Z axis
            BlockPos straightZPos = new BlockPos(
                    originX + ROAD_WIDTH - 1, // Due to rotation
                    GROUND_LEVEL - straight.getDepth(),
                    originZ + ROAD_WIDTH // Due to intersection positioning.
            );
            placeRoadStructure(
                    level,
                    chunkSquare,
                    normalizeTemplateId(straight.getStructureId()),
                    straightZPos,
                    Rotation.CLOCKWISE_90
            );
        }
    }

    /**
     * Place a road structure in the world at the given position with the specified rotation.
     * @param level level from buildSurface
     * @param chunkSquare the square area of the chunk for bounding.
     * @param normalizedId normalized resource location of the structure template to place
     * @param pos world position to place the structure at (should be the bottom north-west corner of the structure)
     * @param rotation rotation to apply to the structure when placing
     */
    private void placeRoadStructure(WorldGenRegion level, Square chunkSquare, ResourceLocation normalizedId, BlockPos pos, Rotation rotation) {
        StructureTemplateManager templateManager = level.getServer().getStructureManager();
        StructureTemplate template = templateManager.getOrCreate(normalizedId);

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setBoundingBox(getChunkBoundingBox(chunkSquare));


        boolean success = template.placeInWorld(
                level,
                pos,
                pos,
                settings,
                structureRandom,
                Block.UPDATE_CLIENTS
        );
        if (!success) {
            WidePlots.LOGGER.warn("Failed to place road structure '{}' at {},{}", normalizedId, pos.getX(), pos.getZ());
        }

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        // No natural mob spawning in plot world
    }

    @Override
    public int getGenDepth() {
        return 384; // Total height of the world
    }


    @Override
    @NotNull
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunk.getPos().getMinBlockX() + x;
                int worldZ = chunk.getPos().getMinBlockZ() + z;
                generateColumn(chunk, x, z, worldX, worldZ);
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    public static void queueRegenerateChunk(int chunkX, int chunkZ, int minX, int minZ, int maxX, int maxZ) {
        ChunkRegenQueue.addToQueue(new ChunkRegenData(chunkX, chunkZ, minX, minZ, maxX, maxZ));
    }

    private static void regenerateChunk(ChunkAccess chunk, int minX, int minZ, int maxX, int maxZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunk.getPos().getMinBlockX() + x;
                int worldZ = chunk.getPos().getMinBlockZ() + z;

                if (worldX >= minX && worldX <= maxX && worldZ >= minZ && worldZ <= maxZ) {
                    generateColumn(chunk, x, z, worldX, worldZ);
                }
            }
        }
    }

    /**
     * Generate a column of blocks for the given local chunk coordinates and world coordinates.
     * @param chunk the chunk to modify
     * @param localX local X coordinate within the chunk (0-15)
     * @param localZ local Z coordinate within the chunk (0-15)
     * @param worldX world X coordinate of the block being generated
     * @param worldZ world Z coordinate of the block being generated
     */
    public static void generateColumn(ChunkAccess chunk, int localX, int localZ, int worldX, int worldZ) {
        int plotX = Math.floorMod(worldX, PLOT_SIZE + ROAD_WIDTH);
        int plotZ = Math.floorMod(worldZ, PLOT_SIZE + ROAD_WIDTH);
        boolean isRoad = plotX < ROAD_WIDTH || plotZ < ROAD_WIDTH;

        chunk.setBlockState(new BlockPos(localX, chunk.getMinBuildHeight(), localZ), Blocks.BEDROCK.defaultBlockState(), false);

        for (int y = chunk.getMinBuildHeight() + 1; y < GROUND_LEVEL - 1; y++) {
            chunk.setBlockState(new BlockPos(localX, y, localZ), Blocks.STONE.defaultBlockState(), false);
        }

        chunk.setBlockState(new BlockPos(localX, GROUND_LEVEL - 2, localZ), Blocks.DIRT.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(localX, GROUND_LEVEL - 1, localZ), Blocks.DIRT.defaultBlockState(), false);

        // Debug: Replace road blocks.
        if (isRoad) {
            chunk.setBlockState(new BlockPos(localX, GROUND_LEVEL, localZ), Blocks.STONE_BRICKS.defaultBlockState(), false);
        } else {
            chunk.setBlockState(new BlockPos(localX, GROUND_LEVEL, localZ), Blocks.GRASS_BLOCK.defaultBlockState(), false);
        }
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType, LevelHeightAccessor level, RandomState random) {
        return GROUND_LEVEL + 1; // All terrain is at ground level
    }

    @Override
    @NotNull
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        if (!baseColumnInitialized) {
            for (int y = 0; y < baseColumn.length; y++) {
                int worldY = y + level.getMinBuildHeight();

                if (worldY == level.getMinBuildHeight()) {
                    // Bedrock at bottom
                    baseColumn[y] = Blocks.BEDROCK.defaultBlockState();
                } else if (worldY < GROUND_LEVEL - 2) {
                    // Stone fill below surface
                    baseColumn[y] = Blocks.STONE.defaultBlockState();
                } else if (worldY < GROUND_LEVEL) {
                    // Dirt layer (2 blocks)
                    baseColumn[y] = Blocks.DIRT.defaultBlockState();
                } else if (worldY == GROUND_LEVEL) {
                    // Surface layer - grass for plots, will be replaced with structures for roads
                    baseColumn[y] = Blocks.GRASS_BLOCK.defaultBlockState();
                } else {
                    // Air above ground
                    baseColumn[y] = Blocks.AIR.defaultBlockState();
                }
            }
            baseColumnInitialized = true;
        }
        return new NoiseColumn(level.getMinBuildHeight(), baseColumn.clone());
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("##Plot World Generator##");
        int plotX = Math.floorMod(pos.getX(), PLOT_SIZE + ROAD_WIDTH);
        int plotZ = Math.floorMod(pos.getZ(), PLOT_SIZE + ROAD_WIDTH);
        boolean isRoad = plotX < ROAD_WIDTH || plotZ < ROAD_WIDTH;
        info.add("Is Road: " + isRoad);
        if (!isRoad) {
            info.add("Plot Position: " + (plotX - ROAD_WIDTH) + ", " + (plotZ - ROAD_WIDTH));
        } else {
            info.add("Plot Position: -, -");
        }
    }

    /**
     * Get a bounding box for the given chunk square.
     * @param chunkSquare the square area of the chunk for bounding.
     * @return a BoundingBox that covers the entire vertical space of the chunk and the horizontal area defined by the chunk square
     */
    private static BoundingBox getChunkBoundingBox(Square chunkSquare) {
        return new BoundingBox(
                chunkSquare.minX(),
                -64, // Min world height
                chunkSquare.minZ(),
                chunkSquare.maxX(),
                384, // Max world height
                chunkSquare.maxZ()
        );
    }

    /**
     * Normalize the resource location of a structure template by removing the "structures/" prefix and ".nbt" suffix if present.
     * @param resourceLocation the original resource location of the structure template (e.g., "modid:structures/roads/straight/basic.nbt")
     * @return the normalized resource location (e.g., "modid:roads/straight/basic")
     */
    private static ResourceLocation normalizeTemplateId(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();
        if (path.startsWith("structures/")) {
            path = path.substring("structures/".length());
        }
        if (path.endsWith(".nbt")) {
            path = path.substring(0, path.length() - ".nbt".length());
        }
        return new ResourceLocation(resourceLocation.getNamespace(), path);
    }
}

