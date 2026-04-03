package games.fatboychummy.wideplots.command.impl.permissions.update;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissions;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

public class PlotPermissionsUpdateSetSetPriorityCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        String setName = context.getArgument("name", String.class);
        int newPriority = context.getArgument("priority", Integer.class);
        PlotPermissions permissions = plot.getPermissions();
        int index = permissions.getPermissionSetIndex(setName);
        if (index == -1) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.no_set_exists");
            return 0;
        }

        permissions.reorganize(index, newPriority);
        CommandUtil.translatableSuccess(context, "commands.wideplots.response.set.reorganized", setName, newPriority);
        return 0;
    }
}
