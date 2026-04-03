package games.fatboychummy.wideplots.command.impl.permissions;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;

public class PlotPermissionsCreateSetCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        String setName = context.getArgument("name", String.class);
        if (plot.getPermissions().hasPermissionSet(setName)) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.set_exists");
            return 0;
        }

        plot.getPermissions().addPermissionSet(setName);
        CommandUtil.translatableSuccess(context, "commands.wideplots.response.permissions.created_set", setName);
        return 1;
    }
}
