package games.fatboychummy.wideplots.world.player;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WPPlayerHandler {
    private static Map<String, PlotPlayerStorage> storage;

    public static void init() {
        storage = new HashMap<>();
    }

    public static PlotPlayerStorage registerPlayer(String uuid) {
        // TODO: Change this to save some data on disk or something.

        return storage.computeIfAbsent(uuid, k -> new PlotPlayerStorage(uuid));
    }

    public static void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = handler.player;
        PlotPlayerStorage store = registerPlayer(player.getStringUUID());

        store.online();
    }

    public static void onLeave(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        ServerPlayer player = handler.player;
        PlotPlayerStorage store = registerPlayer(player.getStringUUID());

        store.offline();
    }

    @Nullable
    public static PlotPlayerStorage getPlayer(String uuid) {
        return storage.get(uuid);
    }
}
