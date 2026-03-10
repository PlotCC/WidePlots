package games.fatboychummy.wideplots.world.plot.storage;

public class SoftErrorState {
    private final String message;
    private final boolean isError;

    /**
     * Create a SoftErrorState with the error state and a default message. Most useful if returning no error.
     * @param isError Whether this SoftErrorState represents an error.
     */
    public SoftErrorState(boolean isError) {
        this.isError = isError;
        this.message = "An unknown error occurred";
    }

    /**
     * Create a SoftErrorState with the error state and a custom message.
     * @param isError Whether this SoftErrorState represents an error.
     * @param message The message to include with this SoftErrorState, which can be used to provide more information about the error if isError is true.
     */
    public SoftErrorState(boolean isError, String message) {
        this.isError = isError;
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isError() {
        return this.isError;
    }
}
