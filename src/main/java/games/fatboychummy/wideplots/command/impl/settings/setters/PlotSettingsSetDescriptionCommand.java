package games.fatboychummy.wideplots.command.impl.settings.setters;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class PlotSettingsSetDescriptionCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        String description = context.getArgument("description", String.class);

        if (CommandUtil.blockedByPermissions(context, plot, PlotActionType.SETTINGS)) {return 0;}

        if (description.length() > PlotStorage.DESCRIPTION_MAX_LENGTH) {
            CommandUtil.translatableFailure(
                    context,
                    "commands.wideplots.response.settings.set.x_too_long",
                    Component.translatable("commands.wideplots.response.settings.description").getString(),
                    PlotStorage.DESCRIPTION_MAX_LENGTH
            );
            return 0;
        }

        plot.setDescription(description);
        CommandUtil.translatableSuccess(
                context,
                "commands.wideplots.response.settings.set.generic_x",
                Component.translatable("commands.wideplots.response.settings.description").getString(),
                description
        );
        return 1;
    }
}
