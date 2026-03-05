package games.fatboychummy.wideplots.world.structures;

import games.fatboychummy.wideplots.WidePlots;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PlotStructures {
    public static final RoadStructureManager ROAD_STRUCTURE_MANAGER = new RoadStructureManager();
    private static final ResourceLocation BASE = new ResourceLocation("wideplots", "structures.roads");

    public static void init() {
        // Automatically register all structures defined in the NBT files under "structures/roads/"
        // This assumes a directory structure like:
        // wideplots/
        //   structures/
        //     roads/
        //       <shape>/
        //         <type>/
        //           <name>.nbt
        // For example:
        // wideplots/
        //   structures/
        //     roads/
        //       straight/
        //         basic/
        //           1.nbt
        // The NBT files should contain the necessary metadata for registration (shape, type, genChance, etc.)
        // The RoadStructure will handle loading the metadata from the NBT files.
        ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.SERVER_DATA);
        IdentifiableResourceReloadListener reloadListener = new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return BASE;
            }

            @Override
            @NotNull
            public CompletableFuture<Void> reload(
                    PreparableReloadListener.PreparationBarrier preparationBarrier,
                    ResourceManager resourceManager,
                    ProfilerFiller prepareProfiler,
                    ProfilerFiller applyProfiler,
                    Executor prepareExecutor,
                    Executor applyExecutor
            ) {
                // 1: Prepare (load resources asynchronously)
                CompletableFuture<Map<ResourceLocation, Resource>> prepareFuture = CompletableFuture.supplyAsync(() -> {
                    return resourceManager.listResources(
                            "structures/roads",
                            path -> path.getPath().endsWith(".nbt")
                    );
                }, prepareExecutor);

                // 2: Sync (wait for preparation to complete)
                CompletableFuture<Map<ResourceLocation, Resource>> applyStart = prepareFuture.thenCompose(preparationBarrier::wait);

                // 3: Apply (register structures)
                return applyStart.thenAcceptAsync(resources -> {
                    ROAD_STRUCTURE_MANAGER.clear(); // Ensure everything is gone.

                    for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                        ResourceLocation resourceId = entry.getKey();
                        String type = resourceId.getPath().split("/")[3]; // e.g., "basic"

                        try (InputStream inputStream = entry.getValue().open()) {
                            CompoundTag nbt = NbtIo.readCompressed(inputStream);
                            CompoundTag roadData = nbt.getCompound("roadData");

                            String shape = roadData.getString("shape");

                            ROAD_STRUCTURE_MANAGER.registerStructure(resourceId, roadData, shape, type);
                        } catch (IOException e) {
                            WidePlots.LOGGER.error("Failed to load road structure from resource: {}", resourceId, e);
                        }
                    }

                    ROAD_STRUCTURE_MANAGER.debugOutputStructures();
                }, applyExecutor);
            }
        };

        helper.registerReloadListener(reloadListener);
        WidePlots.LOGGER.info("PlotStructures initialized and reload listener registered");
    }
}
