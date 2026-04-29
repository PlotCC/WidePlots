package games.fatboychummy.wideplots.integrations.cc.tweaked.event;

public class ComputerEvent {
    public String eventName;
    public Object[] args;

    public ComputerEvent(String eventName) {
        this.eventName = eventName;
    }

    public ComputerEvent(String eventName, Object[] args) {
        this.eventName = eventName;
        this.args = args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
