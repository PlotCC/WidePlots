package games.fatboychummy.wideplots.integrations;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.integrations.cc.tweaked.CcTweakedIntegration;
import net.fabricmc.loader.api.FabricLoader;

public class IntegrationController {
    public static void initIntegrations() {
        initIntegration(CcTweakedIntegration.class, "cc-tweaked");
    }

    public static void initIntegration(Class<?> c, String name) {
        try {
            if (FabricLoader.getInstance().isModLoaded(name)) {
                WidePlots.LOGGER.info("Loading integration for {}", name);
                c.getDeclaredMethod("init")
                        .invoke(null);
            }
        } catch (Exception ignored) {}
    }
}
