package games.fatboychummy.wideplots.command.impl.permissions;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermission;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class PlotTrustCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        GameProfile playerToAdd = context.getArgument("player", GameProfile.class);
        PlotPermissionSet set = getOrCreateTrustedSet(context, plot);
        if (set == null) {
            return 0;
        }

        if (set.hasPlayer(playerToAdd.getId().toString())) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.trust.already_trusted");
            return 0;
        }

        set.addPlayer(playerToAdd.getId().toString());
        CommandUtil.translatableSuccess(context, "commands.wideplots.response.trust.added_player", playerToAdd.getName());
        return 1;
    }

    @Nullable
    public static PlotPermissionSet getOrCreateTrustedSet(CommandContext<CommandSourceStack> context, PlotStorage plot) {
        String setName = "trusted";
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
            set.setPermission(PlotActionType.BUILD, PlotPermission.GRANT);
            set.setPermission(PlotActionType.ACCESS, PlotPermission.GRANT);
            set.setPermission(PlotActionType.INTERACT, PlotPermission.GRANT);
            set.setPermission(PlotActionType.SET_HOME, PlotPermission.GRANT);
            set.setActive(true);
            CommandUtil.translatableSuccess(context, "commands.wideplots.response.permissions.created_set", setName);
        }
        return set;
    }
}
