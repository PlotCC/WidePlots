package games.fatboychummy.wideplots.util;

import com.demonwav.mcdev.annotations.Translatable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;

public class ItemUtil {
    public static void sendError(Player player, String message) {
        player.sendSystemMessage(
                Component.literal(message)
                        .withStyle(style -> style.withColor(0xFF5555))
        );
    }

    public static void translatableError(
            Player player,
            @Translatable(foldMethod = true) String key,
            Object... args
    ) {
        sendError(player, Component.translatable(key, args).getString());
    }

    public static void sendMessage(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    public static void translatableMessage(
            Player player,
            @Translatable(foldMethod = true) String key,
            Object... args
    ) {
        sendMessage(player, Component.translatable(key, args).getString());
    }

    public static Player requirePlayer(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            throw new IllegalStateException("Player is null");
        }
        return player;
    }
}
