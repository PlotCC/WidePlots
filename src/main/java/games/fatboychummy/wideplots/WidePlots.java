package games.fatboychummy.wideplots;

import com.mojang.logging.LogUtils;
import games.fatboychummy.wideplots.debug.DebugCommands;
import games.fatboychummy.wideplots.world.PlotDimension;
import games.fatboychummy.wideplots.world.PlotPlayerHandler;
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

        PlotDimension.init();
        PlotPlayerHandler.init();
        PlotStructures.init();
        DebugCommands.register();

        LOGGER.info("WidePlots initialized successfully");
    }
}
