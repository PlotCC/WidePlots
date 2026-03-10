package games.fatboychummy.wideplots.util;

import games.fatboychummy.wideplots.world.generation.PlotChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;

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
}
