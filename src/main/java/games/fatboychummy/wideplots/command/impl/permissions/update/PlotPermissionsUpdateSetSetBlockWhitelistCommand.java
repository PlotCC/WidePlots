package games.fatboychummy.wideplots.command.impl.permissions.update;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotAccessRuleSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class PlotPermissionsUpdateSetSetBlockWhitelistCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        Player player = CommandUtil.requirePlayer(context);
        PlotStorage plot = PlotStorageHandler.getPlot(player);
        String setName = context.getArgument("name", String.class);
        PlotAccessRuleSet set = plot.getPermissions().getPermissionSet(setName);

        if (set == null) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.no_set_exists");
            return 0;
        }

        // Use a whitelist.
        set.setBlockBlacklist(player.getStringUUID(), false);
        CommandUtil.translatableSuccess(context,
                "commands.wideplots.response.blacklist.disabled",
                setName,
                Component.translatable("commands.wideplots.response.generic.block")
        );
        return 1;
    }
}
