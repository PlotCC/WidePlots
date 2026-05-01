package games.fatboychummy.wideplots.block.entity;

import games.fatboychummy.wideplots.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlotControllerBlockEntity extends EventBlockEntity {
    public static BlockEntityType<PlotControllerBlockEntity> PLOT_CONTROLLER_BLOCK_ENTITY_TYPE;
    private UUID ownerUUID;

    public void setOwner(UUID playerUUID) {
        this.ownerUUID = playerUUID;
    }

    @Nullable
    public UUID getOwner() {
        return ownerUUID;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        if (ownerUUID != null) {
            compoundTag.putUUID("ownerUUID", ownerUUID);
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.hasUUID("ownerUUID")) {
            ownerUUID = compoundTag.getUUID("ownerUUID");
        }
    }

    public PlotControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(PLOT_CONTROLLER_BLOCK_ENTITY_TYPE, blockPos, blockState);
    }

    public static void register() {
         PLOT_CONTROLLER_BLOCK_ENTITY_TYPE = WPBlockEntities.register("plot_controller", BlockEntityType.Builder.of(PlotControllerBlockEntity::new, ModBlocks.PLOT_CONTROLLER));
    }
}
