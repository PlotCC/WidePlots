package games.fatboychummy.wideplots.client;

import games.fatboychummy.wideplots.world.PlotDimension;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class WidePlotsDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // Register dimension type provider
        pack.addProvider((FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) ->
            new FabricDynamicRegistryProvider(output, registriesFuture) {
                @Override
                protected void configure(HolderLookup.Provider registries, Entries entries) {
                    entries.addAll(registries.lookupOrThrow(Registries.DIMENSION_TYPE));
                }

                @Override
                @NotNull
                public String getName() {
                    return "Dimension Types";
                }
            }
        );
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        // Bootstrap dimension types
        registryBuilder.add(Registries.DIMENSION_TYPE, PlotDimension::bootstrap);
    }
}
