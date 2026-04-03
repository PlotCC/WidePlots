package games.fatboychummy.wideplots.command.impl.claims;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.util.TimedRequest;
import games.fatboychummy.wideplots.util.TimedRequestHelper;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import games.fatboychummy.wideplots.world.plot.storage.SoftErrorState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PlotUnclaimCommand {
    private static final String COMMAND_NAME = "unclaim";
    private static final int TIMEOUT = 20 * 10; // 10 seconds in ticks.

    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        ServerPlayer player = context.getSource().getPlayer();
        String playerUUID = player.getStringUUID();
        PlotStorage plot = PlotStorageHandler.getPlot(player);
        if (TimedRequestHelper.isAlive(COMMAND_NAME, playerUUID)) {
            plot.wipe();
            CommandUtil.respondSuccess(context, "Plot wipe queued.");

            SoftErrorState success = PlotStorageHandler.unclaimPlot(player);
            if (success.isError()) {
                CommandUtil.respondFailure(context, success.getMessage());
                return 0;
            }
            CommandUtil.translatableSuccess(context, "commands.wideplots.response.unclaim.success");
        } else {
            CommandUtil.translatableSuccess(context, "commands.wideplots.response.generic.resend_can_wipe", TIMEOUT / 20);
            TimedRequestHelper.timedRequest(
                    COMMAND_NAME,
                    playerUUID,
                    new TimedRequest(
                            TIMEOUT,
                            () -> CommandUtil.translatableFailure(context, "commands.wideplots.response.unclaim.timed_out")
                    )
            );
        }

        return 1;
    }
}
