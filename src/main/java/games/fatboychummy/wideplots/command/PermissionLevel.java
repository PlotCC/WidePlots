package games.fatboychummy.wideplots.command;

/**
 * Minecraft permission levels.
 */
public enum PermissionLevel {
    /**
     * Permission level 0: No special permissions.
     */
    ALL(0),

    /**
     * Permission level 1: Bypass spawn protection
     */
    MOD(1),

    /**
     * Permission level 2: GameMaster, allows access to more commands and command blocks.
     */
    GM(2),

    /**
     * Permission level 3: Commands related to multiplayer management become available.
     */
    ADMIN(3),

    /**
     * Permission level 4: Full permissions, including access to all commands and command blocks.
     */
    OWNER(4);

    private final int level;
    PermissionLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }
}
