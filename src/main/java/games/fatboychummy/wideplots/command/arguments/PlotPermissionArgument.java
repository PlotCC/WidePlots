package games.fatboychummy.wideplots.command.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class PlotPermissionArgument extends StringRepresentableArgument<PlotPermission> {
    private static final Codec<PlotPermission> CODEC =
            StringRepresentable.fromEnum(PlotPermission::values);

    private PlotPermissionArgument() {
        super(CODEC, PlotPermission::values);
    }

    public static PlotPermissionArgument action() {
        return new PlotPermissionArgument();
    }

    public static PlotPermission getAction(CommandContext<CommandSourceStack> ctx, String name) {
        return ctx.getArgument(name, PlotPermission.class);
    }

    @Override
    @NotNull
    protected String convertId(String input) {
        return input.toLowerCase(Locale.ROOT);
    }
}
