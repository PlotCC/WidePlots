package games.fatboychummy.wideplots.client;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.command.arguments.PlotActionTypeArgument;
import games.fatboychummy.wideplots.command.arguments.PlotPermissionArgument;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class WidePlotsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerArgumentTypes();
    }

    private void registerArgumentTypes() {
        WidePlots.LOGGER.info("Registering client-side argument types...");
        ArgumentTypeRegistry.registerArgumentType(
                WidePlots.id("plot_action"),
                PlotActionTypeArgument.class,
                SingletonArgumentInfo.contextFree(PlotActionTypeArgument::action)
        );
        ArgumentTypeRegistry.registerArgumentType(
                WidePlots.id("plot_permission"),
                PlotPermissionArgument.class,
                SingletonArgumentInfo.contextFree(PlotPermissionArgument::action)
        );
    }
}
