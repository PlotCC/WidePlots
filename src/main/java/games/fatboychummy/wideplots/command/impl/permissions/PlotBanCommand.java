package games.fatboychummy.wideplots.command.impl.permissions;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.command.impl.PlotKickCommand;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermission;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

public class PlotBanCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(CommandUtil.requirePlayer(context));
        GameProfile playerToBan = context.getArgument("player", GameProfile.class);
        PlotPermissionSet set = getOrCreateBannedSet(context, plot);
        if (set == null) {
            return 0;
        }

        if (set.hasPlayer(playerToBan.getId().toString())) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.ban.already_banned");
            return 0;
        }

        set.addPlayer(playerToBan.getId().toString());
        PlotKickCommand.execute(context);
        CommandUtil.translatableSuccess(context, "commands.wideplots.response.ban.added_player", playerToBan.getName());
        return 1;
    }

    @Nullable
    public static PlotPermissionSet getOrCreateBannedSet(CommandContext<CommandSourceStack> context, PlotStorage plot) {
        String setName = "banned";
        PlotPermissionSet set = plot.getPermissions().getPermissionSet(setName);
        if (set == null) {
            // Create the set
            plot.getPermissions().addPermissionSet(setName);
            set = plot.getPermissions().getPermissionSet(setName);

            if (set == null) {
                CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.set_creation_failed", setName);
                return null;
            }

            set.setPlayerBlacklist(false);
            set.setPermission(PlotActionType.BUILD, PlotPermission.DENY);
            set.setPermission(PlotActionType.ACCESS, PlotPermission.DENY);
            set.setPermission(PlotActionType.INTERACT, PlotPermission.DENY);
            set.setPermission(PlotActionType.SET_HOME, PlotPermission.DENY);
            set.setPermission(PlotActionType.ENTER, PlotPermission.DENY);
            set.setPermission(PlotActionType.PVE, PlotPermission.DENY);
            set.setPermission(PlotActionType.PVP, PlotPermission.DENY);
            set.setActive(true);
            CommandUtil.translatableSuccess(context, "commands.wideplots.response.permissions.created_set", setName);
        }
        return set;
    }
}
