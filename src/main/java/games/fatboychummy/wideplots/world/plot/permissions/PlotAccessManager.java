package games.fatboychummy.wideplots.world.plot.permissions;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.block.entity.PlotControllerBlockEntity;
import games.fatboychummy.wideplots.block.entity.events.WPEvent;
import games.fatboychummy.wideplots.block.entity.events.WPPlotAccessChangedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Holds permissions for a plot, including who can build, who can access, etc.
 */
public class PlotAccessManager {
    // The server default permissions for every plot. This is used as a fallback for any permissions that are not set for a specific plot or player.
    // No permissions are allowed to be set to "UNCHANGED" in this set.
    public static final PlotAccessRuleSet defaultPermissions = new PlotAccessRuleSet("Server Default Permissions");

    // Any overrides to the plot-default permissions for specific players, blocks, or items.
    private final ArrayList<PlotAccessRuleSet> playerPermissions;

    // The owner of the plot.
    private final String ownerUUID;

    // The Plot Controller registered to the plot.
    private @Nullable PlotControllerBlockEntity controller;

    public PlotAccessManager(String ownerUUID) {
        this.playerPermissions = new ArrayList<>();
        this.ownerUUID = ownerUUID;
    }

    public void registerController(@NotNull PlotControllerBlockEntity controller) {
        this.controller = controller;
        defaultPermissions.registerController(controller);
        for (PlotAccessRuleSet permissions : this.playerPermissions) {
            permissions.registerController(controller);
        }
    }

    public void removeController() {
        this.controller = null;
        defaultPermissions.removeController();
        for (PlotAccessRuleSet permissions : this.playerPermissions) {
            permissions.removeController();
        }
    }

    private void pushEvent(WPEvent event) {
        if (controller != null) {
            controller.fireEvent(event);
        }
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
    public PlotPermissionResult getActionResult(String playerUUID, PlotActionType actionType, @Nullable BlockState blockState, @Nullable BlockPos blockPos) {
        WidePlots.LOGGER.info("Player {} trying {} (owner is {})", playerUUID, actionType.name(), ownerUUID);
        if (playerUUID == null) {
            return PlotPermissionResult.DENY; // Deny all actions for null players
        }
        if (playerUUID.equals(ownerUUID)) {
            return PlotPermissionResult.GRANT; // Grant all permissions to the plot owner
        }

        // Only check permission sets which don't have specific positions
        if (blockPos == null) {
            // Run down the array of player-specific permissions, returning only the first applicable permission that
            // does not have a bounding box.
            for (PlotAccessRuleSet permissionSet: playerPermissions) {
                if (permissionSet.getBoundingBox() == null) {
                    PlotPermissionResult permission = permissionSet.getActionResult(playerUUID, actionType, blockState, null);
                    if (permission !=  PlotPermissionResult.UNCHANGED) {
                        return permission;
                    }
                }
            }

            return defaultPermissions.getActionResult(playerUUID, actionType, blockState, null);
        }

        // Run down the array of player-specific permissions and return the first applicable permission we find.
        for (PlotAccessRuleSet permissionSet : playerPermissions) {
            PlotPermissionResult permission = permissionSet.getActionResult(playerUUID, actionType, blockState, blockPos);
            if (permission != PlotPermissionResult.UNCHANGED) {
                return permission; // Return the first applicable permission we find
            }
        }

        // If we made it here, no player/block/item/etc-specific permissions applied, so we return the default plot permissions.
        return defaultPermissions.getActionResult(playerUUID, actionType, blockState, blockPos);
    }

    public void addPermissionSet(String runnerUUID, PlotAccessRuleSet permissionSet) {
        playerPermissions.add(permissionSet);

        if (controller != null) {
            permissionSet.registerController(controller);
        }
        pushEvent(WPPlotAccessChangedEvent.createSet(
                runnerUUID,
                permissionSet.getName(),
                playerPermissions.indexOf(permissionSet)
        ));
    }

    public void addPermissionSet(String runnerUUID, String name) {
        addPermissionSet(runnerUUID, new PlotAccessRuleSet(name));
    }

    public boolean hasPermissionSet(String name) {
        for (PlotAccessRuleSet set : playerPermissions) {
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
    public PlotAccessRuleSet getPermissionSet(String name) {
        for (PlotAccessRuleSet set : playerPermissions) {
            if (set.getName().equals(name)) {
                return set;
            }
        }

        return null;
    }

    public void removePermissionSet(String runnerUUID, PlotAccessRuleSet permissionSet) {
        this.playerPermissions.remove(permissionSet);

        if (controller != null) {
            permissionSet.registerController(controller);
        }
        pushEvent(WPPlotAccessChangedEvent.removeSet(
                runnerUUID,
                permissionSet.getName()
        ));
    }

    public void removePermissionSet(String runnerUUID, String name) {
        for (PlotAccessRuleSet set : playerPermissions) {
            if (set.getName().equals(name)) {
                removePermissionSet(runnerUUID, set);
                return;
            }
        }
    }

    public ArrayList<PlotAccessRuleSet> getPlayerPermissions() {
        return playerPermissions;
    }

    public void reorganize(String runnerUUID, int from, int to) {
        if (from < 0 || from >= playerPermissions.size() || to < 0 || to >= playerPermissions.size()) {
            // Clamp the indices to valid values.
            from = Math.max(0, Math.min(from, playerPermissions.size() - 1));
            to = Math.max(0, Math.min(to, playerPermissions.size() - 1));
        }
        if (from == to) {
            return; // No need to reorganize if the indices are the same.
        }

        PlotAccessRuleSet permissionSet = playerPermissions.remove(from);
        playerPermissions.add(to, permissionSet);

        if (controller != null) {
            permissionSet.registerController(controller);
        }
        pushEvent(WPPlotAccessChangedEvent.reorganizeSet(
                runnerUUID,
                permissionSet.getName(),
                from, to
        ));
    }

    public static void init() {
        // TODO: Make this configurable
        defaultPermissions.setPermission("SERVER", PlotActionType.BUILD, PlotPermissionResult.DENY);
        defaultPermissions.setPermission("SERVER", PlotActionType.INTERACT, PlotPermissionResult.DENY);
        defaultPermissions.setPermission("SERVER", PlotActionType.ACCESS, PlotPermissionResult.DENY);
        defaultPermissions.setPermission("SERVER", PlotActionType.ENTER, PlotPermissionResult.GRANT);
        defaultPermissions.setPermission("SERVER", PlotActionType.PVP, PlotPermissionResult.DENY);
        defaultPermissions.setPermission("SERVER", PlotActionType.SET_HOME, PlotPermissionResult.DENY);
        defaultPermissions.setPermission("SERVER", PlotActionType.SETTINGS, PlotPermissionResult.DENY);
        defaultPermissions.setPermission("SERVER", PlotActionType.PISTONS, PlotPermissionResult.DENY);
    }
}
