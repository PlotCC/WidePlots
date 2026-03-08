package games.fatboychummy.wideplots;

import com.mojang.logging.LogUtils;
import games.fatboychummy.wideplots.debug.DebugCommands;
import games.fatboychummy.wideplots.world.PlotDimension;
import games.fatboychummy.wideplots.world.player.PlotPlayerYeeter;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionHandler;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissions;
import games.fatboychummy.wideplots.world.structures.PlotStructures;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class WidePlots implements ModInitializer {
    public static final String MOD_ID = "wideplots";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String id) {
        return new ResourceLocation(MOD_ID, id);
    }


    @Override
    public void onInitialize() {
        LOGGER.info("Initializing WidePlots");

        // Initialize the plot dimension and player teleportation handler first.
        PlotDimension.init();
        PlotPlayerYeeter.init();

        // Load structures and some debug commands.
        PlotStructures.init();
        DebugCommands.register();

        // Permission initialization.
        PlotPermissions.init();
        PlotPermissionHandler.init();

        LOGGER.info("WidePlots initialized successfully");
    }
}
