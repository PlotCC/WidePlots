package games.fatboychummy.wideplots.command.impl.settings.getters;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;

public class PlotSettingsGetVisitorLocationCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(CommandUtil.requirePlayer(context));
        BlockPos offset = plot.getVisitorSpawnOffset();
        BlockPos center = plot.getPlotCenter();
        BlockPos real = center.offset(offset);

        if (CommandUtil.blockedByPermissions(context, plot, PlotActionType.SETTINGS)) {return 0;}

        CommandUtil.translatableSuccess(
                context,
                "commands.wideplots.response.settings.get.visitor_location",
                real.getX(), real.getY(), real.getZ()
        );
        return 1;
    }
}
