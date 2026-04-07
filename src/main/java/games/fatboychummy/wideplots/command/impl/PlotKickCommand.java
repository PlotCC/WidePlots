package games.fatboychummy.wideplots.command.impl;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.util.PlotUtility;
import games.fatboychummy.wideplots.world.PlotDimension;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class PlotKickCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        ServerPlayer player = CommandUtil.requirePlayer(context);
        ServerPlayer target = context.getArgument("player", ServerPlayer.class);

        // Get the current player's plot.
        PlotStorage plot = PlotStorageHandler.getPlot(player);

        // Check what plot the target player is actually in.
        PlotStorage targetPlot = PlotStorageHandler.getPlot(target);
        BlockPos targetPos = target.blockPosition();
        if (plot.equals(targetPlot) && PlotUtility.isActuallyInBounds(targetPos)) {
            // Send the target player to the plot dimension spawn.
            target.teleportTo(PlotDimension.PLOTDIM_LEVEL, PlotDimension.PLOTDIM_SPAWN.getX(), PlotDimension.PLOTDIM_SPAWN.getY(), PlotDimension.PLOTDIM_SPAWN.getZ(), target.getYRot(), target.getXRot());
            CommandUtil.translatableSuccess(
                    context,
                    "commands.wideplots.response.kick.kicked",
                    target.getName().getString()
            );
            return 1;
        }

        // If this is being called from `/plot ban`, don't send a failure message since the player is being banned anyway.
        // Otherwise, send it.
        if (context.getNodes().stream().noneMatch(node -> node.getNode().getName().equals("ban"))) {
            CommandUtil.translatableFailure(
                    context,
                    "commands.wideplots.response.kick.not_in_plot"
            );
            return 0;
        }
        return 1;
    }
}
