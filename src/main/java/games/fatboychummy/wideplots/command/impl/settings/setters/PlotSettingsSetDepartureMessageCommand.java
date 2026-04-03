package games.fatboychummy.wideplots.command.impl.settings.setters;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;

public class PlotSettingsSetDepartureMessageCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        String departureMessage = context.getArgument("departureMessage", String.class);

        if (CommandUtil.blockedByPermissions(context, plot, PlotActionType.SETTINGS)) {return 0;}

        if (departureMessage.length() > PlotStorage.DEPARTURE_MESSAGE_MAX_LENGTH) {
            CommandUtil.respondFailure(context, "Plot departure message cannot be longer than " + PlotStorage.DEPARTURE_MESSAGE_MAX_LENGTH + " characters.");
            return 0;
        }

        plot.setDepartureMessage(departureMessage);
        CommandUtil.respondSuccess(context, "Plot departure message set to '" + departureMessage + "'!");
        return 1;
    }
}
