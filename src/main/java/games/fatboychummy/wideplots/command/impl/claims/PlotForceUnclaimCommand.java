package games.fatboychummy.wideplots.command.impl.claims;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.util.PlotUtility;
import games.fatboychummy.wideplots.util.TimedRequest;
import games.fatboychummy.wideplots.util.TimedRequestHelper;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PlotForceUnclaimCommand {
    private static final String COMMAND_NAME = "force_unclaim";
    private static final int COMMAND_TIMEOUT = 20 * 10; // 10 seconds in ticks.

    public static int execute(CommandContext<CommandSourceStack> context) {
        // Check that the caller is a player, that they're in the plot dimension, and that they have admin permissions.
        if (CommandUtil.shouldBlock(context, PermissionLevel.ADMIN)) {return 0;}

        ServerPlayer player = CommandUtil.requirePlayer(context);
        String playerUUID = player.getStringUUID();

        if (!PlotUtility.isActuallyInBounds(player.getOnPos())) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.generic.not_in_plot");
            return 0;
        }

        if (TimedRequestHelper.isAlive(COMMAND_NAME, playerUUID)) {
            PlotStorageHandler.forceUnclaimPlot(player.getBlockX(), player.getBlockZ());
            CommandUtil.translatableSuccess(context, "commands.wideplots.response.force_unclaim.success");
        } else {
            CommandUtil.translatableSuccess(context, "commands.wideplots.response.generic.resend_can_wipe", COMMAND_TIMEOUT / 20);
            TimedRequestHelper.timedRequest(
                    COMMAND_NAME,
                    playerUUID,
                    new TimedRequest(
                            COMMAND_TIMEOUT,
                            () -> CommandUtil.translatableFailure(context, "commands.wideplots.response.force_unclaim.timed_out")
                    )
            );
        }

        return 1;
    }
}
