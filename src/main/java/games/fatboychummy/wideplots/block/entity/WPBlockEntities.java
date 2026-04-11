package games.fatboychummy.wideplots.block.entity;

import com.mojang.datafixers.types.Type;
import games.fatboychummy.wideplots.WidePlots;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class WPBlockEntities {
    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, name);
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                WidePlots.id(name),
                builder.build(type)
        );
    }

    public static void registerModBlockEntities() {
        WidePlots.LOGGER.info("Registering Mod Block Entities for " + WidePlots.MOD_ID);

        PlotControllerBlockEntity.register();
    }
}
