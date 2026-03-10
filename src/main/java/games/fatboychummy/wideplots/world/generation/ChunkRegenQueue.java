package games.fatboychummy.wideplots.world.generation;

import java.util.ArrayList;

public class ChunkRegenQueue {
    private static final ArrayList<ChunkRegenData> queue = new ArrayList<>();

    public static void addToQueue(ChunkRegenData data) {
        queue.add(data);
    }

    public static ChunkRegenData next() {
        if (queue.isEmpty()) {
            return null;
        }
        return queue.remove(0);
    }
}
