package games.fatboychummy.wideplots.integrations.cc.tweaked;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import games.fatboychummy.wideplots.block.PlotControllerBlock;
import games.fatboychummy.wideplots.block.entity.PlotControllerBlockEntity;
import games.fatboychummy.wideplots.integrations.cc.tweaked.event.ComputerEvent;
import games.fatboychummy.wideplots.util.PlotUtility;
import games.fatboychummy.wideplots.world.player.PlotPlayerStorage;
import games.fatboychummy.wideplots.world.player.WPPlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlotControllerPeripheral implements IPeripheral {
    private final List<@NotNull IComputerAccess> computers = new ArrayList<>();
    private final PlotControllerBlockEntity blockEntity;

    public PlotControllerPeripheral(PlotControllerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /**
     * Returns the current decay time of the plot.
     * @return The amount of ticks that have elapsed since the plot owner went offline.
     * @throws LuaException when the plot controller's owner (or data) cannot be found.
     */
    @LuaFunction
    public final long getDecayTime() throws LuaException {
        UUID ownerUUID = blockEntity.getOwner();
        if (ownerUUID == null) {
            throw new LuaException("Plot Controller's owner is null");
        }

        PlotPlayerStorage storage = WPPlayerHandler.getPlayer(ownerUUID.toString());
        if (storage == null) {
            throw new LuaException("Plot Controller owner has no save data.");
        }

        return storage.getTimeOffline();
    }

    /**
     * Gets the maximum number of ticks before the plot becomes claimable.
     * @return MAX_OFFLINE_TIME
     */
    @LuaFunction
    public final long getMaxDecayTime() {
        return PlotControllerBlock.MAX_OFFLINE_TIME;
    }

    /**
     * Gets the plot position
     * @return [1] The plot position X value [2] The plot position Z value
     */
    @LuaFunction
    public final Object getPlotPosition(){
        BlockPos pos = blockEntity.getBlockPos();
        Tuple<Integer, Integer> pc = PlotUtility.plotCoordsFromWorldCoords(
                pos.getX(),
                pos.getZ()
        );

        return new Object[] {pc.getA(), pc.getB()};
    }

    /**
     * Gets a list of players in the plot, by their UUIDs.
     * @return The list of players in the plot.
     */
    @LuaFunction
    public final ArrayList<String> getPlayersInPlot() throws LuaException {
        if (blockEntity.getLevel() == null) {
            throw new LuaException("This should not be reached, unless I am big stoopid");
        }

        // HOPEFULLY, we should not have enough overhead here to worry about.
        // TODO: We will cache player positions at the end of each tick, we should use those instead.
        ArrayList<String> players = new ArrayList<>();
        for (Player player : blockEntity.getLevel().players()) {
            if (PlotUtility.isActuallyInBounds(player.getOnPos())) {
                players.add(player.getStringUUID());
            }
        }

        return players;
    }

    @LuaFunction
    public final Object getPlotShape() throws LuaException {
        // TODO
        throw new LuaException("Not yet implemented.");
    }

    /**
     * Get the type of peripheral
     * @return plot_controller
     */
    @Override
    public String getType() {
        return "plot_controller";
    }

    /**
     * @return Nothing, essentially.
     */
    @Override
    public Set<String> getAdditionalTypes() {
        return IPeripheral.super.getAdditionalTypes();
    }

    /**
     * @param computer The interface to the computer that is being attached. Remember that multiple computers can be
     *                 attached to a peripheral at once.
     */
    @Override
    public void attach(IComputerAccess computer) {
        synchronized (computers) {
            computers.add(computer);
        }
    }

    /**
     * @param computer The interface to the computer that is being detached. Remember that multiple computers can be
     *                 attached to a peripheral at once.
     */
    @Override
    public void detach(IComputerAccess computer) {
        synchronized (computers) {
            computers.remove(computer);
        }
    }

    /**
     * @return
     */
    @Override
    public BlockPos getTarget() {
        return blockEntity.getBlockPos();
    }

    /**
     * @param other The peripheral to compare against. This may be {@code null}.
     * @return
     */
    @Override
    public boolean equals(IPeripheral other) {
        if (other instanceof PlotControllerPeripheral) {
            Object t = other.getTarget();
            if (t instanceof BlockPos) {
                return this.blockEntity.getBlockPos().equals((BlockPos) t);
            }
        }

        return false;
    }

    /**
     * Queues an event to the computer.
     * @param event The event object to be sent to the computer.
     */
    public void queueEvent(@NotNull ComputerEvent event) {
        Object[] objects = new Object[event.args.length + 1];
        System.arraycopy(event.args, 0, objects, 1, event.args.length);
        synchronized (computers) {
            for (IComputerAccess computer : computers) {
                objects[0] = computer.getAttachmentName();
                computer.queueEvent(event.eventName, objects);
            }
        }
    }
}
