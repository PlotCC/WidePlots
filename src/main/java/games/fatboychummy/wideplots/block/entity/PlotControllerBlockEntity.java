package games.fatboychummy.wideplots.block.entity;

import games.fatboychummy.wideplots.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PlotControllerBlockEntity extends BlockEntity {
    public static BlockEntityType<PlotControllerBlockEntity> PLOT_CONTROLLER_BLOCK_ENTITY_TYPE;

    public PlotControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(PLOT_CONTROLLER_BLOCK_ENTITY_TYPE, blockPos, blockState);
    }

    public static void register() {
         PLOT_CONTROLLER_BLOCK_ENTITY_TYPE = WPBlockEntities.register("plot_controller", BlockEntityType.Builder.of(PlotControllerBlockEntity::new, ModBlocks.PLOT_CONTROLLER));
    }


}
