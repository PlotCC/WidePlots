package games.fatboychummy.wideplots.debug;

import games.fatboychummy.wideplots.world.PlotDimension;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * A bunch of debug commands for WidePlots.
 */
public class DebugCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Displays current dimension and generator.
            dispatcher.register(Commands.literal("plotdebug")
                .executes(context -> {
                    var source = context.getSource();
                    var level = source.getLevel();
                    String dimName = level.dimension().location().toString();
                    String generatorName = level.getChunkSource().getGenerator().getClass().getSimpleName();

                    source.sendSuccess(() -> Component.literal(
                        String.format("Current Dimension: %s | Generator: %s", dimName, generatorName)
                    ), false);

                    if (PlotDimension.PLOTDIM_LEVEL != null) {
                        String plotDimName = PlotDimension.PLOTDIM_LEVEL.dimension().location().toString();
                        String plotGen = PlotDimension.PLOTDIM_LEVEL.getChunkSource().getGenerator().getClass().getSimpleName();
                        BlockPos spawnPos = PlotDimension.PLOTDIM_LEVEL.getSharedSpawnPos();

                        source.sendSuccess(() -> Component.literal(
                            String.format("Plot Dimension: %s | Generator: %s", plotDimName, plotGen)
                        ), false);
                        source.sendSuccess(() -> Component.literal(
                            String.format("Plot Spawn Pos: %s", spawnPos)
                        ), false);
                    } else {
                        source.sendSuccess(() -> Component.literal(
                            "Plot dimension is NULL!"
                        ), false);
                    }

                    return 1;
                })
            );

            // Teleports a player to the plot dimension.
            dispatcher.register(Commands.literal("plottp")
                .executes(context -> {
                    var source = context.getSource();
                    var player = source.getPlayerOrException();

                    if (PlotDimension.PLOTDIM_LEVEL == null) {
                        source.sendFailure(Component.literal("Plot dimension is not loaded!"));
                        return 0;
                    }

                    player.teleportTo(PlotDimension.PLOTDIM_LEVEL, 0.5, 65.0, 0.5, player.getYRot(), player.getXRot());
                    source.sendSuccess(() -> Component.literal("Teleported to plot dimension!"), false);
                    return 1;
                })
            );
        });
    }
}

