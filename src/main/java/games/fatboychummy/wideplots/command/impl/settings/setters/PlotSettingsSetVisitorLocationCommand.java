package games.fatboychummy.wideplots.command.impl.settings.setters;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;

public class PlotSettingsSetVisitorLocationCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());

        if (CommandUtil.blockedByPermissions(context, plot, PlotActionType.SETTINGS)) {return 0;}

        // context.getSource().getPlayer().getRotationVector();
        BlockPos center = plot.getPlotCenter();
        BlockPos pos = context.getSource().getPlayer().blockPosition();
        BlockPos offset = pos.subtract(center);
        plot.setVisitorSpawnOffset(offset);

        CommandUtil.translatableSuccess(
                context,
                "commands.wideplots.response.settings.set.visitor_location",
                offset.getX(), offset.getY(), offset.getZ()
        );
        return 1;
    }
}
