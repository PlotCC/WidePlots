package games.fatboychummy.wideplots.util;

public class TimedRequest {
    private int ticksToLive;
    private final Runnable onExpire;

    public TimedRequest(int ticksToLive) {
        this.ticksToLive = ticksToLive;
        onExpire = null;
    }

    public TimedRequest(int ticksToLive, Runnable onExpire) {
        this.ticksToLive = ticksToLive;
        this.onExpire = onExpire;
    }

    /**
     * Ticks the request. If the request is at or below zero, returns true (request expired).
     * @return Whether the request is expired.
     */
    public boolean tick() {
        if (--ticksToLive <= 0) {
            if (this.onExpire != null) {
                this.onExpire.run(); // This could technically get spammed if we aren't removing requests properly...
            }
            return true;
        }
        return false;
    }
}
