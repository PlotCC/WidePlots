package games.fatboychummy.wideplots.world.plot.storage;

/**
 * Handles storage and management of plot data, including plot ownership, permissions, and other data.
 * Planned data to store per plot:
 *  - Owner UUID
 *  - Plot coordinates (x, z)
 *  - Plot connections (north/east/south/west)
 *  - Permissions (who can build, who can access, etc.)
 *  - A single plot backup file.
 *  - For plot backups:
 *      - Store a backup of the plot's state (blocks, entities, etc.) when a player claims a plot or when they manually save it.
 *      - Allow players to restore their plot to a previous state using these backups.
 */
public class PlotStorageHandler {

}
