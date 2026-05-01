package games.fatboychummy.wideplots.world.plot.permissions;

import games.fatboychummy.wideplots.block.entity.PlotControllerBlockEntity;
import games.fatboychummy.wideplots.block.entity.events.WPPermissionChangedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Represents a single permission-set for a plot.
 */
public class PlotPermissionSet {
    // The name of this permission set
    private String name;

    // The actions that this permission set changes.
    private PlotPermissionList permissionList;

    // The UUIDs of players who are a part of this permission set.
    private ArrayList<String> playerUUIDs;

    // Whether the UUID list is whitelist or blacklist. By default, we use a blacklist to include everyone.
    private boolean isPlayerBlacklist;

    // The blocks (or block tags) that this permission set applies to. If empty, applies to all blocks.
    private ArrayList<String> applicableBlocks;

    // Whether the block list is a whitelist or blacklist. By default, we use a blacklist to include all blocks.
    private boolean isBlockBlacklist;

    // Whether this permission set is active or not. Inactive permission sets are ignored when checking permissions.
    private boolean isActive;

    // The area that this permission set applies to. If null, applies to the entire plot.
    private BoundingBox boundingBox;

    private PlotControllerBlockEntity controller;

    public PlotPermissionSet(String name) {
        this.name = name;
        this.permissionList = new PlotPermissionList();
        this.playerUUIDs = new ArrayList<String>();
        this.isPlayerBlacklist = true; // Default to blacklist (include everyone)
        this.applicableBlocks = new ArrayList<String>();
        this.isBlockBlacklist = true; // Default to blacklist (include all blocks)
        this.isActive = true; // Permission sets are active by default
        this.boundingBox = null; // Applies to entire plot by default
    }

    public String getName() {
        return name;
    }

    public void addPlayer(String playerUUID) {
        this.playerUUIDs.add(playerUUID);
    }

    public boolean hasPlayer(String playerUUID) {
        return this.playerUUIDs.contains(playerUUID);
    }

    public void removePlayer(String playerUUID) {
        this.playerUUIDs.remove(playerUUID);
    }

    public ArrayList<String> getPlayerUUIDs() {
        return playerUUIDs;
    }

    public void setPlayerBlacklist(boolean isBlacklist) {
        this.isPlayerBlacklist = isBlacklist;
    }

    public boolean isPlayerBlacklist() {
        return isPlayerBlacklist;
    }

    public void addApplicableBlock(String blockId) {
        this.applicableBlocks.add(blockId);
    }

    public boolean hasApplicableBlock(String blockId) {
        return this.applicableBlocks.contains(blockId);
    }

    public void removeApplicableBlock(String blockId) {
        this.applicableBlocks.remove(blockId);
    }

    public ArrayList<String> getApplicableBlocks() {
        return applicableBlocks;
    }

    public void setBlockBlacklist(boolean isBlacklist) {
        this.isBlockBlacklist = isBlacklist;
    }

    public boolean isBlockBlacklist() {
        return isBlockBlacklist;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Sets the permission for a specific permission name.
     * @param permission The PlotActionType of the permission to set.
     * @param value The value of the permission (GRANT, UNCHANGED, DENY).
     */
    public void setPermission(PlotActionType permission, PlotPermission value) {
        PlotPermission old = this.permissionList.getPermission(permission);
        String oldName = "unset";
        if (old != null) {
            oldName = old.name();
        }
        String newName = "unset";
        if (value != null) {
            newName = value.name();
        }

        if (controller != null) {
            controller.fireEvent(new WPPermissionChangedEvent(
                    permission.name(),
                    oldName,
                    newName
            ));
        }

        this.permissionList.setPermission(permission, value);
    }

    public PlotPermissionList getPermissionList() {
        return permissionList;
    }


    /**
     * Whether this permission set allows the given action for the given player and block.
     * @param playerUUID The UUID of the player performing the action.
     * @param actionType The type of action being performed (e.g. BUILD, ACCESS, etc.)
     * @param blockState the BlockState of the block being changed or altered or whatever the heck
     * @param blockPos The position the action is taking place at.
     * @return GRANT if the action is allowed, DENY if the action is denied, or UNCHANGED if this permission set does not apply to the player or action.
     */
    public PlotPermission getActionResult(String playerUUID, PlotActionType actionType, @Nullable BlockState blockState, @Nullable BlockPos blockPos) {
        if (!isActive()) {
            return PlotPermission.UNCHANGED; // Permission set is disabled.
        }

        if (!appliesToPlayer(playerUUID)) {
            return PlotPermission.UNCHANGED; // Permission set does not apply to this player.
        }

        if (boundingBox != null && blockPos != null) {
            if (!boundingBox.isInside(blockPos.getX(), blockPos.getY(), blockPos.getZ())) {
                return PlotPermission.UNCHANGED;
            } // Otherwise it applies to this position.
        }

        // If this has a bounding box, but no position is passed through, ignore the test.
        if (boundingBox != null && blockPos == null) {
            return PlotPermission.UNCHANGED;
        }

        if (blockState != null) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
            if (hasApplicableBlock(blockId.toString())) {
                if (isBlockBlacklist) {
                    return PlotPermission.UNCHANGED;
                } // Otherwise it applies to this block.
            }
        }

        return permissionList.getPermission(actionType);
    }

    /**
     * Checks if the permission set applies to the given player.
     * @param playerUUID The UUID of the player to check.
     * @return True if the player is included in this permission set, false otherwise.
     */
    private boolean appliesToPlayer(String playerUUID) {
        boolean contains = playerUUIDs.contains(playerUUID);
        return isPlayerBlacklist != contains;
    }

    /**
     * Checks if the permission set applies to the given block.
     * @param blockId The ID of the block to check (e.g. "minecraft:stone").
     * @return True if the block is included in this permission set, false otherwise.
     */


}