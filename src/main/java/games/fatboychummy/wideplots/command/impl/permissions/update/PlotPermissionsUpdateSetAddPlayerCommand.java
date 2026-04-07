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

public class PlotPermissionsUpdateSetAddPlayerCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(CommandUtil.requirePlayer(context));
        Collection<GameProfile> playersToAdd;
        try {
            playersToAdd = GameProfileArgument.getGameProfiles(context, "player");
        } catch (Exception e) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.invalid_player");
            WidePlots.LOGGER.error("Error parsing player argument for add player to permission set command: ", e);
            return 0;
        }
        String setName = context.getArgument("name", String.class);
        PlotPermissionSet set = plot.getPermissions().getPermissionSet(setName);

        if (playersToAdd.size() > 1) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.only_one_x", "player");
            return 0;
        }

        GameProfile playerToAdd = playersToAdd.iterator().next();
        if (playerToAdd.getId() == null) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.invalid_player");
            return 0;
        }

        if (set == null) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.no_set_exists");
            return 0;
        }

        if (set.hasPlayer(playerToAdd.getId().toString())) {
            CommandUtil.translatableFailure(context,
                    "commands.wideplots.response.permissions.x_in_set",
                    Component.translatable("commands.wideplots.response.generic.player")
            );
            return 0;
        }

        set.addPlayer(playerToAdd.getId().toString());
        CommandUtil.translatableSuccess(context,
                "commands.wideplots.response.permissions.added_x_to_set",
                playerToAdd.getName() == null ? Component.translatable("commands.wideplots.response.generic.unkown_player") : playerToAdd.getName(),
                setName
        );
        return 1;
    }
}
