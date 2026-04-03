package games.fatboychummy.wideplots.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.UseOnContext;

public class ItemUtil {
    public static void sendError(UseOnContext context, String message) {
        context.getPlayer().sendSystemMessage(
                Component.literal(message)
                        .withStyle(style -> style.withColor(0xFF5555))
        );
    }

    public static void sendMessage(UseOnContext context, String message) {
        context.getPlayer().sendSystemMessage(Component.literal(message));
    }
}
