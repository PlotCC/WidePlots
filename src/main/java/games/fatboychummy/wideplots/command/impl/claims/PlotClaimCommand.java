package games.fatboychummy.wideplots.command.impl.claims;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import games.fatboychummy.wideplots.world.plot.storage.SoftErrorState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PlotClaimCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        ServerPlayer player = context.getSource().getPlayer();
        SoftErrorState success = PlotStorageHandler.claimPlot(player);

        if (success.isError()) {
            CommandUtil.respondFailure(context, success.getMessage());
            return 0;
        }

        CommandUtil.respondSuccess(context, "Plot claimed successfully!");
        return 1;
    }
}
