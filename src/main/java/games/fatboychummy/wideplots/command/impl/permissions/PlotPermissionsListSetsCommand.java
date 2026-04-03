package games.fatboychummy.wideplots.command.impl.permissions;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import games.fatboychummy.wideplots.command.PermissionLevel;
import games.fatboychummy.wideplots.util.CommandUtil;
import games.fatboychummy.wideplots.world.plot.permissions.*;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorage;
import games.fatboychummy.wideplots.world.plot.storage.PlotStorageHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
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
        builder.append(Component.translatable("commands.wideplots.response.permissions.list.sets").getString());

        ArrayList<PlotPermissionSet> sets = plot.getPermissions().getPlayerPermissions();
        if (sets.isEmpty()) {
            CommandUtil.translatableSuccess(context, "commands.wideplots.response.permissions.list.none");
            return 1;
        }

        for (int i = 0; i < sets.size(); i++) {
            PlotPermissionSet set = sets.get(i);
            builder.append("\n  (p").append(i).append(") "); // Priority
            appendSet(builder, set);
        }
        CommandUtil.respondSuccess(context, builder.toString());
        return 1;
    }

    private static void appendSet(StringBuilder builder, PlotPermissionSet set) {
        // Set name
        builder.append(set.getName()).append(":\n    ");

        // Players
        builder.append(Component.translatable("commands.wideplots.response.permissions.list.applies_to").getString())
                .append(' ');
        if (set.isPlayerBlacklist()) {
            builder.append(Component.translatable("commands.wideplots.response.permissions.list.all_player_except").getString())
                    .append(' ');
        }
        builder.append("(");
        String result = set.getPlayerUUIDs().stream()
                .map(PlotPermissionsListSetsCommand::getPlayerNameFromUUID)
                .collect(Collectors.joining(", "));
        builder.append(result).append(")\n    ");

        // Blocks
        builder.append(Component.translatable("commands.wideplots.response.permissions.list.applies_to").getString())
                .append(' ');
        if (set.isBlockBlacklist()) {
            builder.append(Component.translatable("commands.wideplots.response.permissions.list.all_block_except").getString())
                    .append(' ');
        }
        builder.append("(");
        result = String.join(", ", set.getApplicableBlocks());
        builder.append(result).append(")\n    ");

        // Bounding box
        if (set.getBoundingBox() != null) {
            BoundingBox box = set.getBoundingBox();
            builder.append(Component.translatable("commands.wideplots.response.permissions.list.bounding_box").getString());
            builder.append("\n      (")
                    .append(box.minX()).append(", ").append(box.minY()).append(", ").append(box.minZ()).append(")\n      (")
                    .append(box.maxX()).append(", ").append(box.maxY()).append(", ").append(box.maxZ()).append(")\n    ");
        } else {
            builder.append(Component.translatable("commands.wideplots.response.permissions.list.no_bounding_box").getString())
                    .append("\n    ");
        }

        // Permissions
        PlotPermissionList permissions = set.getPermissionList();
        builder.append(Component.translatable("commands.wideplots.response.permissions.list.permissions").getString());

        for (PlotActionType action : permissions.getPermissions()) {
            builder.append("\n      ");

            PlotPermission perm = permissions.getPermission(action);

            String actionTranslation = "commands.wideplots.response.permissions.list.actions."
                    + action.toString().toLowerCase();
            String permissionTranslation = "commands.wideplots.response.permissions.list.permission."
                    + perm.toString().toLowerCase();

            builder.append(Component.translatable(actionTranslation).getString())
                    .append(": ").append(Component.translatable(permissionTranslation).getString());
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
