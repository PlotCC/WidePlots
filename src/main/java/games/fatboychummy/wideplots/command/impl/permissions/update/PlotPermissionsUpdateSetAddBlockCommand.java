package games.fatboychummy.wideplots.command.impl.permissions.update;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PlotPermissionsUpdateSetAddBlockCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());
        ResourceLocation block = context.getArgument("block", ResourceLocation.class);
        String setName = context.getArgument("name", String.class);
        PlotPermissionSet set = plot.getPermissions().getPermissionSet(setName);

        if (set == null) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.no_set_exists");
            return 0;
        }

        if (set.hasApplicableBlock(block.toString())) {
            CommandUtil.translatableFailure(context,
                    "commands.wideplots.response.permissions.x_in_set",
                    Component.translatable("commands.wideplots.response.generic.block")
            );
            return 0;
        }

        set.addApplicableBlock(block.toString());
        CommandUtil.translatableSuccess(context, "commands.wideplots.response.permissions.added_x_to_set", block.toString(), setName);
        return 1;
    }
}
