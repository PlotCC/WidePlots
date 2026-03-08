package games.fatboychummy.wideplots.world.player;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.world.PlotDimension;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



/**
 * Handles player spawning and teleportation to the plot dimension.
 */
public class PlotPlayerYeeter {

    // Store players that need to be teleported with a delay counter (using UUID to avoid duplicates)
    private static final Map<UUID, Integer> PENDING_TELEPORTS = new HashMap<>();
    private static final int TELEPORT_DELAY_TICKS = 1; // Minimal delay - just need to wait for respawn to complete

    public static void init() {
        WidePlots.LOGGER.info("PlotPlayerHandler initialized");

        // On entity load, check if the entity is a player then yeet them to the plot dimension.
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player && !level.dimension().equals(PlotDimension.PLOTDIM)) {
                WidePlots.LOGGER.info("Player {} loaded in dimension {}, scheduling teleport to plot dimension",
                    player.getName().getString(), level.dimension().location());

                ServerLevel plotLevel = PlotDimension.PLOTDIM_LEVEL;
                player.teleportTo(plotLevel, 0.5, 65.0, 0.5, player.getYRot(), player.getXRot());
            }
        });
    }
}



