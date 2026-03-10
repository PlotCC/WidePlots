package games.fatboychummy.wideplots.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import net.minecraft.commands.CommandSourceStack;

import java.util.Map;

public class PlotHelpCommand {
    private final Map<String, String> descriptions = Map.of(
        "claim", "Claim the plot you're standing in.",
        "unclaim", "Unclaim the plot you're standing in.",
        "forceclaim [player]", "Force-claim the plot you're standing in as yourself or another player.",
        "forceunclaim", "Force-unclaim the plot you're standing in.",
        "info", "Get information about the plot you're standing in.",
        "visit <player>", "Teleport to another player's plot.",
        "clear <player>", "Clear another player's plot."
    );

    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}

        CommandDispatcher<CommandSourceStack> dispatcher = context.getSource().getServer().getCommands().getDispatcher();
        CommandNode<CommandSourceStack> node = dispatcher.getRoot().getChild("plot");
        Map<CommandNode<CommandSourceStack>, String> usages = dispatcher.getSmartUsage(node, context.getSource());

        for (String usage : usages.values()) {
            CommandUtil.respondSuccess(
                    context,
                    "/plot " + usage + ": " + getDescription(usage)
            );
        }
        return 1;
    }

    private String getDescription(String usage) {
        return "No description available.";
    }
}
