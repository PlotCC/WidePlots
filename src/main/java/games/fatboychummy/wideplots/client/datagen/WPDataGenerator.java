package games.fatboychummy.wideplots.client.datagen;

import games.fatboychummy.wideplots.world.PlotDimension;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class WPDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // Register dimension type provider
        pack.addProvider(WPDimensionProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        // Bootstrap dimension types
        registryBuilder.add(Registries.DIMENSION_TYPE, PlotDimension::bootstrap);
    }
}
