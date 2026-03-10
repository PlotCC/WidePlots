package games.fatboychummy.wideplots.util;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.world.PlotDimension;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CommandUtil {
    /**
     * Sends an error message to the command source indicating that the command is not yet implemented.
     * @param context The command context to respond to.
     */
    public static void notYetImplemented(CommandContext<CommandSourceStack> context) {
        respondFailure(context, "This command is not yet implemented!");
    }

    /**
     * Sends an error message to the command source indicating that they do not have permission to use the command.
     * @param context The command context to respond to.
     */
    public static void noPermission(CommandContext<CommandSourceStack> context) {
        respondFailure(context, "You do not have permission to use this command!");
    }

    /**
     * Sends an error message to the command source indicating that only players can use the command.
     * @param context The command context to respond to.
     */
    public static void notAPlayer(CommandContext<CommandSourceStack> context) {
        respondFailure(context, "Only players can use this command!");
    }

    /**
     * Sends an error message to the command source indicating that they must be in the plot dimension to use the command.
     * @param context The command context to respond to.
     */
    public static void notInPlotDimension(CommandContext<CommandSourceStack> context) {
        respondFailure(context, "You must be in the plot dimension to use this command!");
    }

    /**
     * Checks if the command source has the required permission level. If not, sends an error message and returns false.
     * @param context The command context to check and respond to if the source does not have the required permission level.
     * @param level The required permission level to use the command.
     * @return True if the source has the required permission level, false otherwise.
     */
    public static boolean checkPermission(CommandContext<CommandSourceStack> context, PermissionLevel level) {
        if (!context.getSource().hasPermission(level.getLevel())) {
            noPermission(context);
            return false;
        }
        return true;
    }

    /**
     * Checks if the command source is a player. If not, sends an error message and returns false.
     * @param context The command context to check and respond to if the source is not a player.
     * @return True if the source is a player, false otherwise.
     */
    public static boolean isPlayer(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) {
            notAPlayer(context);
            return false;
        }
        return true;
    }

    /**
     * Checks if the command source is in the plot dimension. If not, sends an error message and returns false.
     * @param context The command context to check and respond to if the source is not in the plot dimension.
     * @return True if the source is in the plot dimension, false otherwise.
     */
    public static boolean inPlotDimension(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().getLevel().dimension().equals(PlotDimension.PLOTDIM_LEVEL.dimension())) {
            notInPlotDimension(context);
            return false;
        }
        return true;
    }

    /**
     * The base checks for all plot commands: is the caller a player, are they in the plot dimension, and do they have the required permission level?
     * If any of these checks fail, an appropriate error message is sent to the command source and true is returned.
     * `if (shouldBlock(context, PermissionLevel.ADMIN)) {return 0;}` is a common pattern in all command execute methods.
     * @param context The command context to check and respond to if any checks fail.
     * @param permissionLevel The required permission level to use the command.
     * @return True if the command should be blocked (i.e. if any checks fail), false if the command can proceed.
     */
    public static boolean shouldBlock(CommandContext<CommandSourceStack> context, PermissionLevel permissionLevel) {
        if (!checkPermission(context, permissionLevel)) {return true;}
        if (!isPlayer(context)) {return true;}
        if (!inPlotDimension(context)) {return true;}
        return false;
    }

    /**
     * Sends a success message to the command source.
     * @param context The command context to respond to.
     * @param message The success message to send.
     */
    public static void respondSuccess(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), false);
    }

    /**
     * Sends a failure message to the command source.
     * @param context The command context to respond to.
     * @param message The failure message to send.
     */
    public static void respondFailure(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendFailure(Component.literal(message));
    }


    /**
     * Checks if the player is standing in a claimed plot and is the owner of that plot. If not, sends an appropriate error message and returns true.
     * `if (blockNonOwner(context)) {return 0;}` is a common pattern in all plot owner-only command execute methods.
     * @param context The command context to check and respond to if the player is not standing in a claimed plot or is not the owner of that plot.
     * @return True if the player is not standing in a claimed plot or is not the owner of that plot. False if they own the plot.
     */
    public static boolean blockNonOwner(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        assert player != null;

        PlotStorage plot = PlotStorageHandler.getPlot(player);
        if (plot == null) {
            CommandUtil.respondFailure(context, "You must be standing in a claimed plot to use this command.");
            return true;
        }

        if (!plot.getOwnerUUID().equals(player.getStringUUID())) {
            CommandUtil.respondFailure(context, "Only the plot owner can use this command.");
            return true;
        }

        return false;
    }
}
