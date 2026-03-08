package games.fatboychummy.wideplots.world.plot.permissions;

/**
 * Small enum that contains the three permission types.
 */
public enum PlotPermission {
    GRANT,     // Allows the specified player to perform the action.
    UNCHANGED, // Allows other permissions to determine this action's result, falling back to the default plot permissions if no specific permissions are set for the player.
    DENY,      // Denies the specified player from performing the action.
}
