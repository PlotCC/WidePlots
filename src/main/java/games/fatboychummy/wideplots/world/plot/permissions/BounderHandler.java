package games.fatboychummy.wideplots.world.plot.permissions;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class BounderHandler {
    private static final Map<String, Bounder> bounders = new HashMap<>();

    public static void init() {
        // Register tick handler to delete stale bounders.
        ServerTickEvents.END_SERVER_TICK.register(BounderHandler::tick);
    }

    public static void tick(MinecraftServer server) {
        for (String playerUUID : bounders.keySet()) {
            Bounder bounder = bounders.get(playerUUID);
            if (bounder.tick()) {
                bounders.remove(playerUUID);
            }
        }
    }

    /**
     * "hits" a bounder. If the bounder is complete, returns it.
     * @param playerUUID The UUID of the player hitting the bounder.
     * @param pos The position of the block being hit.
     * @return The bounder if it is complete, or null if it is not complete.
     */
    public static Bounder hit(String playerUUID, BlockPos pos) {
        Bounder bounder = bounders.computeIfAbsent(playerUUID, k -> new Bounder());

        if (bounder.setNextPos(pos)) {
            bounders.remove(playerUUID);
            return bounder;
        }

        return null;
    }

    /**
     * Removes a bounder for a player. Used when a player cancels the bounding process or disconnects.
     * @param playerUUID The UUID of the player whose bounder should be removed.
     */
    public static void removeBounder(String playerUUID) {
        bounders.remove(playerUUID);
    }
}
