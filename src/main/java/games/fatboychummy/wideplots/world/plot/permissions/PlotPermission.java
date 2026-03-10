package games.fatboychummy.wideplots.world.plot.permissions;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Small enum that contains the three permission types.
 */
public enum PlotPermission implements StringRepresentable {
    GRANT,     // Allows the specified player to perform the action.
    UNCHANGED, // Allows other permissions to determine this action's result, falling back to the default plot permissions if no specific permissions are set for the player.
    DENY,      // Denies the specified player from performing the action.
    ;

    @Override
    @NotNull
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
