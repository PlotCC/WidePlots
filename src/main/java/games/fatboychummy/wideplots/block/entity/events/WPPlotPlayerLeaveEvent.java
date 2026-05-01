package games.fatboychummy.wideplots.block.entity.events;

import net.minecraft.world.entity.player.Player;

public class WPPlotPlayerLeaveEvent extends WPEvent {
    public static final String EVENT_ID = "plot_player_leave";

    public WPPlotPlayerLeaveEvent(String playerUUID) {
        super(EVENT_ID, playerUUID);
    }

    public WPPlotPlayerLeaveEvent(Player player) {
        super(EVENT_ID, player.getStringUUID());
    }
}
