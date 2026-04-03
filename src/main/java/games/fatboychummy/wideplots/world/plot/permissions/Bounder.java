package games.fatboychummy.wideplots.world.plot.permissions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class Bounder {
    private static final int BOUNDER_TIMEOUT = 20 * 30; // 30 seconds in ticks.

    // The first position of the bounder.
    private BlockPos pos1;

    // The second position of the bounder.
    private BlockPos pos2;

    private int timeLeft;

    /**
     * Creates a new bounder.
     */
    public Bounder() {
        timeLeft = BOUNDER_TIMEOUT;
    }

    /**
     * Sets the next position for this bounder.
     * If this is the second position being set, returns true. Otherwise, returns false.
     * @param pos The position to set for this bounder.
     * @return True if this is the second position being set, false otherwise.
     */
    public boolean setNextPos(BlockPos pos) {
        if (pos1 == null) {
            pos1 = pos;
            return false;
        }
        pos2 = pos;
        return true;
    }

    /**
     * Gets the first position of this bounder.
     * @return The first position of this bounder.
     */
    public BlockPos getPos1() {
        return pos1;
    }

    /**
     * Gets the second position of this bounder.
     * @return The second position of this bounder.
     */
    public BlockPos getPos2() {
        return pos2;
    }

    /**
     * Gets the bounding box defined by the two positions of this bounder. If either position is null, returns null.
     * @return The bounding box defined by the two positions of this bounder, or null if either position is null.
     */
    public BoundingBox getBoundingBox() {
        if (pos1 == null || pos2 == null) {
            return null;
        }
        return BoundingBox.fromCorners(pos1, pos2);
    }

    /**
     * Ticks this bounder, reducing the time left by one tick.
     * If the time left reaches zero, this bounder is considered expired and should be removed.
     * @return True if this bounder has expired and should be removed, false otherwise.
     */
    public boolean tick() {
        return --timeLeft <= 0;
    }
}
