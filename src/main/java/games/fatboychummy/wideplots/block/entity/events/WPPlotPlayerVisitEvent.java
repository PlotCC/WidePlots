package games.fatboychummy.wideplots.block.entity.events;

import net.minecraft.world.entity.player.Player;

public class WPPlotPlayerVisitEvent extends WPEvent {
    public static final String EVENT_ID = "plot_player_visit";

    public WPPlotPlayerVisitEvent(String playerUUID) {
        super(EVENT_ID, playerUUID);
    }

    public WPPlotPlayerVisitEvent(Player player) {
        super(EVENT_ID, player.getStringUUID());
    }
}
