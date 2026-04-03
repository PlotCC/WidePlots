package games.fatboychummy.wideplots.util;

import games.fatboychummy.wideplots.WidePlots;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class TimedRequestHelper {
    private static Map<String, Map<String, TimedRequest>> requests = new HashMap<>();

    public static void tick(MinecraftServer server) {
        for (Map.Entry<String, Map<String, TimedRequest>> mapEntry : requests.entrySet()) {
            Map<String, TimedRequest> requestMap = mapEntry.getValue();

            requestMap.entrySet().removeIf(request -> request.getValue().tick());
        }
    }

    public static void registerTimedRequestCommand(String name) {
        requests.putIfAbsent(name, new HashMap<>());
    }

    public static void timedRequest(String name, String playerUUID, TimedRequest request) {
        if (!requests.containsKey(name)) {
            WidePlots.LOGGER.warn("Attempted to create TimedRequest of command '{}' which was not registered.", name);
        }

        requests.get(name).putIfAbsent(playerUUID, request);
    }

    public static boolean isAlive(String name, String playerUUID) {
        if (!requests.containsKey(name)) {
            WidePlots.LOGGER.warn("Attempted to get TimedRequest of command '{}' which was not registered.", name);
            return false;
        }

        return requests.get(name).containsKey(playerUUID);
    }

    public static void cancel(String name, String playerUUID) {
        if (!requests.containsKey(name)) {
            WidePlots.LOGGER.warn("Attempted to remove TimedRequest of command '{}' which was not registered.", name);
            return;
        }

        requests.get(name).remove(playerUUID);
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(CommandUtil::tick);
    }
}
