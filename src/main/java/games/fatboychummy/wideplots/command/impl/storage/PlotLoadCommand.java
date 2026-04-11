package games.fatboychummy.wideplots.command.impl.storage;

import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.util.TimedRequestHelper;
import net.minecraft.commands.CommandSourceStack;

public class PlotLoadCommand {
    private static final String COMMAND_NAME = "load";
    private static final int TIMEOUT = 20 * 10;

    public static void init() {
        TimedRequestHelper.registerTimedRequestCommand(COMMAND_NAME);
    }

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandUtil.notYetImplemented(context);
        return 0;
    }
}
