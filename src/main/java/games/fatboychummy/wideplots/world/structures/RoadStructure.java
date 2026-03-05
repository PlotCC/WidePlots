package games.fatboychummy.wideplots.world.structures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a road structure with metadata for generation.
 */
public class RoadStructure {
    private final ResourceLocation structureId;
    private final String shape; // "straight", "corner", "t_junction", etc.
    private final int width;
    private final int length;
    private final double genChance; // Probability of selecting this structure when applicable

    public RoadStructure(ResourceLocation structureId, CompoundTag roadData) {
        this.structureId = structureId;
        this.shape = roadData.getString("shape");
        this.width = roadData.getInt("width");
        this.length = roadData.getInt("length");
        this.genChance = roadData.getDouble("genChance");
    }

    public ResourceLocation getStructureId() {
        return structureId;
    }

    public String getShape() {
        return shape;
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public double getGenChance() {
        return genChance;
    }

    @Override
    public String toString() {
        return "RoadStructure{" +
                "id=" + structureId +
                ", shape='" + shape + '\'' +
                ", width=" + width +
                ", length=" + length +
                ", genChance=" + genChance +
                '}';
    }
}
