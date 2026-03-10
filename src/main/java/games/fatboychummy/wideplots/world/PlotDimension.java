package games.fatboychummy.wideplots.world;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.world.generation.PlotChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.OptionalLong;

public class PlotDimension {
    public static final ResourceKey<Level> PLOTDIM = ResourceKey.create(Registries.DIMENSION, WidePlots.id("plotdim"));

    //TODO: find out if I need the stem
    public static final ResourceKey<LevelStem> PLOTDIM_STEM = ResourceKey.create(Registries.LEVEL_STEM, WidePlots.id("plotdim"));

    public static final ResourceKey<DimensionType> PLOTDIM_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, WidePlots.id("plotdim"));

    public static DimensionType PLOTDIM_TYPE;

    public static ServerLevel PLOTDIM_LEVEL;

    public static BlockPos PLOTDIM_SPAWN = new BlockPos(0, 65, 0);

    // Register chunk generator codec
    static {
        Registry.register(
            BuiltInRegistries.CHUNK_GENERATOR,
            WidePlots.id("plot_generator"),
            PlotChunkGenerator.CODEC
        );
    }


    public static void init() {
        WidePlots.LOGGER.info("Registering Mod Dimensions for " + WidePlots.MOD_ID);

        // Register server startup listener to load dimension instances
        // I do hope this is how this is supposed to be done.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PLOTDIM_TYPE = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).get(PLOTDIM_TYPE_KEY);
            PLOTDIM_LEVEL = server.getLevel(PLOTDIM);

            if (PLOTDIM_LEVEL == null) {
                WidePlots.LOGGER.error("Plot dimension failed to load! Make sure dimension JSON files are properly configured.");
            } else {
                WidePlots.LOGGER.info("Plot dimension loaded successfully!");

                // Set the spawn point for the dimension so players spawn in the right place
                PLOTDIM_LEVEL.setDefaultSpawnPos(new BlockPos(0, 65, 0), 0.0f);
                WidePlots.LOGGER.info("Set plot dimension spawn point to 0, 65, 0");
            }
        });
    }

    public static void bootstrap(BootstapContext<DimensionType> context) {
        // Create a dimension type that matches the Overworld exactly
        // This gives us: sky, sun, moon, daylight cycle, weather, etc.
        context.register(PLOTDIM_TYPE_KEY, new DimensionType(
            OptionalLong.empty(), // No fixed time - allows day/night cycle like Overworld
            true, // Has skylight - enables sun/moon/stars
            false, // Has ceiling - false like Overworld (true would be like Nether)
            false, // Ultrawarm - false like Overworld (true = Nether-like)
            true, // Natural - true like Overworld (affects some game mechanics)
            1.0, // Coordinate scale - 1.0 = normal (Nether uses 8.0)
            true, // Bed works - players can set spawn
            false, // Respawn anchor works - false like Overworld (true = Nether)
            -64, // Min Y - same as Overworld
            320, // Height - same as Overworld (min Y + height = max Y of 256)
            320, // Logical height - same as Overworld
            BlockTags.INFINIBURN_OVERWORLD, // Infiniburn block tag
            new ResourceLocation("minecraft:overworld"), // Use overworld effects (sky, fog, etc.)
            0.0f, // Ambient light - 0.0 = dark at night like Overworld
            new DimensionType.MonsterSettings(
                    true, // Piglin safe - true unlike Overworld. We don't want piglins spawning.
                    false, // Has raids - false, unlike Overworld. We don't want raids in the plot world.
                    UniformInt.of(0, 7), // Monster spawn light level - 0-7 like Overworld
                    0 // Monster spawn block light limit
            )
        ));
    }
}
