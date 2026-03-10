package games.fatboychummy.wideplots.command.impl.claims;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PlotForceUnclaimCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        // Check that the caller is a player, that they're in the plot dimension, and that they have permission level 4.
        if (CommandUtil.shouldBlock(context, PermissionLevel.ADMIN)) {return 0;}

        ServerPlayer player = context.getSource().getPlayer();

        PlotStorageHandler.forceUnclaimPlot(player.getBlockX(), player.getBlockZ()); // baseChecks ensures `player` is not null.
        CommandUtil.respondSuccess(context, "Plot force-unclaimed!");
        return 1;
    }
}
