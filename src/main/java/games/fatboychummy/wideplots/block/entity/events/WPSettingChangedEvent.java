package games.fatboychummy.wideplots.block.entity.events;

public class WPSettingChangedEvent extends WPEvent {
    public static final String EVENT_ID = "setting_changed";

    public WPSettingChangedEvent(String settingKey, String oldValue, String newValue) {
        super(EVENT_ID, settingKey, oldValue, newValue);
    }
}
