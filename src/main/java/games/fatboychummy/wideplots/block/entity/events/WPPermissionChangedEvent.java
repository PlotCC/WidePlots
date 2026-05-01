package games.fatboychummy.wideplots.block.entity.events;

public class WPPermissionChangedEvent extends WPEvent {
    public static final String EVENT_ID = "permission_changed";

    public WPPermissionChangedEvent(String permissionKey, String oldValue, String newValue) {
        super(EVENT_ID, permissionKey, oldValue, newValue);
    }
}
