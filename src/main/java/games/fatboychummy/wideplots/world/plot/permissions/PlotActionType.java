package games.fatboychummy.wideplots.world.plot.permissions;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an action type that can be allowed or denied in a PlotPermissionSet.
 */
public enum PlotActionType implements StringRepresentable {
    BUILD,    // Allows building and breaking blocks.
    ACCESS,   // Allows opening chests, doors, etc.
    INTERACT, // Allows interacting with entities (e.g. right-clicking villagers).
    PVP,      // Allows player-vs-player combat.
    PVE,      // Allows player-vs-entity combat (i.e. Players vs animals, bosses, etc.)
    ENTER,    // Allows entering the plot (e.g. walking onto it).
    SET_HOME, // Allows setting a home location within the plot.
    SETTINGS, // Allows changing settings of the plot.
    PISTONS,  // Allows pistons to extend/retract blocks within the plot.
    ;

    @Override
    @NotNull
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
