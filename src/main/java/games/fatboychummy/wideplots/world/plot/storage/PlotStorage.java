package games.fatboychummy.wideplots.world.plot.storage;

import games.fatboychummy.wideplots.util.PlotUtility;
import games.fatboychummy.wideplots.world.generation.PlotChunkGenerator;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

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

    // The plot's name as given by the player.
    private String name;

    // The plot's description as given by the player.
    private String description;

    // The plot's welcome message, shown to players when they enter the plot.
    private String welcomeMessage;

    // The plot's departure message, shown to players when they leave the plot.
    private String departureMessage;

    public PlotStorage(String ownerUUID, int x, int z) {
        this.ownerUUID = ownerUUID;
        this.x = x;
        this.z = z;
        this.permissions = new PlotPermissions(ownerUUID);

        this.name = "Unnamed Plot";
        this.description = "";
        this.welcomeMessage = "";
        this.departureMessage = "";
    }

    // Getters for the plot data.
    public String getOwnerUUID() {
        return ownerUUID;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public void setDepartureMessage(String departureMessage) {
        this.departureMessage = departureMessage;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getDepartureMessage() {
        return departureMessage;
    }

    public PlotPermissions getPermissions() {
        return permissions;
    }

    /**
     * Wipes the plot, reverting it back to its original state.
     */
    public void wipe(MinecraftServer server) {
        int minBlockX = x * PlotUtility.CELL;
        int minBlockZ = z * PlotUtility.CELL;
        int maxBlockX = minBlockX + PlotChunkGenerator.PLOT_SIZE - 1;
        int maxBlockZ = minBlockZ + PlotChunkGenerator.PLOT_SIZE - 1;

        // Determine the chunks we need to regenerate.
        int minChunkX = minBlockX >> 4;
        int minChunkZ = minBlockZ >> 4;
        int maxChunkX = maxBlockX >> 4;
        int maxChunkZ = maxBlockZ >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                PlotChunkGenerator.queueRegenerateChunk(chunkX, chunkZ, minBlockX, minBlockZ, maxBlockX, maxBlockZ);
            }
        }
    }
}
