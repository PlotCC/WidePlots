package games.fatboychummy.wideplots.command.impl.permissions;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotAccessRuleSet;
import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionResult;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class PlotTrustCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        Player player = CommandUtil.requirePlayer(context);
        PlotStorage plot = PlotStorageHandler.getPlot(player);
        GameProfile playerToAdd = context.getArgument("player", GameProfile.class);
        PlotAccessRuleSet set = getOrCreateTrustedSet(context, plot);
        if (set == null) {
            return 0;
        }

        if (set.hasPlayer(playerToAdd.getId().toString())) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.trust.already_trusted");
            return 0;
        }

        set.addPlayer(player.getStringUUID(), playerToAdd.getId().toString());
        CommandUtil.translatableSuccess(context, "commands.wideplots.response.trust.added_player", playerToAdd.getName());
        return 1;
    }

    @Nullable
    public static PlotAccessRuleSet getOrCreateTrustedSet(CommandContext<CommandSourceStack> context, PlotStorage plot) {
        String setName = "trusted";
        PlotAccessRuleSet set = plot.getPermissions().getPermissionSet(setName);
        Player player = CommandUtil.requirePlayer(context);
        if (set == null) {
            // Create the set
            plot.getPermissions().addPermissionSet(player.getStringUUID(), setName);
            set = plot.getPermissions().getPermissionSet(setName);

            if (set == null) {
                CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.set_creation_failed", setName);
                return null;
            }

            set.setPlayerBlacklist("SERVER", false);
            set.setPermission("SERVER", PlotActionType.BUILD, PlotPermissionResult.GRANT);
            set.setPermission("SERVER", PlotActionType.ACCESS, PlotPermissionResult.GRANT);
            set.setPermission("SERVER", PlotActionType.INTERACT, PlotPermissionResult.GRANT);
            set.setPermission("SERVER", PlotActionType.SET_HOME, PlotPermissionResult.GRANT);
            set.setActive("SERVER", true);
            CommandUtil.translatableSuccess(context, "commands.wideplots.response.permissions.created_set", setName);
        }
        return set;
    }
}
