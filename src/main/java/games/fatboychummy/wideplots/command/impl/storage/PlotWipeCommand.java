package games.fatboychummy.wideplots.command.impl.storage;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.util.TimedRequest;
import games.fatboychummy.wideplots.util.TimedRequestHelper;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PlotWipeCommand {
    private static final int WIPE_RESEND_TICKS = 20 * 10; // 10 seconds in ticks.
    private static final String COMMAND_NAME = "wipe";

    public static void init() {
        TimedRequestHelper.registerTimedRequestCommand(COMMAND_NAME);
    }

    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        ServerPlayer player = context.getSource().getPlayer();
        PlotStorage plot = PlotStorageHandler.getPlot(player);
        String playerUUID = player.getStringUUID();

        if (TimedRequestHelper.isAlive(COMMAND_NAME, playerUUID)) {
            CommandUtil.respondSuccess(context, "Plot wipe has been queued.");
            TimedRequestHelper.cancel(COMMAND_NAME, playerUUID);
            plot.wipe();
        } else {
            CommandUtil.respondSuccess(context, "Are you sure you want to wipe your plot? If so, please resend the command within the next " + (WIPE_RESEND_TICKS / 20) + " seconds to confirm.");
            TimedRequestHelper.timedRequest(
                    COMMAND_NAME,
                    playerUUID,
                    new TimedRequest(WIPE_RESEND_TICKS, () -> CommandUtil.respondFailure(context, "Plot wipe request expired."))
            );
        }

        return 1;
    }
}
