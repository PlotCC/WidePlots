package games.fatboychummy.wideplots.integrations.cc.tweaked;

import dan200.computercraft.api.peripheral.PeripheralLookup;
import games.fatboychummy.wideplots.block.entity.PlotControllerBlockEntity;

public class CCTweakedIntegration {
    public static void init() {
        PeripheralLookup.get().registerForBlockEntities(
                (blockEntity, side) -> new PlotControllerPeripheral((PlotControllerBlockEntity) blockEntity)
        );
    }
}
