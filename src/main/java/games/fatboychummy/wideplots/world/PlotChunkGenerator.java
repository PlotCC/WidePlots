package games.fatboychummy.wideplots.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.world.structures.PlotStructures;
import games.fatboychummy.wideplots.world.structures.RoadStructure;
import games.fatboychummy.wideplots.world.structures.RoadStructureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Custom chunk generator that generates a world of plots separated by roads.
 */
public class PlotChunkGenerator extends ChunkGenerator {
    public static final MapCodec<PlotChunkGenerator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource)
        ).apply(instance, PlotChunkGenerator::new)
    );

    public static final Codec<PlotChunkGenerator> CODEC = MAP_CODEC.codec();

    // Plot configuration - TODO: Make these configurable
    // Note on the todo: Make based on the structure sizes?
    private static final int PLOT_SIZE = 48; // Size of each plot in blocks
    private static final int ROAD_WIDTH = 8; // Width of roads between plots
    private static final int GROUND_LEVEL = 64; // Y level of the plot floor
    private static final String ROAD_TYPE = "debug"; // Default road type for structure selection

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
        final int cell = PLOT_SIZE + ROAD_WIDTH;

        final int chunkMinX = chunk.getPos().getMinBlockX();
        final int chunkMinZ = chunk.getPos().getMinBlockZ();
        final int chunkMaxX = chunk.getPos().getMaxBlockX();
        final int chunkMaxZ = chunk.getPos().getMaxBlockZ();

        // Find first grid origin that could be inside this chunk.
        int startOriginX = firstGridAtOrAfter(chunkMinX, cell);
        int startOriginZ = firstGridAtOrAfter(chunkMinZ, cell);

        // Rule out chunks that are parts of a player's plot.
        // We can do this by checking if the chunk is horizontal to any grid origins.
        // If they chunk is horizontal to any grid origins, then it's not part of a plot.
        // If there is no horizontal connections to a grid origin, then this is a plot chunk.
        boolean isHorizontalToOriginX = startOriginX >= chunkMinX && startOriginX <= chunkMaxX;
        boolean isHorizontalToOriginZ = chunkMinZ < startOriginZ && chunkMaxZ > startOriginZ;
        BlockPos debugPos = new BlockPos(chunkMinX + 8, GROUND_LEVEL + 10, chunkMinZ + 8);
        if (!isHorizontalToOriginX && !isHorizontalToOriginZ) {
            // Debug: Place a red wool block in the center of the chunk at ground_level+10
            chunk.setBlockState(debugPos, Blocks.RED_WOOL.defaultBlockState(), false);
            return; // This chunk is not part of a road, so skip structure placement.
        }
        // Debug: Place a green wool block in the center of the chunk at ground_level+10 if it's horizontal to an origin
        chunk.setBlockState(debugPos, Blocks.GREEN_WOOL.defaultBlockState(), false);

        for (int originX = startOriginX; originX <= chunkMaxX; originX += cell) {
            for (int originZ = startOriginZ; originZ <= chunkMaxZ; originZ += cell) {
                // Place the two roads that intersect at this grid origin.
                if (isHorizontalToOriginX)
                    placeRoadSetAtOrigin(level, originX, originZ, true);
                if (isHorizontalToOriginZ)
                    placeRoadSetAtOrigin(level, originX, originZ, false);

                if (isHorizontalToOriginX && isHorizontalToOriginZ) {
                    // Place a four-way intersection at the grid origin if it's within this chunk
                    placeFourWay(level, originX, originZ);
                }
            }
        }
    }

    /**
     * Find the first grid origin at or after the given value.
     * @param value The value to find the grid origin for (e.g., chunkMinX or chunkMinZ)
     * @param cell The size of each cell (plot + road width)
     * @return The first grid origin at or after the given value.
     */
    private int firstGridAtOrAfter(int value, int cell) {
        int base = Math.floorDiv(value, cell) * cell;
        return (base < value) ? base + cell : base;
    }

    /**
     * Place a four-way intersection structure at the given grid origin.
     * @param level level from buildSurface
     * @param originX X coordinate of the grid origin (intersection point)
     * @param originZ Z coordinate of the grid origin (intersection point)
     */
    private void placeFourWay(WorldGenRegion level, int originX, int originZ) {
        RoadStructure intersection = PlotStructures.ROAD_STRUCTURE_MANAGER.selectStructure("four_way", ROAD_TYPE);
        if (intersection == null) {
            WidePlots.LOGGER.warn("No four-way intersection structure available for placement at {},{}", originX, originZ);
            return;
        }

        BlockPos intersectionPos = new BlockPos(originX, GROUND_LEVEL, originZ);
        placeRoadStructure(
                level,
                normalizeTemplateId(intersection.getStructureId()),
                intersectionPos,
                Rotation.NONE
        );
    }

    /**
     * Place a straight road structure along either the X or Z axis at the given grid origin.
     * @param level level from buildSurface
     * @param originX X coordinate of the grid origin (intersection point)
     * @param originZ Z coordinate of the grid origin (intersection point)
     * @param horizontalToOriginX true if the road should be placed along the X axis, false for Z axis
     */
    private void placeRoadSetAtOrigin(WorldGenRegion level, int originX, int originZ, boolean horizontalToOriginX) {
        RoadStructure intersection = PlotStructures.ROAD_STRUCTURE_MANAGER.selectStructure("four_way", ROAD_TYPE);
        RoadStructure straight = PlotStructures.ROAD_STRUCTURE_MANAGER.selectStructure("straight", ROAD_TYPE);

        if (intersection == null || straight == null) {
            WidePlots.LOGGER.warn("No road structures available for placement at {},{}", originX, originZ);
            return;
        }

        if (horizontalToOriginX) {
            // 1: Straight road along +X axis
            BlockPos straightXPos = new BlockPos(originX + ROAD_WIDTH, GROUND_LEVEL, originZ);
            placeRoadStructure(
                    level,
                    normalizeTemplateId(straight.getStructureId()),
                    straightXPos,
                    Rotation.NONE
            );
        } else {
            // 2: Straight road along +Z axis
            BlockPos straightZPos = new BlockPos(originX, GROUND_LEVEL, originZ + ROAD_WIDTH);
            placeRoadStructure(
                    level,
                    normalizeTemplateId(straight.getStructureId()),
                    straightZPos,
                    Rotation.CLOCKWISE_90
            );
        }
    }


    /**
     * Check if the given world coordinates are part of a road segment and place the appropriate road structure if so.
     * NOTE: Did not work as intended, left for future me reference.
     * @param level level from buildSurface
     * @param structureManager structure manager from buildSurface
     * @param worldX X coordinate of the block to check
     * @param worldZ Z coordinate of the block to check
     */
    public void checkAndPlaceRoadStructure(WorldGenRegion level, StructureManager structureManager, int worldX, int worldZ) {
        int plotX = Math.floorMod(worldX, PLOT_SIZE + ROAD_WIDTH);
        int plotZ = Math.floorMod(worldZ, PLOT_SIZE + ROAD_WIDTH);
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;

        boolean isRoadX = plotX < ROAD_WIDTH;
        boolean isRoadZ = plotZ < ROAD_WIDTH;

        if (!isRoadX && !isRoadZ) {
            return; // Not a road column
        }

        if (isRoadX && chunkZ == 0) {
            // Place straight road along X axis
            RoadStructure straightX = PlotStructures.ROAD_STRUCTURE_MANAGER.selectStructure("straight", ROAD_TYPE);
            BlockPos straightXPos = new BlockPos(worldX, GROUND_LEVEL, worldZ);
            placeRoadStructure(
                    level,
                    normalizeTemplateId(straightX.getStructureId()),
                    straightXPos,
                    Rotation.NONE
            );
        }

        if (isRoadZ && chunkX == 0) {
            // Place straight road along Z axis
            RoadStructure straightZ = PlotStructures.ROAD_STRUCTURE_MANAGER.selectStructure("straight", ROAD_TYPE);
            BlockPos straightZPos = new BlockPos(worldX, GROUND_LEVEL, worldZ);
            placeRoadStructure(
                    level,
                    normalizeTemplateId(straightZ.getStructureId()),
                    straightZPos,
                    Rotation.CLOCKWISE_90
            );
        }

        if (plotX == 0 && plotZ == 0) {
            // Place four-way intersection at the origin of the road segments
            RoadStructure intersection = PlotStructures.ROAD_STRUCTURE_MANAGER.selectStructure("four_way", ROAD_TYPE);
            BlockPos intersectionPos = new BlockPos(worldX, GROUND_LEVEL, worldZ);
            placeRoadStructure(
                    level,
                    normalizeTemplateId(intersection.getStructureId()),
                    intersectionPos,
                    Rotation.NONE
            );
        }
    }

    /**
     * Place a road structure in the world at the given position with the specified rotation.
     * @param level level from buildSurface
     * @param normalizedId normalized resource location of the structure template to place
     * @param pos world position to place the structure at (should be the bottom north-west corner of the structure)
     * @param rotation rotation to apply to the structure when placing
     */
    public void placeRoadStructure(WorldGenRegion level, ResourceLocation normalizedId, BlockPos pos, Rotation rotation) {
        StructureTemplateManager templateManager = level.getServer().getStructureManager();
        StructureTemplate template = templateManager.getOrCreate(normalizedId);

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation);

        WidePlots.LOGGER.info("Placing road structure '{}' at {},{} with rotation {}", normalizedId, pos.getX(), pos.getZ(), settings.getRotation());
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
        } else {
            WidePlots.LOGGER.info("Successfully placed road structure '{}' at {},{}", normalizedId, pos.getX(), pos.getZ());
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

    /**
     * Generate a column of blocks for the given local chunk coordinates and world coordinates.
     * @param chunk the chunk to modify
     * @param localX local X coordinate within the chunk (0-15)
     * @param localZ local Z coordinate within the chunk (0-15)
     * @param worldX world X coordinate of the block being generated
     * @param worldZ world Z coordinate of the block being generated
     */
    private void generateColumn(ChunkAccess chunk, int localX, int localZ, int worldX, int worldZ) {
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
     * Determine the shape of the road segment at the given world coordinates, if any.
     * Not used in current implementation.
     * @param worldX X coordinate of the block to check
     * @param worldZ Z coordinate of the block to check
     * @return the shape of the road segment ("straight_x", "straight_z", "four_way") or null if not a road block
     */
    private String getRoadShape(int worldX, int worldZ) {
        int plotX = Math.floorMod(worldX, PLOT_SIZE + ROAD_WIDTH);
        int plotZ = Math.floorMod(worldZ, PLOT_SIZE + ROAD_WIDTH);

        boolean isRoadX = plotX < ROAD_WIDTH;
        boolean isRoadZ = plotZ < ROAD_WIDTH;

        if (isRoadX && isRoadZ) {
            return "four_way";
        } else if (isRoadX) {
            return "straight_x";
        } else if (isRoadZ) {
            return "straight_z";
        }

        return null;
    }

    /**
     * Determine the appropriate rotation for a road structure based on the shape of the road segment at the given world coordinates.
     * Not used in current implementation.
     * @param worldX X coordinate of the block to check
     * @param worldZ Z coordinate of the block to check
     * @return the rotation to apply to the road structure when placing
     */
    private Rotation getRotationForRoad(int worldX, int worldZ) {
        String shape = getRoadShape(worldX, worldZ);
        if (shape == null) {
            return Rotation.NONE;
        }

        return switch (shape) {
            case "straight_x" -> Rotation.COUNTERCLOCKWISE_90;
            case "straight_z" -> Rotation.NONE; // No rotation needed for straight roads along Z axis
            case "four_way" -> Rotation.NONE; // No rotation needed for four-way junctions
            default -> Rotation.NONE;
        };
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

