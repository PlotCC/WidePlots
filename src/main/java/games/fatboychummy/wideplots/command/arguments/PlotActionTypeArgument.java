package games.fatboychummy.wideplots.command.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import java.util.Locale;

import games.fatboychummy.wideplots.world.plot.permissions.PlotActionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class PlotActionTypeArgument extends StringRepresentableArgument<PlotActionType> {
    private static final Codec<PlotActionType> CODEC =
            StringRepresentable.fromEnum(PlotActionType::values);

    private PlotActionTypeArgument() {
        super(CODEC, PlotActionType::values);
    }

    public static PlotActionTypeArgument action() {
        return new PlotActionTypeArgument();
    }

    public static PlotActionType getAction(CommandContext<CommandSourceStack> ctx, String name) {
        return ctx.getArgument(name, PlotActionType.class);
    }

    @Override
    @NotNull
    protected String convertId(String input) {
        return input.toLowerCase(Locale.ROOT);
    }
}
