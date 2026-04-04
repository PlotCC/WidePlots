package games.fatboychummy.wideplots.command.impl.teleportation;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.PlotDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class PlotTpCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        ServerPlayer player = context.getSource().getPlayer();

        if (PlotDimension.PLOTDIM_LEVEL == null) {
            CommandUtil.respondFailure(
                    context,
                    "commands.wideplots.response.tp.tp.not_loaded"
            );
            return 0;
        }

        BlockPos spawnPos = PlotDimension.PLOTDIM_SPAWN;
        player.teleportTo(PlotDimension.PLOTDIM_LEVEL, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), player.getYRot(), player.getXRot());
        CommandUtil.respondSuccess(
                context,
                "commands.wideplots.response.tp.tp.teleported"
        );
        return 1;
    }
}
