package games.fatboychummy.wideplots.world.plot.storage;

import games.fatboychummy.wideplots.util.PlotUtility;
import games.fatboychummy.wideplots.world.generation.PlotChunkGenerator;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;

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

    // The name's maximum length.
    public static final int NAME_MAX_LENGTH = 32;

    // The plot's description as given by the player.
    private String description;

    // The plot description's maximum length.
    public static final int DESCRIPTION_MAX_LENGTH = 256;

    // The plot's welcome message, shown to players when they enter the plot.
    private String welcomeMessage;

    // The welcome message's maximum length.
    public static final int WELCOME_MESSAGE_MAX_LENGTH = 64;

    // The plot's departure message, shown to players when they leave the plot.
    private String departureMessage;

    // The departure message's maximum length.
    public static final int DEPARTURE_MESSAGE_MAX_LENGTH = 64;

    // When players run `/plot visit <player>`, they will be teleported to the center of the plot with this offset.
    private BlockPos visitorSpawnOffset = new BlockPos(0, 32, 0);

    GameType visitorGameMode = GameType.CREATIVE;

    public PlotStorage(String ownerUUID, int x, int z) {
        this.ownerUUID = ownerUUID;
        this.x = x;
        this.z = z;
        this.permissions = new PlotPermissions(ownerUUID);

        this.name = "Unnamed Plot";
        this.description = "No plot description.";
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

    public BlockPos getVisitorSpawnOffset() {
        return visitorSpawnOffset;
    }

    public void setVisitorSpawnOffset(BlockPos visitorSpawnOffset) {
        this.visitorSpawnOffset = visitorSpawnOffset;
    }

    public BlockPos getPlotCenter() {
        int centerX = x * PlotUtility.CELL + PlotChunkGenerator.PLOT_SIZE / 2;
        int centerZ = z * PlotUtility.CELL + PlotChunkGenerator.PLOT_SIZE / 2;
        return new BlockPos(centerX, 0, centerZ);
    }

    public void setVisitorGameMode(GameType gameMode) {
        this.visitorGameMode = gameMode;
    }

    public GameType getVisitorGameMode() {
        return visitorGameMode;
    }

    /**
     * Wipes the plot, reverting it back to its original state.
     */
    public void wipe() {
        int minBlockX = x * PlotUtility.CELL + PlotChunkGenerator.ROAD_WIDTH;
        int minBlockZ = z * PlotUtility.CELL + PlotChunkGenerator.ROAD_WIDTH;
        int maxBlockX = minBlockX + PlotChunkGenerator.PLOT_SIZE - 1;
        int maxBlockZ = minBlockZ + PlotChunkGenerator.PLOT_SIZE - 1;

        // Determine the chunks to wipe
        int minChunkX = minBlockX >> 4;
        int minChunkZ = minBlockZ >> 4;
        int maxChunkX = maxBlockX >> 4;
        int maxChunkZ = maxBlockZ >> 4;

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                PlotChunkGenerator.queueRegenerateChunk(x, z, minBlockX, minBlockZ, maxBlockX, maxBlockZ);
            }
        }
    }
}

/**
 * [20:18:45] [Server thread/INFO] (WidePlots) Regenerating chunk at 0,0 for plot changes within 0,0, 47,47
 * [20:18:45] [Server thread/INFO] (WidePlots) Regenerating chunk at 0,1 for plot changes within 0,0, 47,47
 * [20:18:45] [Server thread/INFO] (WidePlots) Regenerating chunk at 0,2 for plot changes within 0,0, 47,47
 * [20:18:46] [Server thread/INFO] (WidePlots) Regenerating chunk at 1,0 for plot changes within 0,0, 47,47
 * [20:18:46] [Server thread/INFO] (WidePlots) Regenerating chunk at 1,1 for plot changes within 0,0, 47,47
 * [20:18:46] [Server thread/INFO] (WidePlots) Regenerating chunk at 1,2 for plot changes within 0,0, 47,47
 * [20:18:46] [Server thread/INFO] (WidePlots) Regenerating chunk at 2,0 for plot changes within 0,0, 47,47
 * [20:18:47] [Server thread/INFO] (WidePlots) Regenerating chunk at 2,1 for plot changes within 0,0, 47,47
 * [20:18:47] [Server thread/INFO] (WidePlots) Regenerating chunk at 2,2 for plot changes within 0,0, 47,47
 *
 * Should be within 8,8 to 55,55
 * Should regen chunks 0,0 to 3,3
 */
