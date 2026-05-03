package games.fatboychummy.wideplots.block.entity;

import games.fatboychummy.wideplots.block.entity.events.WPEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EventBlockEntity extends BlockEntity {
    private final Set<Consumer<WPEvent>> listeners = new HashSet<>();

    public EventBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public void addListener(Consumer<WPEvent> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<WPEvent> listener) {
        listeners.remove(listener);
    }

    public void fireEvent(WPEvent event) {
        for (Consumer<WPEvent> listener : listeners) {
            listener.accept(event);
        }
    }
}
