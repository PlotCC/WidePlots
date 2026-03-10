package games.fatboychummy.wideplots.command.impl.permissions.update;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;

public class PlotPermissionsUpdateSetRemovePlayerCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        GameProfile playerToAdd = context.getArgument("player", GameProfile.class);
        String setName = context.getArgument("name", String.class);
        PlotPermissionSet set = plot.getPermissions().getPermissionSet(setName);

        if (set == null) {
            CommandUtil.respondFailure(context, "No permission set with that name exists.");
            return 0;
        }

        if (set.hasPlayer(playerToAdd.getId().toString())) {
            set.removePlayer(playerToAdd.getId().toString());
            CommandUtil.respondSuccess(context, "Removed " + playerToAdd.getName() + " from permission set " + setName + "!");
            return 1;
        }

        CommandUtil.respondFailure(context, "That player is not in this permission set.");
        return 0;
    }
}
