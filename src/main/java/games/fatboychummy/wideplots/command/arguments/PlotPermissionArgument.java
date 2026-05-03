package games.fatboychummy.wideplots.command.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import games.fatboychummy.wideplots.world.plot.permissions.PlotPermissionResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class PlotPermissionArgument extends StringRepresentableArgument<PlotPermissionResult> {
    private static final Codec<PlotPermissionResult> CODEC =
            StringRepresentable.fromEnum(PlotPermissionResult::values);

    private PlotPermissionArgument() {
        super(CODEC, PlotPermissionResult::values);
    }

    public static PlotPermissionArgument action() {
        return new PlotPermissionArgument();
    }

    public static PlotPermissionResult getAction(CommandContext<CommandSourceStack> ctx, String name) {
        return ctx.getArgument(name, PlotPermissionResult.class);
    }

    @Override
    @NotNull
    protected String convertId(String input) {
        return input.toLowerCase(Locale.ROOT);
    }
}
