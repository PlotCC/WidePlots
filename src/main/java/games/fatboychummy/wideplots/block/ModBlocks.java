package games.fatboychummy.wideplots.block;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.item.BoundingToolItem;
import games.fatboychummy.wideplots.item.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static final Block PLOT_CONTROLLER = registerBlock(
            "plot_controller",
            new PlotControllerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK))
    );

    private static Block registerBlock(String name, Block block) {
        return Registry.register(
                BuiltInRegistries.BLOCK,
                WidePlots.id(name),
                block
        );
    }

    public static void registerModBlocks() {
        WidePlots.LOGGER.info("Registering Mod Blocks for " + WidePlots.MOD_ID);
    }
}
