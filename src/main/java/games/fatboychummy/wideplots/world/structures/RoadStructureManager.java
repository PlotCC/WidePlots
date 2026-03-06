package games.fatboychummy.wideplots.world.structures;

import games.fatboychummy.wideplots.WidePlots;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Manages loading and selecting road structures with weighted random selection.
 */
public class RoadStructureManager {
    private final Map<String, List<RoadStructure>> straightRoads; // Keyed by road type ('basic', 'path', 'modern', etc)
    private final Map<String, List<RoadStructure>> fourWayJunctions; // Keyed by junction type ('basic', 'path', 'modern', etc)
    private final Map<String, List<RoadStructure>> tJunctions; // Keyed by junction type ('basic', 'path', 'modern', etc)
    private final Map<String, List<RoadStructure>> cornerJunctions; // Keyed by corner type ('basic', 'path', 'modern', etc)
    private final Map<String, List<RoadStructure>> straightJunctions; // Keyed by straight junction type ('basic', 'path', 'modern', etc)
    private final Random random;

    public RoadStructureManager() {
        this.straightRoads = new HashMap<>();
        this.fourWayJunctions = new HashMap<>();
        this.tJunctions = new HashMap<>();
        this.cornerJunctions = new HashMap<>();
        this.straightJunctions = new HashMap<>();
        this.random = new Random();
    }

    /**
     * Manually register a road structure.
     */
    public void registerStructure(ResourceLocation structureId, CompoundTag roadData, String shape, String type) {
        RoadStructure roadStruct = new RoadStructure(structureId, roadData);

        switch (shape) {
            case "straight" -> {
                straightRoads.computeIfAbsent(type, k -> new ArrayList<>()).add(roadStruct);
                WidePlots.LOGGER.info("Registered straight road structure of type '{}': {}", type, roadStruct);
            }
            case "four_way" -> {
                fourWayJunctions.computeIfAbsent(type, k -> new ArrayList<>()).add(roadStruct);
                WidePlots.LOGGER.info("Registered four-way junction structure of type '{}': {}", type, roadStruct);
            }
            case "t_junction" -> {
                tJunctions.computeIfAbsent(type, k -> new ArrayList<>()).add(roadStruct);
                WidePlots.LOGGER.info("Registered T-junction structure of type '{}': {}", type, roadStruct);
            }
            case "corner" -> {
                cornerJunctions.computeIfAbsent(type, k -> new ArrayList<>()).add(roadStruct);
                WidePlots.LOGGER.info("Registered corner junction structure of type '{}': {}", type, roadStruct);
            }
            case "straight_junction" -> {
                straightJunctions.computeIfAbsent(type, k -> new ArrayList<>()).add(roadStruct);
                WidePlots.LOGGER.info("Registered straight junction structure of type '{}': {}", type, roadStruct);
            }
            default -> WidePlots.LOGGER.warn("Unknown road shape '{}' for structure '{}' (type: {})", shape, structureId, type);
        }
    }

    public void debugOutputStructures() {
        WidePlots.LOGGER.info("Current registered road structures:");

        WidePlots.LOGGER.info("  Straight Roads:");
        straightRoads.forEach((type, list) -> {
            list.forEach(struct -> WidePlots.LOGGER.info("    - Type: {}, Structure: {}", type, struct.getStructureId()));
        });

        WidePlots.LOGGER.info("  Four-Way Junctions:");
        fourWayJunctions.forEach((type, list) -> {
            list.forEach(struct -> WidePlots.LOGGER.info("    - Type: {}, Structure: {}", type, struct.getStructureId()));
        });

        WidePlots.LOGGER.info("  T-Junctions:");
        tJunctions.forEach((type, list) -> {
            list.forEach(struct -> WidePlots.LOGGER.info("    - Type: {}, Structure: {}", type, struct.getStructureId()));
        });

        WidePlots.LOGGER.info("  Corner Junctions:");
        cornerJunctions.forEach((type, list) -> {
            list.forEach(struct -> WidePlots.LOGGER.info("    - Type: {}, Structure: {}", type, struct.getStructureId()));
        });

        WidePlots.LOGGER.info("  Straight Junctions:");
        straightJunctions.forEach((type, list) -> {
            list.forEach(struct -> WidePlots.LOGGER.info("    - Type: {}, Structure: {}", type, struct.getStructureId()));
        });
    }

