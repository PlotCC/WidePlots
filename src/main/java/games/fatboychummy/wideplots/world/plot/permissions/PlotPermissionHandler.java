package games.fatboychummy.wideplots.world.plot.permissions;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.world.PlotChunkGenerator;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

    public PlotPermissionHandler() {}

    public static void init() {
        // Register event listeners for block breaking, block placing, chest opening, etc.
        WidePlots.LOGGER.info("Setting up permission handling.");

        // Hook into block break events
        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) -> {
                    long plotKey = keyFromCoords(pos.getX(), pos.getZ());
                    PlotPermissions permissions = activePlots.get(plotKey);

                    PlotPermission result = (
                            permissions == null || !isActuallyInBounds(pos) ?
                                    PlotPermissions.defaultPermissions.getActionResult( // Player is not within a plot
                                            player.getStringUUID(),
                                            PlotActionType.BUILD,
                                            state
                                    ) :
                                    permissions.getActionResult( // Player within plot
                                            player.getStringUUID(),
                                            PlotActionType.BUILD,
                                            state
                                    )
                    );

                    if (result != PlotPermission.GRANT) {
                        tellPlayerDisallowedAction(
                                player,
                                PlotActionType.BUILD,
                                isActuallyInBounds(pos) ? "You do not own this plot." : "You may not alter the roads."
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
                    long plotKey = keyFromCoords(pos.getX(), pos.getZ());
                    PlotPermissions permissions = activePlots.get(plotKey);

                    if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem) {
                        // Right-clicking while holding a block, likely trying to place a block.
                        PlotPermission result = (
                                permissions == null || !isActuallyInBounds(pos) ?
                                        PlotPermissions.defaultPermissions.getActionResult(
                                                player.getStringUUID(),
                                                PlotActionType.BUILD,
                                                block
                                        ) :
                                        permissions.getActionResult(
                                                player.getStringUUID(),
                                                PlotActionType.BUILD,
                                                block
                                        )
                        );

                        return result == PlotPermission.GRANT ? InteractionResult.PASS : InteractionResult.FAIL;
                    }

                    if (block.hasBlockEntity()) {
                        PlotPermission result = (
                                permissions == null || !isActuallyInBounds(pos) ?
                                        PlotPermissions.defaultPermissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.ACCESS,
                                            block
                                        ) :
                                        permissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.ACCESS,
                                            block
                                        )
                        );

                        return result == PlotPermission.GRANT ? InteractionResult.PASS : InteractionResult.FAIL;
                    }

                    // Right-clicking while holding nothing, likely trying to interact with a block.
                    PlotPermission result = (
                            permissions == null || !isActuallyInBounds(pos) ?
                                    PlotPermissions.defaultPermissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.INTERACT,
                                            block
                                    ) :
                                    permissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.INTERACT,
                                            block
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
                    long plotKey = keyFromCoords((int) (player.getX() + 0.5d), (int) (player.getZ() + 0.5d));
                    PlotPermissions permissions = activePlots.get(plotKey);
                    ItemStack itemStack = player.getItemInHand(hand);
                    BlockPos playerPos = new BlockPos(
                            (int) (player.getX() + 0.5d),
                            (int) (player.getY() + 0.5d),
                            (int) (player.getZ() + 0.5d)
                    );
                    BlockState block = world.getBlockState(playerPos);

                    PlotPermission result = (
                            permissions == null || !isActuallyInBounds(playerPos) ?
                                    PlotPermissions.defaultPermissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.ACCESS,
                                            block
                                    ) :
                                    permissions.getActionResult(
                                            player.getStringUUID(),
                                            PlotActionType.ACCESS,
                                            block
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

    private static void tellPlayerDisallowedAction(Player player, PlotActionType actionType, @Nullable String message) {
        String actionMessage = "You cannot do that";
        switch (actionType) {
            case PVE, PVP -> actionMessage = "You may not attack here";
            case BUILD -> actionMessage = "You cannot build here";
            case ENTER -> actionMessage = "You cannot enter this location";
            case SET_HOME -> actionMessage = "You cannot set a home in this location";
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

    /**
     * Gets the unique key for the given plot coordinates (x, z) to store in the activePlots map.
     * @param x The x coordinate of the plot.
     * @param z The z coordinate of the plot.
     * @return A unique long key representing the plot coordinates (x, z).
     */
    private static long key(int x, int z) {
        return ((long)x << 32) ^ (z & 0xffffffffL);
    }

    /**
     * Converts world coordinates (x, z) to plot coordinates and returns the unique key for the plot at those coordinates.
     * @param x The x coordinate in the world.
     * @param z The z coordinate in the world.
     * @return A unique long key representing the plot coordinates (x, z) corresponding to the given world coordinates.
     */
    private static long keyFromCoords(int x, int z) {
        return key(Math.floorDiv(x, CELL), Math.floorDiv(z, CELL));
    }

    /**
     * Checks if a BlockPos is actually within the bounds of a plot.
     * @param pos The position to check.
     * @return Whether the position is actually within a plot.
     */
    private static boolean isActuallyInBounds(BlockPos pos) {
        return Math.floorMod(pos.getX(), CELL) >= PlotChunkGenerator.ROAD_WIDTH &&
                Math.floorMod(pos.getZ(), CELL) >= PlotChunkGenerator.ROAD_WIDTH;
    }
}
