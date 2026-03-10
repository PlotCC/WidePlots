package games.fatboychummy.wideplots.command.impl.permissions;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.*;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PlotPermissionsListSetsCommand {
    private static MinecraftServer server;

    public static int execute(CommandContext<CommandSourceStack> context) {
        if (CommandUtil.shouldBlock(context, PermissionLevel.ALL)) {return 0;}
        if (CommandUtil.blockNonOwner(context)) {return 0;}
        if (server == null) {
            // Cache the server for use in getPlayerNameFromUUID.
            server = context.getSource().getServer();
        }

        PlotStorage plot = PlotStorageHandler.getPlot(context.getSource().getPlayer());

        StringBuilder builder = new StringBuilder();

        ArrayList<PlotPermissionSet> sets = plot.getPermissions().getPlayerPermissions();
        if (sets.isEmpty()) {
            CommandUtil.respondSuccess(context, "No permission sets found.");
            return 1;
        }

        boolean first = true;
        for (PlotPermissionSet set : sets) {
            if (!first) {
                builder.append("\n  ");
            }
            appendSet(builder, set);
            first = false;
        }
        CommandUtil.respondSuccess(context, builder.toString());
        return 1;
    }

    private static void appendSet(StringBuilder builder, PlotPermissionSet set) {
        // Set name
        builder.append(set.getName()).append(":\n    ");

        // Players
        builder.append("Applies to ");
        if (set.isPlayerBlacklist()) {
            builder.append("all players except ");
        }
        builder.append("(");
        String result = set.getPlayerUUIDs().stream()
                .map(PlotPermissionsListSetsCommand::getPlayerNameFromUUID)
                .collect(Collectors.joining(", "));
        builder.append(result).append(")\n    ");

        // Blocks
        builder.append("Applies to ");
        if (set.isBlockBlacklist()) {
            builder.append("all blocks except ");
        }
        builder.append("(");
        result = String.join(", ", set.getApplicableBlocks());
        builder.append(result).append(")\n    ");

        // Bounding box
        if (set.getBoundingBox() != null) {
            BoundingBox box = set.getBoundingBox();
            builder.append("Applies within bounding box:\n      (")
                    .append(box.minX()).append(", ").append(box.minY()).append(", ").append(box.minZ()).append(")\n      (")
                    .append(box.maxX()).append(", ").append(box.maxY()).append(", ").append(box.maxZ()).append(")\n    ");
        }

        // Permissions
        PlotPermissionList permissions = set.getPermissionList();
        builder.append("Permissions:\n      ");

        boolean first = true;
        for (PlotActionType action : permissions.getPermissions()) {
            if (!first) {
                builder.append("\n      ");
            }
            builder.append(action.toString()).append(": ").append(permissions.getPermission(action).toString());
            first = false;
        }
    }

    private static String getPlayerNameFromUUID(String playerUUID) {
        GameProfile profile = server.getProfileCache().get(playerUUID).orElse(null);

        if (profile == null) {
            return playerUUID; // Return the UUID if the profile is not found.
        } else {
            return profile.getName();
        }
    }
}