    /**
     * Selects a random structure from the given list of candidates based on their genChance weights.
     * @param candidates List of candidate structures to select from.
     * @return A randomly selected RoadStructure based on weighted probabilities, or null if no candidates are available or all have zero weight.
     */
    public RoadStructure selectStructure(List<RoadStructure> candidates) {
        Objects.requireNonNull(candidates);

        if (candidates.isEmpty()) {
            WidePlots.LOGGER.warn("No candidate structures available for selection");
            return null;
        }

        // Calculate total weight
        double totalWeight = candidates.stream()
                .mapToDouble(RoadStructure::getGenChance)
                .sum();

        if (totalWeight <= 0) {
            return null;
        }

        // Select based on weighted random
        double selection = random.nextDouble() * totalWeight;
        double accumulated = 0;

        for (RoadStructure structure : candidates) {
            accumulated += structure.getGenChance();
            if (selection <= accumulated) {
                return structure;
            }
        }

        // Fallback (shouldn't reach here)
        return candidates.get(candidates.size() - 1);
    }

    /**
     * Convenience method to select a structure directly by shape and type.
     * @see RoadStructureManager#selectStructure(List)
     * @param shape The shape of the structure (e.g., "straight", "four_way", "t_junction", "corner", "straight_junction").
     * @param type The type of the structure (e.g., "basic", "path", "modern").
     * @return A randomly selected RoadStructure matching the given shape and type, or null if no candidates are available or all have zero weight.
     */
    public RoadStructure selectStructure(String shape, String type) {
        WidePlots.LOGGER.info("Selecting structure for shape '{}' and type '{}'", shape, type); //temp
        return selectStructure(getStructures(shape, type));
    }

    /**
     * Retrieves the list of structures for a given shape and type.
     * @param shape The shape of the structure (e.g., "straight", "four_way", "t_junction", "corner", "straight_junction").
     * @param type The type of the structure (e.g., "basic", "path", "modern").
     * @return A list of RoadStructures matching the given shape and type, or an empty list if none are found.
     */
    public List<RoadStructure> getStructures(String shape, String type) {
        WidePlots.LOGGER.info("Retrieving structures for shape '{}' and type '{}'", shape, type); //temp
        return switch (shape) {
            case "straight" -> straightRoads.getOrDefault(type, Collections.emptyList());
            case "four_way" -> fourWayJunctions.getOrDefault(type, Collections.emptyList());
            case "t_junction" -> tJunctions.getOrDefault(type, Collections.emptyList());
            case "corner" -> cornerJunctions.getOrDefault(type, Collections.emptyList());
            case "straight_junction" -> straightJunctions.getOrDefault(type, Collections.emptyList());
            default -> Collections.emptyList();
        };
    }


    /**
     * Seeds the structure random based on the chunk given, and the direction of placement.
     * Seeds like so:
     * seed = (chunkX * 341873128712L + chunkZ * 132897987541L) XOR (facingX ? 0 : 1)
     */
    public void randomSeed(int chunkX, int chunkZ, boolean facingX) {
        random.setSeed((chunkX * 341873128712L + chunkZ * 132897987541L) ^ (facingX ? 0 : 1));
    }

    /**
     * Clears all registered structures from the manager. Useful for reloading or resetting state.
     */
    public void clear() {
        straightRoads.clear();
        fourWayJunctions.clear();
        tJunctions.clear();
        cornerJunctions.clear();
        straightJunctions.clear();
    }
}

