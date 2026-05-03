package games.fatboychummy.wideplots.block.entity.events;

public class WPSettingChangedEvent extends WPEvent {
    public static final String EVENT_ID = "plot_setting_changed";

    public WPSettingChangedEvent(String playerUUID, String settingKey, String oldValue, String newValue) {
        super(EVENT_ID, playerUUID, settingKey, oldValue, newValue);
    }
}
