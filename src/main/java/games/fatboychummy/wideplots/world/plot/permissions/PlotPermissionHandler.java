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
    private static final Map<Long, PlotAccessManager> activePlots = new HashMap<>();;
    private static final int CELL = PlotChunkGenerator.PLOT_SIZE + PlotChunkGenerator.ROAD_WIDTH;

    public static void init() {
        // Register event listeners for block breaking, block placing, chest opening, etc.
        WidePlots.LOGGER.info("Setting up permission handling.");

        // Hook into block break events
        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) -> {
                    long plotKey = PlotUtility.keyFromCoords(pos.getX(), pos.getZ());
                    PlotAccessManager permissions = activePlots.get(plotKey);

                    PlotPermissionResult result = (
                            permissions == null || !PlotUtility.isActuallyInBounds(pos) ?
                                    PlotAccessManager.defaultPermissions.getActionResult( // Player is not within a plot
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

                    if (result != PlotPermissionResult.GRANT) {
                        tellPlayerDisallowedActionC(
                                player,
                                PlotActionType.BUILD,
                                PlotUtility.isActuallyInBounds(pos) ? Component.translatable("permissions.wideplots.action.disallowed.build.plot") : Component.translatable("permissions.wideplots.action.disallowed.build.road")
                        );
                    }

                    return result == PlotPermissionResult.GRANT;
                }
        );

        // Hook into use block events (can be block place, container access, or block interaction)
        UseBlockCallback.EVENT.register(
                (player, world, hand, hitResult) -> {
                    BlockPos pos = hitResult.getBlockPos();
                    BlockState block = world.getBlockState(pos);
                    long plotKey = PlotUtility.keyFromCoords(pos.getX(), pos.getZ());
                    PlotAccessManager permissions = activePlots.get(plotKey);

                    if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem) {
                        // Right-clicking while holding a block, likely trying to place a block.
                        PlotPermissionResult result = (
                                permissions == null || !PlotUtility.isActuallyInBounds(pos) ?
                                        PlotAccessManager.defaultPermissions.getActionResult(
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

                        if (result != PlotPermissionResult.GRANT) {
                            tellPlayerDisallowedAction(
                                    player,
                                    PlotActionType.BUILD,
                                    null
                            );
                        }

                        return result == PlotPermissionResult.GRANT ? InteractionResult.PASS : InteractionResult.FAIL;
                    }

                    if (block.hasBlockEntity()) {
                        PlotPermissionResult result = (
                                permissions == null || !PlotUtility.isActuallyInBounds(pos) ?
                                        PlotAccessManager.defaultPermissions.getActionResult(
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

                        if (result != PlotPermissionResult.GRANT) {
                            tellPlayerDisallowedAction(
                                    player,
                                    PlotActionType.ACCESS,
                                    null
                            );
                        }

                        return result == PlotPermissionResult.GRANT ? InteractionResult.PASS : InteractionResult.FAIL;
                    }

                    // Right-clicking while holding nothing, likely trying to interact with a block.
                    PlotPermissionResult result = (
                            permissions == null || !PlotUtility.isActuallyInBounds(pos) ?
                                    PlotAccessManager.defaultPermissions.getActionResult(
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

                    if (result != PlotPermissionResult.GRANT) {
                        tellPlayerDisallowedAction(
                                player,
                                PlotActionType.INTERACT,
                                null
                        );
                    }

                    return result == PlotPermissionResult.GRANT ? InteractionResult.PASS : InteractionResult.FAIL;
                }
        );

        // Hook into use item events (i.e: bow draw, fire charge, etc.)
        UseItemCallback.EVENT.register(
                (player, world, hand) -> {
                    long plotKey = PlotUtility.keyFromCoords(player.getBlockX(), player.getBlockZ());
                    PlotAccessManager permissions = activePlots.get(plotKey);
                    ItemStack itemStack = player.getItemInHand(hand);
                    BlockPos playerPos = new BlockPos(
                            player.getBlockX(),
                            player.getBlockY(),
                            player.getBlockZ()
                    );
                    BlockState block = world.getBlockState(playerPos);

                    PlotPermissionResult result = (
                            permissions == null || !PlotUtility.isActuallyInBounds(playerPos) ?
                                    PlotAccessManager.defaultPermissions.getActionResult(
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

                    if (result != PlotPermissionResult.GRANT) {
                        tellPlayerDisallowedAction(
                                player,
                                PlotActionType.ACCESS,
                                null
                        );
                    }

                    return result == PlotPermissionResult.GRANT ? InteractionResultHolder.pass(itemStack): InteractionResultHolder.fail(itemStack);
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
    public static boolean register(long key, PlotAccessManager permissions) {
        WidePlots.LOGGER.info("Registering PlotPermissionHandler for key {}", key);
        if (activePlots.containsKey(key)) {
            return false;
        }

        activePlots.put(key, permissions);
        return true;
    }

    /**
     * Gets a plot permissions object for a given key.
     */
    public static @Nullable PlotAccessManager get(long key) {
        WidePlots.LOGGER.info("Getting PlotPermissionHandler for key {}", key);
        return activePlots.get(key);
    }

    public static @Nullable PlotAccessManager get(BlockPos pos) {
        long key = PlotUtility.keyFromCoords(pos.getX(), pos.getZ());
        WidePlots.LOGGER.info("Getting PlotPermissionHandler for pos {} ({})", pos, key);
        return get(key);
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
     * @param message An optional additional message to tack on, as a direct string.
     */
    private static void tellPlayerDisallowedAction(Player player, PlotActionType actionType, @Nullable String message) {
        String actionKey = "permissions.wideplots.action.disallowed.default";
        switch (actionType) {
            case PVE, PVP -> actionKey = "permissions.wideplots.action.disallowed.pvepvp";
            case BUILD -> actionKey = "permissions.wideplots.action.disallowed.build";
            case ENTER -> actionKey = "permissions.wideplots.action.disallowed.enter";
            case SET_HOME -> actionKey = "permissions.wideplots.action.disallowed.set_home";
            case SETTINGS -> actionKey = "permissions.wideplots.action.disallowed.settings";
            case ACCESS -> actionKey = "permissions.wideplots.action.disallowed.access";
            case INTERACT -> actionKey = "permissions.wideplots.action.disallowed.interact";
            case PISTONS -> actionKey = "permissions.wideplots.action.disallowed.pistons";
        }

        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xff5555));

        if (message == null) {
            player.sendSystemMessage(
                    Component.translatable(actionKey)
                            .withStyle(style)
            );
            return;
        }

        player.sendSystemMessage(
                Component.translatable(actionKey)
                        .append(Component.literal("\n  " + message))
                        .withStyle(style)
        );
    }

    /**
     * Tells a player that the action they've done is not allowed.
     * @param player The player to tell.
     * @param actionType The PlotActionType they performed.
     * @param message An optional additional message to tack on, as a component.
     */
    private static void tellPlayerDisallowedActionC(Player player, PlotActionType actionType, Component message) {
        tellPlayerDisallowedAction(player, actionType, message.getString());
    }
}
