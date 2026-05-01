package games.fatboychummy.wideplots.util;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.world.generation.PlotChunkGenerator;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermission;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class PlotUtility {
    public static final int CELL = PlotChunkGenerator.PLOT_SIZE + PlotChunkGenerator.ROAD_WIDTH;

    /**
     * Gets the unique key for the given plot coordinates (x, z) to store in the activePlots map.
     * @param x The x coordinate of the plot.
     * @param z The z coordinate of the plot.
     * @return A unique long key representing the plot coordinates (x, z).
     */
    public static long key(int x, int z) {
        return ((long)x << 32) ^ (z & 0xffffffffL);
    }

    /**
     * Converts world coordinates (x, z) to plot coordinates and returns the unique key for the plot at those coordinates.
     * @param x The x coordinate in the world.
     * @param z The z coordinate in the world.
     * @return A unique long key representing the plot coordinates (x, z) corresponding to the given world coordinates.
     */
    public static long keyFromCoords(int x, int z) {
        return key(Math.floorDiv(x, CELL), Math.floorDiv(z, CELL));
    }

    public static Tuple<Integer, Integer> plotCoordsFromWorldCoords(int x, int z) {
        return new Tuple<>(Math.floorDiv(x, CELL), Math.floorDiv(z, CELL));
    }

    /**
     * Checks if a BlockPos is actually within the bounds of a plot.
     * @param pos The position to check.
     * @return Whether the position is actually within a plot.
     */
    public static boolean isActuallyInBounds(BlockPos pos) {
        return Math.floorMod(pos.getX(), CELL) >= PlotChunkGenerator.ROAD_WIDTH &&
                Math.floorMod(pos.getZ(), CELL) >= PlotChunkGenerator.ROAD_WIDTH;
    }

    /**
     * Gets a bounding box of the plot at a given plot coordinate.
     */
    public static BoundingBox getPlotBoundingBox(Tuple<Integer, Integer> coordinate) {
        int px = coordinate.getA();
        int pz = coordinate.getB();

        int baseX = px * CELL;
        int baseZ = pz * CELL;

        int minX = baseX + PlotChunkGenerator.ROAD_WIDTH;
        int minZ = baseZ + PlotChunkGenerator.ROAD_WIDTH;

        int maxX = minX + PlotChunkGenerator.PLOT_SIZE - 1;
        int maxZ = minZ + PlotChunkGenerator.PLOT_SIZE - 1;

        int minY = -64;
        int maxY = 384;

        return new BoundingBox(
            minX, minY, maxX, maxY, minZ, maxZ
        );
    }
}
