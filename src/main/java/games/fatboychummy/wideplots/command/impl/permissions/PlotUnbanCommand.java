package games.fatboychummy.wideplots.command.impl.permissions;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotAccessRuleSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

public class PlotUnbanCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        Player player = CommandUtil.requirePlayer(context);
        PlotStorage plot = PlotStorageHandler.getPlot(player);
        GameProfile playerToUnban = context.getArgument("player", GameProfile.class);
        PlotAccessRuleSet set = PlotBanCommand.getOrCreateBannedSet(context, plot);
        if (set == null) {
            return 0;
        }

        if (set.hasPlayer(playerToUnban.getId().toString())) {
            set.removePlayer(player.getStringUUID(), playerToUnban.getId().toString());
            CommandUtil.translatableSuccess(context, "commands.wideplots.response.unban.removed_player", playerToUnban.getName());
            return 1;
        }

        CommandUtil.translatableFailure(context, "commands.wideplots.response.unban.not_banned");
        return 0;
    }
}
