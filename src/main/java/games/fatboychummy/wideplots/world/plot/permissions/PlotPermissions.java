package games.fatboychummy.wideplots.world.plot.permissions;

import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

/**
 * Holds permissions for a plot, including who can build, who can access, etc.
 */
public class PlotPermissions {
    // The server default permissions for every plot. This is used as a fallback for any permissions that are not set for a specific plot or player.
    // No permissions are allowed to be set to "UNCHANGED" in this set.
    public static final PlotPermissionSet defaultPermissions = new PlotPermissionSet("Server Default Permissions");

    // Any overrides to the plot-default permissions for specific players, blocks, or items.
    private ArrayList<PlotPermissionSet> playerPermissions;

    // The owner of the plot.
    private final String ownerUUID;

    public PlotPermissions(String ownerUUID) {
        this.playerPermissions = new ArrayList<PlotPermissionSet>();
        this.ownerUUID = ownerUUID;
    }

    /**
     * Gets the permission for a specific player to perform a specific action.
     * This method checks player-specific permissions first, then falls back to the default plot permissions if no
     * specific permissions are set for the player.
     * @param playerUUID The UUID of the player performing the action.
     * @param actionType The type of action being performed (e.g. BUILD, INTERACT, etc.).
     * @param blockState The block being interacted with (if applicable).
     * @param itemType The item being interacted with (if applicable).
     * @return The permission for the specified player to perform the specified action. Should be either GRANT or DENY.
     */
    public PlotPermission getActionResult(String playerUUID, PlotActionType actionType, BlockState blockState) {
        if (playerUUID == null) {
            return PlotPermission.DENY; // Deny all actions for null players
        }
        if (playerUUID.equals(ownerUUID)) {
            return PlotPermission.GRANT; // Grant all permissions to the plot owner
        }

        // Run down the array of player-specific permissions and return the first applicable permission we find.
        for (PlotPermissionSet permissionSet : playerPermissions) {
            PlotPermission permission = permissionSet.getActionResult(playerUUID, actionType, blockState);
            if (permission != PlotPermission.UNCHANGED) {
                return permission; // Return the first applicable permission we find
            }
        }

        // If we made it here, no player/block/item/etc-specific permissions applied, so we return the default plot permissions.
        return defaultPermissions.getActionResult(playerUUID, actionType, blockState);
    }

    public void addPermissionSet(PlotPermissionSet permissionSet) {
        this.playerPermissions.add(permissionSet);
    }

    public void removePermissionSet(PlotPermissionSet permissionSet) {
        this.playerPermissions.remove(permissionSet);
    }

    public void reorganize(int from, int to) {
        if (from < 0 || from >= playerPermissions.size() || to < 0 || to >= playerPermissions.size()) {
            throw new IndexOutOfBoundsException("Invalid from/to indices for reorganizing permission sets.");
        }
        PlotPermissionSet permissionSet = playerPermissions.remove(from);
        playerPermissions.add(to, permissionSet);
    }

    public static void init() {
        // TODO: Make this configurable
        defaultPermissions.setPermission(PlotActionType.BUILD, PlotPermission.DENY);
        defaultPermissions.setPermission(PlotActionType.INTERACT, PlotPermission.DENY);
        defaultPermissions.setPermission(PlotActionType.ACCESS, PlotPermission.DENY);
        defaultPermissions.setPermission(PlotActionType.ENTER, PlotPermission.GRANT);
        defaultPermissions.setPermission(PlotActionType.PVP, PlotPermission.DENY);
        defaultPermissions.setPermission(PlotActionType.SET_HOME, PlotPermission.DENY);
    }
}
