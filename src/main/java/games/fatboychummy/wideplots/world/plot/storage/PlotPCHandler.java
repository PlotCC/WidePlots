package games.fatboychummy.wideplots.world.plot.storage;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.block.entity.PlotControllerBlockEntity;
import games.fatboychummy.wideplots.util.PlotUtility;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlotPCHandler {
    private static final Map<Long, PlotControllerBlockEntity> plotControllers = new ConcurrentHashMap<>();

    /**
     * Get the plot controller for a given plot.
     * @param x The Plot Coordinate X
     * @param z The Plot Coordinate Z
     * @return The plot controller, or null if none exists in the plot.
     */
    public static @Nullable PlotControllerBlockEntity getPlotController(int x, int z) {
        return getPlotController(PlotUtility.key(x, z));
    }

    public static @Nullable PlotControllerBlockEntity getPlotController(long key) {
        return plotControllers.get(key);
    }

    /**
     * Checks if a plot controller is in the given plot.
     * @param x The Plot Coordinate X
     * @param z The Plot Coordinate Z
     * @return whether there is a plot controller already in the plot.
     */
    public static boolean hasPlotController(int x, int z) {
        return hasPlotController(PlotUtility.key(x, z));
    }

    public static boolean hasPlotController(long key) {
        WidePlots.LOGGER.info("Check for plot controller: {}", key);
        return plotControllers.containsKey(key);
    }

    /**
     * Attempts to set a plot controller into the plot. If a plot controller already exists, returns false (failure).
     * @param x The Plot Coordinate X
     * @param z The Plot Coordinate Z
     * @return Whether insertion was successful.
     */
    public static boolean setPlotController(int x, int z, PlotControllerBlockEntity plotController) {
        return setPlotController(PlotUtility.key(x, z), plotController);
    }

    public static boolean setPlotController(long key, PlotControllerBlockEntity plotController) {
        if (hasPlotController(key)) {
            return false;
        }

        WidePlots.LOGGER.info("Setting plot controller at {}", key);
        plotControllers.put(key, plotController);
        return true;
    }

    /**
     * Removes the tracked plot controller for a given plot coordinate
     * @param x The Plot Coordinate X
     * @param z The Plot Coordinate Z
     */
    public static void removePlotController(int x, int z) {
        removePlotController(PlotUtility.key(x, z));
    }

    public static void removePlotController(long key) {
        WidePlots.LOGGER.info("Removing plot controller {}", key);
        plotControllers.remove(key);
    }

    public static void removePlotController(PlotControllerBlockEntity plotController) {
        // Find then remove the plot controller.
        for (Map.Entry<Long, PlotControllerBlockEntity> entry : plotControllers.entrySet()) {
            if (entry.getValue().equals(plotController)) {
                removePlotController(entry.getKey());
                return;
            }
        }
    }
}
