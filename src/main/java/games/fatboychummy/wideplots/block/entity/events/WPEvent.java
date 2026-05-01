package games.fatboychummy.wideplots.block.entity.events;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WPEvent {
    private final @NotNull String eventName;
    private final Object[] args;
    public WPEvent(@NotNull String eventName, Object... args) {
        this.eventName = eventName;
        this.args = args;
    }

    public @Nullable Object getArg(int i) {
        if (i >= 0 && i < this.args.length) {
            return args[i];
        }
        return null;
    }

    public @NotNull String getEventName() {
        return eventName;
    }
}
