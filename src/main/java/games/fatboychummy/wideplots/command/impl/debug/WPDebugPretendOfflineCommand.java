package games.fatboychummy.wideplots.command.impl.debug;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.player.WPPlayerHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class WPDebugPretendOfflineCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {
            return 0;
        }
        ServerPlayer player = CommandUtil.requirePlayer(context);

        WPPlayerHandler.registerPlayer(player.getStringUUID()).offline();
        CommandUtil.translatableSuccess(context, "commands.wideplots.response.debug.pretend_offline");
        return 1;
    }
}
