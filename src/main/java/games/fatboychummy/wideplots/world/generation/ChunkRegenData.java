package games.fatboychummy.wideplots.world.generation;

/**
 * Stores data about a chunk that needs regenerating.
 */
public record ChunkRegenData(int chunkX, int chunkZ, int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {}

