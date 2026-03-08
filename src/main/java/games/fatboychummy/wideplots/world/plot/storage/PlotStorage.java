package games.fatboychummy.wideplots.world.plot.storage;

import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissions;

/**
 * Holds an individual plot's data, including ownership, permissions, and backup information.
 *
 * Note: When a plot is unclaimed, its PlotStorage object becomes invalid and should not be used.
 */
public class PlotStorage {
    // The player-owner of the plot.
    private final String ownerUUID;

    // The plot's Plot Coordinates (x, z)
    private final int x;
    private final int z;

    // Permissions data (who can build, who can access, etc.)
    private final PlotPermissions permissions;

    public PlotStorage(String ownerUUID, int x, int z) {
        this.ownerUUID = ownerUUID;
        this.x = x;
        this.z = z;
        this.permissions = new PlotPermissions(ownerUUID);
    }
}
