package games.fatboychummy.wideplots.block.entity.events;

public class WPWipeEvent extends WPEvent {
    public static final String EVENT_ID = "plot_wipe";

    public WPWipeEvent(String issuerUUID) {
        super(EVENT_ID, issuerUUID);
    }
}
