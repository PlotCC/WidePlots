package games.fatboychummy.wideplots.world.plot.storage;

import games.fatboychummy.wideplots.block.ModBlocks;
import games.fatboychummy.wideplots.block.entity.PlotControllerBlockEntity;
import games.fatboychummy.wideplots.block.entity.events.WPSettingChangedEvent;
import games.fatboychummy.wideplots.util.PlotUtility;
import games.fatboychummy.wideplots.world.generation.PlotChunkGenerator;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

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

    // The plot controller block associated with this plot data.
    private PlotControllerBlockEntity controller;

    // The gamemode of visitors.
    private GameType visitorGameMode = GameType.CREATIVE;

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

    private void settingChange(String setting, String oldValue, String newValue) {
        if (controller != null) {
            controller.fireEvent(new WPSettingChangedEvent(
                    setting,
                    oldValue,
                    newValue
            ));
        }
    }

    public void setName(String name) {
        settingChange("name", this.name, name);
        this.name = name;
    }

    public void setDescription(String description) {
        settingChange("description", this.description, description);
        this.description = description;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        settingChange("welcome_message", this.welcomeMessage, welcomeMessage);
        this.welcomeMessage = welcomeMessage;
    }

    public void setDepartureMessage(String departureMessage) {
        settingChange("departure_message", this.departureMessage, departureMessage);
        this.departureMessage = departureMessage;
    }

    public void setVisitorSpawnOffset(BlockPos visitorSpawnOffset) {
        String oldName = "x:" + visitorSpawnOffset.getX() + " y:" + visitorSpawnOffset.getY() + " z:" + visitorSpawnOffset.getZ();
        String newName = "x:" + visitorSpawnOffset.getX() + " y:" + visitorSpawnOffset.getY() + " z:";

        settingChange("visitor_spawn", oldName, newName);
        this.visitorSpawnOffset = visitorSpawnOffset;
    }

    public void setVisitorGameMode(GameType gameMode) {
        settingChange("visitor_gamemode", this.visitorGameMode.name(), gameMode.name());
        this.visitorGameMode = gameMode;
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

    public BlockPos getPlotCenter() {
        int centerX = x * PlotUtility.CELL + PlotChunkGenerator.PLOT_SIZE / 2;
        int centerZ = z * PlotUtility.CELL + PlotChunkGenerator.PLOT_SIZE / 2;
        return new BlockPos(centerX, 0, centerZ);
    }

    public GameType getVisitorGameMode() {
        return visitorGameMode;
    }

    public @Nullable PlotControllerBlockEntity getPlotController() {
        if (controller == null) {
            return PlotPCHandler.getPlotController(x, z);
        }

        return controller;
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