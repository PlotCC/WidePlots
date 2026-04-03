package games.fatboychummy.wideplots.world.plot.storage;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SoftErrorState {
    private final MutableComponent message;
    private final boolean isError;

    /**
     * Create a SoftErrorState with the error state and a default message. Most useful if returning no error.
     * @param isError Whether this SoftErrorState represents an error.
     */
    public SoftErrorState(boolean isError) {
        this.isError = isError;
        this.message = Component.translatable("command.response.default_error");
    }

    /**
     * Create a SoftErrorState with the error state and a custom message.
     * @param isError Whether this SoftErrorState represents an error.
     * @param component The message to include with this SoftErrorState, which can be used to provide more information about the error if isError is true.
     */
    public SoftErrorState(boolean isError, MutableComponent component) {
        this.isError = isError;
        this.message = component;
    }

    public MutableComponent getMessage() {
        return this.message;
    }

    public boolean isError() {
        return this.isError;
    }
}
