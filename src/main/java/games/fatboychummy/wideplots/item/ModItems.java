package games.fatboychummy.wideplots.item;


import games.fatboychummy.wideplots.WidePlots;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final Item BOUNDING_TOOL = registerItem(
            BoundingToolItem.NAME,
            new BoundingToolItem(
                    new Item.Properties().stacksTo(1)
            )
    );

    public static final CreativeModeTab WIDE_PLOTS_ITEM_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            WidePlots.id("wide_plots_item_group"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(BOUNDING_TOOL::getDefaultInstance)
                    .displayItems(ModItems::addItemsToItemGroup)
                    .build()
    );

    private static void addItemsToItemGroup(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
        output.accept(BOUNDING_TOOL);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                WidePlots.id(name),
                item
        );
    }

    public static void registerModItems() {
        WidePlots.LOGGER.info("Registering Mod Items for " + WidePlots.MOD_ID);
        // TODO: Need to datagen stuff for this item.


    }
}
