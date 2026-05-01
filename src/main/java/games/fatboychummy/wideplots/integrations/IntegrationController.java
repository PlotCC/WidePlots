package games.fatboychummy.wideplots.integrations;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.integrations.cc.tweaked.CCTweakedIntegration;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Map;

public class IntegrationController {
    private static final Map<Class<?>, String> integrations = Map.of(
            CCTweakedIntegration.class, "computercraft"
    );

    public static void initIntegrations() {
        for (Map.Entry<Class<?>, String> entry : integrations.entrySet()) {
            initIntegration(entry.getKey(), entry.getValue());
        }
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
