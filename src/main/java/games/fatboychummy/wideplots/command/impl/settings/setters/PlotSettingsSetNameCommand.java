package games.fatboychummy.wideplots.command.impl.settings.setters;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;

public class PlotSettingsSetNameCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        String name = context.getArgument("name", String.class);

        if (CommandUtil.blockedByPermissions(context, plot, PlotActionType.SETTINGS)) {return 0;}

        if (name.length() > PlotStorage.NAME_MAX_LENGTH) {
            CommandUtil.respondFailure(context, "Plot name cannot be longer than " + PlotStorage.NAME_MAX_LENGTH + " characters.");
            return 0;
        }

        plot.setName(name);
        CommandUtil.respondSuccess(context, "Plot name set to '" + name + "'!");
        return 1;
    }
}
