package games.fatboychummy.wideplots.world.player;

/**
 * Handles storage and management of player data related to plot ownership, permissions, and other data.
 * Planned data to store per player:
 *   - Owned plot IDs (list of plot coordinates or unique IDs)
 *   - Plot backups/save files
 *      - For each current plot, store a backup of the plot's state (done in PlotStorage, but linked here).
 *      - Allow players to save up to XMB of save files (configurable).
 *          - Save files can be loaded to any plot (so long as the plot is the correct size).
 *      - Need some kind of DB or file-based storage system to manage these backups efficiently.
 *      - Players will be able to download their plots as schematic files for local storage.
 */
public class PlotPlayerStorage {
}
