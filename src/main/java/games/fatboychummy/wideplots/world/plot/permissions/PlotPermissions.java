package games.fatboychummy.wideplots.world.plot.permissions;

import games.fatboychummy.wideplots.WidePlots;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Holds permissions for a plot, including who can build, who can access, etc.
 */
public class PlotPermissions {
    // The server default permissions for every plot. This is used as a fallback for any permissions that are not set for a specific plot or player.
    // No permissions are allowed to be set to "UNCHANGED" in this set.
    public static final PlotPermissionSet defaultPermissions = new PlotPermissionSet("Server Default Permissions");

    // Any overrides to the plot-default permissions for specific players, blocks, or items.
    private final ArrayList<PlotPermissionSet> playerPermissions;

    // The owner of the plot.
    private final String ownerUUID;

    public PlotPermissions(String ownerUUID) {
        this.playerPermissions = new ArrayList<>();
        this.ownerUUID = ownerUUID;
    }

    /**
     * Gets the permission for a specific player to perform a specific action.
     * This method checks player-specific permissions first, then falls back to the default plot permissions if no
     * specific permissions are set for the player.
     * @param playerUUID The UUID of the player performing the action.
     * @param actionType The type of action being performed (e.g. BUILD, INTERACT, etc.).
     * @param blockState The BlockState of the block being checked.
     * @param blockPos The position being interacted with.
     * @return The permission for the specified player to perform the specified action. Should be either GRANT or DENY.
     */
    public PlotPermission getActionResult(String playerUUID, PlotActionType actionType, @Nullable BlockState blockState, @Nullable BlockPos blockPos) {
        WidePlots.LOGGER.info("Player {} trying {} (owner is {})", playerUUID, actionType.name(), ownerUUID);
        if (playerUUID == null) {
            return PlotPermission.DENY; // Deny all actions for null players
        }
        if (playerUUID.equals(ownerUUID)) {
            return PlotPermission.GRANT; // Grant all permissions to the plot owner
        }

        // Only check permission sets which don't have specific positions
        if (blockPos == null) {
            // Run down the array of player-specific permissions, returning only the first applicable permission that
            // does not have a bounding box.
            for (PlotPermissionSet permissionSet: playerPermissions) {
                if (permissionSet.getBoundingBox() == null) {
                    PlotPermission permission = permissionSet.getActionResult(playerUUID, actionType, blockState, null);
                    if (permission !=  PlotPermission.UNCHANGED) {
                        return permission;
                    }
                }
            }

            return defaultPermissions.getActionResult(playerUUID, actionType, blockState, null);
        }

        // Run down the array of player-specific permissions and return the first applicable permission we find.
        for (PlotPermissionSet permissionSet : playerPermissions) {
            PlotPermission permission = permissionSet.getActionResult(playerUUID, actionType, blockState, blockPos);
            if (permission != PlotPermission.UNCHANGED) {
                return permission; // Return the first applicable permission we find
            }
        }

        // If we made it here, no player/block/item/etc-specific permissions applied, so we return the default plot permissions.
        return defaultPermissions.getActionResult(playerUUID, actionType, blockState, blockPos);
    }

    public void addPermissionSet(PlotPermissionSet permissionSet) {
        this.playerPermissions.add(permissionSet);
    }

    public void addPermissionSet(String name) {
        addPermissionSet(new PlotPermissionSet(name));
    }

    public boolean hasPermissionSet(String name) {
        for (PlotPermissionSet set : playerPermissions) {
            if (set.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public int getPermissionSetIndex(String name) {
        for (int i = 0; i < playerPermissions.size(); i++) {
            if (playerPermissions.get(i).getName().equals(name)) {
                return i;
            }
        }

        return -1;
    }

    @Nullable
    public PlotPermissionSet getPermissionSet(String name) {
        for (PlotPermissionSet set : playerPermissions) {
            if (set.getName().equals(name)) {
                return set;
            }
        }

        return null;
    }

    public void removePermissionSet(PlotPermissionSet permissionSet) {
        this.playerPermissions.remove(permissionSet);
    }

    public void removePermissionSet(String name) {
        for (PlotPermissionSet set : playerPermissions) {
            if (set.getName().equals(name)) {
                removePermissionSet(set);
                return;
            }
        }
    }

    public ArrayList<PlotPermissionSet> getPlayerPermissions() {
        return playerPermissions;
    }

    public void reorganize(int from, int to) {
        if (from < 0 || from >= playerPermissions.size() || to < 0 || to >= playerPermissions.size()) {
            // Clamp the indices to valid values.
            from = Math.max(0, Math.min(from, playerPermissions.size() - 1));
            to = Math.max(0, Math.min(to, playerPermissions.size() - 1));
        }
        if (from == to) {
            return; // No need to reorganize if the indices are the same.
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
        defaultPermissions.setPermission(PlotActionType.SETTINGS, PlotPermission.DENY);
        defaultPermissions.setPermission(PlotActionType.PISTONS, PlotPermission.DENY);
    }
}
