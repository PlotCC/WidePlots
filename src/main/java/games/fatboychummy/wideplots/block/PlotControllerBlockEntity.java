package games.fatboychummy.wideplots.block;

import com.mojang.datafixers.types.Type;
import games.fatboychummy.wideplots.WidePlots;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PlotControllerBlockEntity extends BlockEntity {
    public static final BlockEntityType<PlotControllerBlockEntity> PLOT_CONTROLLER_BLOCK_ENTITY_TYPE = register("plot_controller", BlockEntityType.Builder.of(PlotControllerBlockEntity::new, ModBlocks.PLOT_CONTROLLER));

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, name);
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                WidePlots.id(name),
                builder.build(type)
        );
    }

    public PlotControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(PLOT_CONTROLLER_BLOCK_ENTITY_TYPE, blockPos, blockState);
    }
}
