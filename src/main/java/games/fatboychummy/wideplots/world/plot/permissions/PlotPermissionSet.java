package games.fatboychummy.wideplots.world.plot.permissions;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

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

    // The items that this permission set applies to. If empty, applies to all items.
    private ArrayList<String> applicableItems;

    // Whether the item list is a whitelist or blacklist. By default, we use a blacklist to include all items.
    private boolean isItemBlacklist;

    // The blocks (or block tags) that this permission set applies to. If empty, applies to all blocks.
    private ArrayList<String> applicableBlocks;

    // Whether the block list is a whitelist or blacklist. By default, we use a blacklist to include all blocks.
    private boolean isBlockBlacklist;

    // Whether this permission set is active or not. Inactive permission sets are ignored when checking permissions.
    private boolean isActive;

    // The area that this permission set applies to. If null, applies to the entire plot.
    private BoundingBox boundingBox;

    public PlotPermissionSet(String name) {
        this.name = name;
        this.permissionList = new PlotPermissionList();
        this.playerUUIDs = new ArrayList<String>();
        this.isPlayerBlacklist = true; // Default to blacklist (include everyone)
        this.applicableBlocks = new ArrayList<String>();
        this.isBlockBlacklist = true; // Default to blacklist (include all blocks)
        this.applicableItems = new ArrayList<String>();
        this.isItemBlacklist = true; // Default to blacklist (include all items)
        this.isActive = true; // Permission sets are active by default
        this.boundingBox = null; // Applies to entire plot by default
    }

    public void addUser(String playerUUID) {
        this.playerUUIDs.add(playerUUID);
    }

    public boolean hasUser(String playerUUID) {
        return this.playerUUIDs.contains(playerUUID);
    }

    public void removeUser(String playerUUID) {
        this.playerUUIDs.remove(playerUUID);
    }

    public void setPlayerBlacklist(boolean isBlacklist) {
        this.isPlayerBlacklist = isBlacklist;
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

    public void setBlockBlacklist(boolean isBlacklist) {
        this.isBlockBlacklist = isBlacklist;
    }

    public void addApplicableItem(String itemId) {
        this.applicableItems.add(itemId);
    }

    public boolean hasApplicableItem(String itemId) {
        return this.applicableItems.contains(itemId);
    }

    public void removeApplicableItem(String itemId) {
        this.applicableItems.remove(itemId);
    }

    public void setItemBlacklist(boolean isBlacklist) {
        this.isItemBlacklist = isBlacklist;
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

    /**
     * Sets the permission for a specific permission name.
     * @param permission The PlotActionType of the permission to set.
     * @param value The value of the permission (GRANT, UNCHANGED, DENY).
     */
    public void setPermission(PlotActionType permission, PlotPermission value) {
        this.permissionList.setPermission(permission, value);
    }


    /**
     * Whether this permission set allows the given action for the given player and block.
     * @param playerUUID The UUID of the player performing the action.
     * @param actionType The type of action being performed (e.g. BUILD, ACCESS, etc.)
     * @param blockState The block state being interacted with (if applicable). Can be null if not applicable.
     * @param itemUsed <todo> The item being used to perform the action (if applicable). Can be null if not applicable.
     * @return GRANT if the action is allowed, DENY if the action is denied, or UNCHANGED if this permission set does not apply to the player or action.
     */
    public PlotPermission getActionResult(String playerUUID, PlotActionType actionType, BlockState blockState) {
        if (!appliesToPlayer(playerUUID)) {
            return PlotPermission.UNCHANGED; // Permission set does not apply to this player.
        }





        return PlotPermission.UNCHANGED; // No specific permission set for this action.
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