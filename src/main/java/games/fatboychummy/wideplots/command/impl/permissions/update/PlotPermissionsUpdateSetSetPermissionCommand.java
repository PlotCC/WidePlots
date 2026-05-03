package games.fatboychummy.wideplots.command.impl.permissions.update;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionResult;
import games.fatboychummy.wideplots.world.plot.permissions.PlotAccessRuleSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

public class PlotPermissionsUpdateSetSetPermissionCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        Player player = CommandUtil.requirePlayer(context);
        PlotStorage plot = PlotStorageHandler.getPlot(player);
        String setName = context.getArgument("name", String.class);
        PlotActionType action = context.getArgument("action", PlotActionType.class);
        PlotPermissionResult permission = context.getArgument("permission", PlotPermissionResult.class);
        PlotAccessRuleSet set = plot.getPermissions().getPermissionSet(setName);

        if (set == null) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.no_set_exists");
            return 0;
        }

        set.setPermission(player.getStringUUID(), action, permission);
        CommandUtil.translatableSuccess(context,
                "commands.wideplots.response.set.permission",
                action.getSerializedName(),
                permission.getSerializedName(),
                setName
        );
        return 1;
    }
}
