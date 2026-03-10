package games.fatboychummy.wideplots.command.impl.permissions.update;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermission;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;

public class PlotPermissionsUpdateSetSetPermissionCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        String setName = context.getArgument("name", String.class);
        PlotActionType action = context.getArgument("action", PlotActionType.class);
        PlotPermission permission = context.getArgument("permission", PlotPermission.class);
        PlotPermissionSet set = plot.getPermissions().getPermissionSet(setName);

        if (set == null) {
            CommandUtil.respondFailure(context, "No permission set with that name exists.");
            return 0;
        }

        set.setPermission(action, permission);
        CommandUtil.respondSuccess(context, "Set permission " + action.getSerializedName() + " to " + permission.getSerializedName() + " for permission set " + setName + "!");
        return 1;
    }
}
