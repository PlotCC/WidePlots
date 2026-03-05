package games.fatboychummy.wideplots.mixin;

import games.fatboychummy.wideplots.world.PlotDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables end portal teleportation in the plot dimension.
 * Allows players to use portal blocks as decoration without teleporting.
 */
@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void disablePortalTeleportInPlotDim(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        // Only cancel portal teleportation in the plot dimension
        // This allows portal blocks to be used decoratively in builds
        // TODO: it seems portals don't actually spawn in the plot dimension, so this may not be necessary?
        if (level.dimension().equals(PlotDimension.PLOTDIM)) {
            ci.cancel();
        }
    }
}


