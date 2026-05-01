package games.fatboychummy.wideplots.util;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.world.PlotDimension;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermission;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;


public class CommandUtil {
    /**
     * Sends an error message to the command source indicating that the command is not yet implemented.
     * @param context The command context to respond to.
     */
    public static void notYetImplemented(CommandContext<CommandSourceStack> context) {
        translatableFailure(context, "commands.wideplots.response.generic.nyi");
    }

    /**
     * Sends an error message to the command source indicating that they do not have permission to use the command.
     * @param context The command context to respond to.
     */
    public static void noPermission(CommandContext<CommandSourceStack> context) {
        translatableFailure(context, "commands.wideplots.response.generic.no_permission");
    }

    /**
     * Sends an error message to the command source indicating that only players can use the command.
     * @param context The command context to respond to.
     */
    public static void notAPlayer(CommandContext<CommandSourceStack> context) {
        translatableFailure(context, "commands.wideplots.response.generic.only_players");
    }

    /**
     * Sends an error message to the command source indicating that they must be in the plot dimension to use the command.
     * @param context The command context to respond to.
     */
    public static void notInPlotDimension(CommandContext<CommandSourceStack> context) {
        translatableFailure(context, "commands.wideplots.response.generic.dimension");
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
        //if (!inPlotDimension(context)) {return true;}
        //return false;
        return !inPlotDimension(context);
    }

    /**
     * Sends a success message to the command source.
     * @param context The command context to respond to.
     * @param message The success message to send.
     */
    public static void respondSuccess(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSystemMessage(Component.literal(message));
    }

    /**
     * Sends a success message to the command source.
     * @param context The command context to respond to.
     * @param message The success message to send.
     */
    public static void respondSuccess(CommandContext<CommandSourceStack> context, Component message) {
        context.getSource().sendSystemMessage(message);
    }

    public static void translatableSuccess(
            CommandContext<CommandSourceStack> context,
            @Translatable(foldMethod = true) String key,
            Object... args
    ) {
        context.getSource().sendSystemMessage(Component.translatable(key, args));
    }

    /**
     * Sends a failure message to the command source.
     * @param context The command context to respond to.
     * @param message The failure message to send.
     */
    public static void respondFailure(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSystemMessage(Component.literal(message)
                .withStyle(style -> style.withColor(0xFF5555)));
    }

    public static void translatableFailure(
            CommandContext<CommandSourceStack> context,
            @Translatable(foldMethod = true) String key,
            Object... args
    ) {
        context.getSource().sendSystemMessage(Component.translatable(key, args)
                .withStyle(style -> style.withColor(0xFF5555)));
    }

    /**
     * Sends a failure message to the command source. Adds `.withStyle(style -> style.withColor(0xFF5555))`
     * @param context The command context to respond to.
     * @param message The failure message to send.
     */
    public static void respondFailure(CommandContext<CommandSourceStack> context, MutableComponent message) {
        context.getSource().sendSystemMessage(
                message.withStyle(style -> style.withColor(0xFF5555))
        );
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
            translatableFailure(context, "commands.wideplots.response.generic.stand_in_plot");
            return true;
        }

        if (!plot.getOwnerUUID().equals(player.getStringUUID())) {
            translatableFailure(context, "commands.wideplots.response.generic.only_owner");
            return true;
        }

        return false;
    }

    /**
     * Checks if the player has permission to perform the given action in the given plot. If not, sends an appropriate error message and returns true.
     * `if (playerBlockedByPermissions(context, plot, PlotActionType.BUILD)) {return 0;}` is a common pattern in many plot commands.
     * @param context The command context to check and respond to if the player does not have permission to perform the action in the plot.
     * @param plot The plot to check permissions for.
     * @param action The action to check permissions for.
     * @return True if the player does not have permission to perform the action in the plot, false if they do have permission.
     */
    public static boolean blockedByPermissions(CommandContext<CommandSourceStack> context, PlotStorage plot, PlotActionType action) {
        ServerPlayer player = context.getSource().getPlayer();
        assert player != null;

        if (plot.getPermissions().getActionResult(player.getStringUUID(), action, null, null) == PlotPermission.GRANT) {
            return false;
        }
        translatableFailure(context, "commands.wideplots.response.generic.no_permission_plot");
        return true;
    }

    public static ServerPlayer requirePlayer(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            throw new IllegalStateException("Player is null");
        }
        return player;
    }
}
