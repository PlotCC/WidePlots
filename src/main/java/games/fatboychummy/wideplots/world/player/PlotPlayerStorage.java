package games.fatboychummy.wideplots.world.player;

import games.fatboychummy.wideplots.world.PlotDimension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

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
    private static ServerLevel level;
    private long lastOnlineTick = 0;
    private boolean isOnline = false;
    private String uuid;

    public PlotPlayerStorage(String uuid) {
        this.uuid = uuid;

        // TODO: Check if we should keep this here.
        //this.lastOnlineTick = level.getGameTime();
    }

    public static void init(MinecraftServer server) {
        level = server.getLevel(PlotDimension.PLOTDIM);
    }

    /**
     * Mark this player as currently online.
     */
    public void online() {
        lastOnlineTick = level.getGameTime();
        isOnline = true;
    }

    /**
     * Mark this player as now offline.
     */
    public void offline() {
        lastOnlineTick = level.getGameTime();
        isOnline = false;
    }

    /**
     * Gets the total time the player has been offline for.
     * @return Elapsed time between now and lastOnlineTick
     */
    public long getTimeOffline() {
        if (isOnline) {
            return 0;
        }
        return level.getGameTime() - lastOnlineTick;
    }

    /**
     * Gets the last online tick of this player.
     * @return the last online tick (waow)
     */
    public long getLastOnlineTick() {
        return lastOnlineTick;
    }
}
