package games.fatboychummy.wideplots.command.impl.settings.getters;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

public class PlotSettingsGetGamemodeCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());

        if (CommandUtil.blockedByPermissions(context, plot, PlotActionType.SETTINGS)) {return 0;}

        GameType gameMode = plot.getVisitorGameMode();
        CommandUtil.translatableSuccess(
                context,
                "commands.wideplots.response.settings.get.generic_x",
                Component.translatable("commands.wideplots.response.settings.gamemode").getString(),
                gameMode.getName()
        );
        return 1;
    }
}
