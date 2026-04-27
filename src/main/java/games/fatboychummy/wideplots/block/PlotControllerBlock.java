package games.fatboychummy.wideplots.block;

import games.fatboychummy.wideplots.WidePlots;
import games.fatboychummy.wideplots.block.entity.PlotControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class PlotControllerBlock extends BaseEntityBlock {
    public static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 5);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final int DECAY_TIME = 20 * 10; // 10 seconds in ticks.
    private final RandomSource randomSource = RandomSource.create();

    public PlotControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(DECAY, 0)
                        .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onPlace(blockState, level, blockPos, blockState2, bl);

        if (!level.isClientSide()) {
            level.scheduleTick(blockPos, this, DECAY_TIME);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        super.animateTick(blockState, level, blockPos, randomSource);

        if (blockState.getValue(DECAY) == 5) {
            level.addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    blockPos.getX() + 0.5F,
                    blockPos.getY() + 1,
                    blockPos.getZ() + 0.5F,
                    randomSource.nextGaussian() * 0.02F,
                    (randomSource.nextGaussian() + 1) * 0.05F,
                    randomSource.nextGaussian() * 0.02F
            );
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        WidePlots.LOGGER.info("Plot Controller Damaged");

        int decay = blockState.getValue(DECAY);
        serverLevel.setBlock(blockPos, blockState.setValue(DECAY, (decay + 1) % 6), 2);

        serverLevel.scheduleTick(blockPos, this, DECAY_TIME);
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(DECAY, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return ModBlocks.PLOT_CONTROLLER.defaultBlockState()
                .setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())
                .setValue(DECAY, 0);
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @NotNull
    public InteractionResult use(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            Player player,
            InteractionHand interactionHand,
            BlockHitResult blockHitResult
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            WidePlots.LOGGER.info("we press buttons up in here");

            player.sendSystemMessage(Component.translatable(
                    "gui.plot_controller.nyi"
            ));

            return InteractionResult.CONSUME;
        }
    }

    /** Creates a new block entity for the plot controller.
     * @param blockPos The position of the block
     * @param blockState The state of the block
     * @return The block state
     */
    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        return new PlotControllerBlockEntity(blockPos, blockState);
    }
}
