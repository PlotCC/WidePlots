package games.fatboychummy.wideplots.item;

import games.fatboychummy.wideplots.command.impl.permissions.PlotPermissionsGetBoundingToolCommand;
import games.fatboychummy.wideplots.util.ItemUtil;
import games.fatboychummy.wideplots.util.PlotUtility;
import games.fatboychummy.wideplots.world.plot.permissions.*;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class BoundingToolItem extends Item {
    public static final String NAME = "bounding_tool";
    public static final int COOLDOWN_TICKS = 5;

    // TODO: When crouch-scrolling, should swap through plot permission sets.

    public BoundingToolItem(Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext context) {
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        assert player != null;

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.CONSUME;
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        // Check the tag is on the item.
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(PlotPermissionsGetBoundingToolCommand.BOUNDING_TOOL_TAG_NAME)) {
            ItemUtil.sendError(context, "You must use '/plot permissions getBoundingTool <permissionSetName>' to use this item.");
            return InteractionResult.FAIL;
        }
        String setName = tag.getString(PlotPermissionsGetBoundingToolCommand.BOUNDING_TOOL_TAG_NAME);

        // Check the block is within a plot.
        PlotStorage plot = PlotStorageHandler.getPlot(pos.getX(), pos.getZ());
        if (plot == null || !PlotUtility.isActuallyInBounds(pos)) {
            ItemUtil.sendError(context, "This block is not within a plot.");
            return InteractionResult.FAIL;
        }

        // Check the player has permission to do this in this plot.
        if (plot.getPermissions().getActionResult(player.getStringUUID(),  PlotActionType.INTERACT, null, pos) != PlotPermission.GRANT) {
            ItemUtil.sendError(context, "You do not have permission to interact with this block.");
            return InteractionResult.FAIL;
        }

        // Check the permission set still exists.
        PlotPermissionSet set = plot.getPermissions().getPermissionSet(setName);
        if (set == null) {
            ItemUtil.sendError(context, "The permission set associated with this item no longer exists.");
            return InteractionResult.FAIL;
        }

        // If they player crouch-rightclicks, clear the selection.
        if (player.isCrouching()) {
            BounderHandler.removeBounder(player.getStringUUID());
            ItemUtil.sendMessage(context, "Cleared bounding box selection.");
            return InteractionResult.FAIL;
        }

        if (context.getLevel().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        // Handle the bounding box selection.
        Bounder result = BounderHandler.hit(player.getStringUUID(), pos);
        if (result == null) {
            ItemUtil.sendMessage(context, "Set the first position of the bounding box.");
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        ItemUtil.sendMessage(context, "Set the second position of the bounding box. Applied bounding box to permission set " + setName + "!");
        set.setBoundingBox(result.getBoundingBox());
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
