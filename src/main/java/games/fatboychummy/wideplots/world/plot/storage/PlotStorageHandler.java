package games.fatboychummy.wideplots.world.plot.storage;

import com.mojang.authlib.GameProfile;
import games.fatboychummy.wideplots.util.PlotUtility;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private static final Map<Long, PlotStorage> activePlots = new HashMap<>();

    public static void init() {
        // TODO ???
        // TODO Probably load plot data from disk here once we get to that point.
    }

    /**
     * Claims a plot for the given player at the player's coordinates.
     * @param player The player claiming the plot.
     * @return (True, String) if the plot was successfully claimed, (False, String) if there was an error.
     */
    public static SoftErrorState claimPlot(Player player) {
        long plotKey = PlotUtility.keyFromCoords(player.getBlockX(), player.getBlockZ());

        if (activePlots.containsKey(plotKey)) {
            // Get player name from the UUID of the plot owner for the error message.
            MinecraftServer server = player.getServer();
            assert server != null; // This should never run in a client context.

            UUID ownerUUID = UUID.fromString(activePlots.get(plotKey).getOwnerUUID());
            ServerPlayer online = server.getPlayerList().getPlayer(ownerUUID);

            String ownerName;

            if (online != null) {
                ownerName = online.getName().getString();
            } else {
                GameProfile profile = server.getProfileCache().get(ownerUUID).orElse(null);
                ownerName = profile != null ? profile.getName() : Component.translatable("commands.wideplots.response.generic.unkown_player").getString();
            }

            return new SoftErrorState(true, Component.translatable("commands.wideplots.response.claim.already_claimed", ownerName));
        }

        if (!PlotUtility.isActuallyInBounds(player.blockPosition())) {
            return new SoftErrorState(true, Component.translatable("commands.wideplots.response.generic.not_in_plot"));
        }

        // Actually claim the plot.
        forceClaimPlot(player.getBlockX(), player.getBlockZ(), player.getStringUUID());
        return new SoftErrorState(false, Component.translatable("commands.wideplots.response.claim.success"));
    }

    /**
     * Unclaims a plot for the given player at the player's coordinates. Only the plot owner can unclaim their plot.
     * @param player The player unclaiming the plot.
     * @return (True, String) if the plot was successfully unclaimed, (False, String) if there was an error (e.g. plot not claimed, player does not own the plot, etc.).
     */
    public static SoftErrorState unclaimPlot(Player player) {
        long plotKey = PlotUtility.keyFromCoords(player.getBlockX(), player.getBlockZ());

        if (!activePlots.containsKey(plotKey)) {
            return new SoftErrorState(true, Component.translatable("commands.wideplots.response.unclaim.not_claimed"));
        }

        if (!activePlots.get(plotKey).getOwnerUUID().equals(player.getStringUUID())) {
            return new SoftErrorState(true, Component.translatable("commands.wideplots.response.unclaim.not_owner"));
        }

        if (!PlotUtility.isActuallyInBounds(player.blockPosition())) {
            return new SoftErrorState(true, Component.translatable("commands.wideplots.response.generic.not_in_plot"));
        }

        // Actually unclaim the plot.
        forceUnclaimPlot(player.getBlockX(), player.getBlockZ());
        return new SoftErrorState(false, Component.translatable("commands.wideplots.response.unclaim.success"));
    }

    /**
     * Overload to get the plot at a player's coordinates.
     * @param player The player whose position we should grab the plot for.
     * @return The PlotStorage object for the plot at the player's coordinates, or null if the plot is not claimed.
     */
    public static PlotStorage getPlot(Player player) {
        return getPlot(player.getBlockX(), player.getBlockZ());
    }

    /**
     * Get a plot by its coordinates. Returns null if the plot is not claimed.
     * @param x The x coordinate of the plot to get.
     * @param z The z coordinate of the plot to get.
     * @return The PlotStorage object for the plot at the given coordinates, or null if the plot is not claimed.
     */
    public static PlotStorage getPlot(int x, int z) {
        return activePlots.get(PlotUtility.keyFromCoords(x, z));
    }

    /**
     * Forcefully unclaims a plot at the given coordinates, without checking for permissions or ownership.
     * Used for admin commands/debug. Used under-the-hood by unclaimPlot after checking permissions and ownership.
     * @param x The x coordinate of the plot to unclaim.
     * @param z The z coordinate of the plot to unclaim.
     */
    public static void forceUnclaimPlot(int x, int z) {
        long plotKey = PlotUtility.keyFromCoords(x, z);
        PlotPermissionHandler.remove(plotKey);
        activePlots.remove(plotKey);
    }

    /**
     * Forcefully claims a plot at the given coordinates for the given player, without checking for permissions or ownership.
     * Used for admin commands/debug. Used under-the-hood by claimPlot after checking permissions
     * @param x The x coordinate of the plot to claim.
     * @param z The z coordinate of the plot to claim.
     * @param ownerUUID The UUID of the player to claim the plot for.
     */
    public static void forceClaimPlot(int x, int z, String ownerUUID) {
        long plotKey = PlotUtility.keyFromCoords(x, z);

        if (activePlots.containsKey(plotKey)) {
            forceUnclaimPlot(x, z);
        }

        Tuple<Integer, Integer> plotCoords = PlotUtility.plotCoordsFromWorldCoords(x, z);
        PlotStorage storage = new PlotStorage(ownerUUID, plotCoords.getA(), plotCoords.getB());
        activePlots.put(plotKey, storage);
        if (!PlotPermissionHandler.register(plotKey, storage.getPermissions())) {
            PlotPermissionHandler.remove(plotKey);

            // If we fail again, just assume the world is ending or something.
            if (!PlotPermissionHandler.register(plotKey, storage.getPermissions())) {
                activePlots.remove(plotKey);
                throw new RuntimeException("Failed to register plot permissions for plot at (" + plotCoords.getA() + ", " + plotCoords.getB() + ") after claiming.");
            }
        }
    }
}
