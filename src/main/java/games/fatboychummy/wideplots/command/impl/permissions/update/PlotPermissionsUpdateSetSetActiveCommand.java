package games.fatboychummy.wideplots.command.impl.permissions.update;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class PlotPermissionsUpdateSetSetActiveCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(CommandUtil.requirePlayer(context));
        String setName = context.getArgument("name", String.class);
        boolean active = context.getArgument("active", Boolean.class);
        PlotPermissionSet set = plot.getPermissions().getPermissionSet(setName);

        if (set == null) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.no_set_exists");
            return 0;
        }

        set.setActive(active);
        CommandUtil.translatableSuccess(context,
                "commands.wideplots.response.permissions.toggle_set",
                setName,
                active ? Component.translatable("commands.wideplots.response.generic.active") : Component.translatable("commands.wideplots.response.generic.inactive")
        );
        return 1;
    }
}
