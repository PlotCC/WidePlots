package games.fatboychummy.wideplots.command.impl.permissions;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.item.ModItems;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionSet;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PlotPermissionsGetBoundingToolCommand {
    public static final String BOUNDING_TOOL_TAG_NAME = WidePlots.id("bounding_tool").toString();

    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}

        ServerPlayer player = CommandUtil.requirePlayer(context);
        PlotStorage plot = PlotStorageHandler.getPlot(player);
        String setName = context.getArgument("name", String.class);
        PlotPermissionSet set = plot.getPermissions().getPermissionSet(setName);

        if (set == null) {
            CommandUtil.translatableFailure(context, "commands.wideplots.response.permissions.no_set_exists");
            return 0;
        }

        ItemStack tool = new ItemStack(ModItems.BOUNDING_TOOL);
        CompoundTag tag = new CompoundTag();

        tag.putString(BOUNDING_TOOL_TAG_NAME, setName);
        tool.setTag(tag);
        tool.setHoverName(
                Component.literal(setName)
                        .withStyle(ChatFormatting.RESET)
                        .withStyle(ChatFormatting.BOLD)
                        .withStyle(ChatFormatting.AQUA)
        );

        player.getInventory().add(tool);
        CommandUtil.translatableSuccess(
                context,
                "commands.wideplots.response.tool.get"
        );
        return 1;
    }
}
