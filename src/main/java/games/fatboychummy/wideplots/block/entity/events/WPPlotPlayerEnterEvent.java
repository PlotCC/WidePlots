package games.fatboychummy.wideplots.block.entity.events;

import net.minecraft.world.entity.player.Player;

public class WPPlotPlayerEnterEvent extends WPEvent {
    public static final String EVENT_ID = "plot_player_enter";

    public WPPlotPlayerEnterEvent(String playerUUID) {
        super(EVENT_ID, playerUUID);
    }

    public WPPlotPlayerEnterEvent(Player player) {
        super(EVENT_ID, player.getStringUUID());
    }
}
