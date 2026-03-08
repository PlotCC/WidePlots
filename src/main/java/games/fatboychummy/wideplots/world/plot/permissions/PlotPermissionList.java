package games.fatboychummy.wideplots.world.plot.permissions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Contains the list of permissions for a plot.
 *
 * Permissions are structured as a hashmap of the permission name to the permission value (GRANT, UNCHANGED, DENY) for each player.
 */
public class PlotPermissionList {
    // The permissions for this plot.
    private Map<PlotActionType, PlotPermission> permissions;

    public PlotPermissionList() {
        this.permissions = new HashMap<>();
    }

    /**
     * Sets the permission for a specific permission name.
     * @param permission The name of the permission to set.
     * @param value The value of the permission (GRANT, UNCHANGED, DENY).
     */
    public void setPermission(PlotActionType permission, PlotPermission value) {
        this.permissions.put(permission, value);
    }

    /**
     * Gets the permission for a specific permission name. If the permission is not set, returns UNCHANGED by default.
     * @param permission The name of the permission to get.
     * @return The value of the permission (GRANT, UNCHANGED, DENY) for the specified permission name.
     */
    public PlotPermission getPermission(PlotActionType permission) {
        return this.permissions.getOrDefault(permission, PlotPermission.UNCHANGED);
    }

    /**
     * Gets the set of all permission names that have been set for this plot.
     * @return A set of all permission names that have been set for this plot.
     */
    public Set<PlotActionType> getPermissions() {
        return this.permissions.keySet();
    }
}
