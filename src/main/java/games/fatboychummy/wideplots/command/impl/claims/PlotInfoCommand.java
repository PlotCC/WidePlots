package games.fatboychummy.wideplots.command.impl.claims;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PlotInfoCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        ServerPlayer player = CommandUtil.requirePlayer(context);

        // Get information about the current plot.
        PlotStorage storage = PlotStorageHandler.getPlot(player);
        CommandUtil.respondSuccess(context, formatPlotInfo(context, storage));
        return 1;
    }

    private static String formatPlotInfo(CommandContext<CommandSourceStack> context, PlotStorage storage) {
        ServerPlayer player = CommandUtil.requirePlayer(context);
        StringBuilder builder = new StringBuilder("Name: " + storage.getName() + "\n" +
                "Owner: " + storage.getOwnerUUID() + "\n" +
                "Plot Coordinates: (" + storage.getX() + ", " + storage.getZ() + ")\n" +
                "Description: " + storage.getDescription() + "\n" +
                "Welcome Message: " + storage.getWelcomeMessage() + "\n" +
                "Departure Message: " + storage.getDepartureMessage() + "\n" +
                "Your Permissions: " + "\n");

        for (PlotActionType actionType : PlotActionType.values()) {
            builder.append("  - ").append(getActionName(actionType)).append(": ").append(storage.getPermissions().getActionResult(
                    player.getStringUUID(),
                    actionType,
                    null,
                    null
            ).toString());
        }

        return builder.toString();
    }


    private static String getActionName(PlotActionType actionType) {
        return switch (actionType) {
            case BUILD -> "Build/Break Blocks";
            case INTERACT -> "Interact with Entities/Blocks";
            case ENTER -> "Enter Plot";
            case PVE -> "PvE";
            case PVP -> "PvP";
            case ACCESS -> "Access Containers";
            case SET_HOME -> "Set Home";
            case SETTINGS -> "Change Settings";
            case PISTONS -> "Pistons";
        };
    }
}
