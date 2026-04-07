package games.fatboychummy.wideplots.command.impl.permissions;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;

public class PlotUntrustCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(CommandUtil.requirePlayer(context));
        GameProfile playerToRemove = context.getArgument("player", GameProfile.class);
        PlotPermissionSet set = PlotTrustCommand.getOrCreateTrustedSet(context, plot);
        if (set == null) {
            return 0;
        }

        if (!set.hasPlayer(playerToRemove.getId().toString())) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.untrust.not_trusted");
            return 0;
        }

        set.removePlayer(playerToRemove.getId().toString());
        CommandUtil.translatableSuccess(context, "commands.wideplots.response.untrust.removed_player", playerToRemove.getName());
        return 1;
    }
}
