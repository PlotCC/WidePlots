package games.fatboychummy.wideplots.command.impl.permissions.update;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class PlotPermissionsUpdateSetRemovePlayerCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        WidePlots.LOGGER.info("Executing command to add player to permission set...");
        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        Collection<GameProfile> playersToRemove;
        try {
            playersToRemove = GameProfileArgument.getGameProfiles(context, "player");
        } catch (Exception e) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.invalid_player");
            WidePlots.LOGGER.error("Error parsing player argument for add player to permission set command: ", e);
            return 0;
        }
        String setName = context.getArgument("name", String.class);
        PlotPermissionSet set = plot.getPermissions().getPermissionSet(setName);

        if (playersToRemove.size() > 1) {
            CommandUtil.translatableFailure(context,
                    "commands.wideplots.response.permissions.only_one_x",
                    Component.translatable("commands.wideplots.response.generic.player")
            );
            return 0;
        }

        GameProfile playerToRemove = playersToRemove.iterator().next();
        if (playerToRemove.getId() == null) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.invalid_player");
            return 0;
        }

        if (set == null) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.no_set_exists");
            return 0;
        }

        if (set.hasPlayer(playerToRemove.getId().toString())) {
            set.removePlayer(playerToRemove.getId().toString());
            CommandUtil.translatableSuccess(context,
                    "commands.wideplots.response.permissions.removed_x_from_set",
                    Component.translatable("commands.wideplots.response.generic.player"),
                    setName
            );
            return 1;
        }

        CommandUtil.translatableFailure(context,
                "commands.wideplots.response.permissions.x_not_in_set",
                Component.translatable("commands.wideplots.response.generic.player")
        );
        return 0;
    }
}
