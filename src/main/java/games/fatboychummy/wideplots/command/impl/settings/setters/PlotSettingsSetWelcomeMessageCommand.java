package games.fatboychummy.wideplots.command.impl.settings.setters;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;

public class PlotSettingsSetWelcomeMessageCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        String welcomeMessage = context.getArgument("welcomeMessage", String.class);

        if (CommandUtil.blockedByPermissions(context, plot, PlotActionType.SETTINGS)) {return 0;}

        if (welcomeMessage.length() > PlotStorage.WELCOME_MESSAGE_MAX_LENGTH) {
            CommandUtil.respondFailure(context, "Plot welcome message cannot be longer than " + PlotStorage.WELCOME_MESSAGE_MAX_LENGTH + " characters.");
            return 0;
        }

        plot.setWelcomeMessage(welcomeMessage);
        CommandUtil.respondSuccess(context, "Plot welcome message set to '" + welcomeMessage + "'!");
        return 1;
    }
}
