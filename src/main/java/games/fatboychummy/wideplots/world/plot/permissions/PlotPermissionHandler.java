package games.fatboychummy.wideplots.world.plot.permissions;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.util.PlotUtility;
import games.fatboychummy.wideplots.world.generation.PlotChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Actually applies plot permissions to the player, updates internal records, stores all currently active plot information.
 */
public class PlotPermissionHandler {
    // Map of active plot coordinates (x, z) to their permission data.
    private static final Map<Long, PlotPermissions> activePlots = new HashMap<>();;
    private static final int CELL = PlotChunkGenerator.PLOT_SIZE + PlotChunkGenerator.ROAD_WIDTH;

    public static void init() {
        // Register event listeners for block breaking, block placing, chest opening, etc.
        WidePlots.LOGGER.info("Setting up permission handling.");

        // Hook into block break events
        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) -> {
                    long plotKey = PlotUtility.keyFromCoords(pos.getX(), pos.getZ());
                    PlotPermissions permissions = activePlots.get(plotKey);

                    PlotPermission result = (
                            permissions == null || !PlotUtility.isActuallyInBounds(pos) ?
                                    PlotPermissions.defaultPermissions.getActionResult( // Player is not within a plot
                                            player.getStringUUID(),
                                            PlotActionType.BUILD,
                                            state,
                                            pos
                                    ) :
                                    permissions.getActionResult( // Player within plot
                                            player.getStringUUID(),
                                            PlotActionType.BUILD,
                                            state,
                                            pos
                                    )
                    );

                    if (result != PlotPermission.GRANT) {
                        tellPlayerDisallowedAction(
                                player,
                                PlotActionType.BUILD,
                                PlotUtility.isActuallyInBounds(pos) ? "You do not own this plot." : "You may not alter the roads."
                        );
                    }

                    return result == PlotPermission.GRANT;
                }
        );

        // Hook into use block events (can be block place, container access, or block interaction)
        UseBlockCallback.EVENT.register(
                (player, world, hand, hitResult) -> {
                    BlockPos pos = hitResult.getBlockPos();
                    BlockState block = world.getBlockState(pos);
                    long plotKey = PlotUtility.keyFromCoords(pos.getX(), pos.getZ());
                    PlotPermissions permissions = activePlots.get(plotKey);

                    if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem) {
                        // Right-clicking while holding a block, likely trying to place a block.
                        PlotPermission result = (
                                permissions == null || !PlotUtility.isActuallyInBounds(pos) ?
                                        PlotPermissions.defaultPermissions.getActionResult(
                                                player.getStringUUID(),
                                                PlotActionType.BUILD,
                                                block,
                                                pos
                                        ) :
                                        permissions.getActionResult(
                                                player.getStringUUID(),
                                                PlotActionType.BUILD,
                                                block,
                                                pos
                                        )
                        );

                        return result == PlotPermission.GRANT ? InteractionResult.PASS : InteractionResult.FAIL;
                    }

                    if (block.hasBlockEntity()) {
                        PlotPermission result = (
                                permissions == null || !PlotUtility.isActuallyInBounds(pos) ?
                                        PlotPermissions.defaultPermissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.ACCESS,
                                            block,
                                            pos
                                        ) :
                                        permissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.ACCESS,
                                            block,
                                            pos
                                        )
                        );

                        return result == PlotPermission.GRANT ? InteractionResult.PASS : InteractionResult.FAIL;
                    }

                    // Right-clicking while holding nothing, likely trying to interact with a block.
                    PlotPermission result = (
                            permissions == null || !PlotUtility.isActuallyInBounds(pos) ?
                                    PlotPermissions.defaultPermissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.INTERACT,
                                            block,
                                            pos
                                    ) :
                                    permissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.INTERACT,
                                            block,
                                            pos
                                    )
                    );

                    if (result != PlotPermission.GRANT) {
                        tellPlayerDisallowedAction(
                                player,
                                PlotActionType.INTERACT,
                                null
                        );
                    }

                    return result == PlotPermission.GRANT ? InteractionResult.PASS : InteractionResult.FAIL;
                }
        );

        // Hook into use item events (i.e: bow draw, fire charge, etc.)
        UseItemCallback.EVENT.register(
                (player, world, hand) -> {
                    long plotKey = PlotUtility.keyFromCoords(player.getBlockX(), player.getBlockZ());
                    PlotPermissions permissions = activePlots.get(plotKey);
                    ItemStack itemStack = player.getItemInHand(hand);
                    BlockPos playerPos = new BlockPos(
                            player.getBlockX(),
                            player.getBlockY(),
                            player.getBlockZ()
                    );
                    BlockState block = world.getBlockState(playerPos);

                    PlotPermission result = (
                            permissions == null || !PlotUtility.isActuallyInBounds(playerPos) ?
                                    PlotPermissions.defaultPermissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.ACCESS,
                                            null,
                                            playerPos
                                    ) :
                                    permissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.ACCESS,
                                            null,
                                            playerPos
                                    )
                    );

                    return result == PlotPermission.GRANT ? InteractionResultHolder.pass(itemStack): InteractionResultHolder.fail(itemStack);
                }
        );

        // Hook into entity callback for PVP and PVE checking.
        AttackEntityCallback.EVENT.register(
                (player, world, hand, entity, hitResult) -> {
                    return InteractionResult.PASS;
                }
        );

        // Hook into the end of the server tick to check if any player has entered a plot they should not be in.
        //TODO
        ServerTickEvents.END_SERVER_TICK.register(
                (minecraftServer) -> {
                    PlayerList players = minecraftServer.getPlayerList();
                }
        );
    }

    /**
     * Registers a new active plot, along with the permissions object to use for it.
     * @param key The key for the plot.
     * @param permissions The PlotPermissions object registered to the plot.
     * @see PlotUtility#key(int, int)
     * @return True if the registration was successful, false if the plot is already registered to another permission object.
     */
    public static boolean register(long key, PlotPermissions permissions) {
        if (activePlots.containsKey(key)) {
            return false;
        }

        activePlots.put(key, permissions);
        return true;
    }

    /**
     * Removes an active plot's permissions (usually due to being unclaimed).
     * @param key The key for the plot.
     */
    public static void remove(long key) {
        activePlots.remove(key);
    }

    /**
     * Tells a player that the action they've done is not allowed.
     * @param player The player to tell.
     * @param actionType The PlotActionType they performed.
     * @param message An optional additional message to tack on.
     */
    private static void tellPlayerDisallowedAction(Player player, PlotActionType actionType, @Nullable String message) {
        String actionMessage = "You cannot do that";
        switch (actionType) {
            case PVE, PVP -> actionMessage = "You may not attack here";
            case BUILD -> actionMessage = "You cannot build here";
            case ENTER -> actionMessage = "You cannot enter this location";
            case SET_HOME -> actionMessage = "You cannot set a home in this location";
            case SETTINGS -> actionMessage = "You cannot change the settings of this plot.";
        }

        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xff5555));

        if (message == null) {
            player.sendSystemMessage(
                    Component.literal(actionMessage + ".")
                            .withStyle(style)
            );
            return;
        }

        player.sendSystemMessage(
                Component.literal(actionMessage + ": " + message)
                        .withStyle(style)
        );
    }
}
