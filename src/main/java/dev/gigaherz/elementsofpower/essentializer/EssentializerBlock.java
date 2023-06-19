package dev.gigaherz.elementsofpower.essentializer;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class EssentializerBlock extends BaseEntityBlock
{

    public EssentializerBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_)
    {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);

        if (!(tileEntity instanceof EssentializerBlockEntity))
            return InteractionResult.FAIL;

        if (player.isShiftKeyDown())
            return InteractionResult.PASS;

        if (worldIn.isClientSide)
            return InteractionResult.SUCCESS;

        NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, playerInventory, playerEntity) ->
                new EssentializerMenu(id, (EssentializerBlockEntity) tileEntity, playerInventory),
                Component.translatable("container.elementsofpower.essentializer")), pos);

        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return Shapes.or(
                Block.box(0, 0, 0, 16, 7, 16),

                Block.box(0, 7, 0, 4, 12, 4),
                Block.box(12, 7, 0, 16, 12, 4),
                Block.box(0, 7, 12, 4, 12, 16),
                Block.box(12, 7, 12, 16, 12, 16),

                Block.box(4, 12, 4, 12, 16, 12)
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_)
    {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new EssentializerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        return level.isClientSide
                ? createTickerHelper(blockEntityType, ElementsOfPowerMod.ESSENTIALIZER_BLOCK_ENTITY.get(), EssentializerBlockEntity::doTickClient)
                : createTickerHelper(blockEntityType, ElementsOfPowerMod.ESSENTIALIZER_BLOCK_ENTITY.get(), EssentializerBlockEntity::doTickServer);
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand)
    {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof EssentializerBlockEntity essentializer)
        {
            if (!essentializer.remainingToConvert.isEmpty())
            {
                double x = (double) pos.getX() + 0.5;
                double y = (double) pos.getY() + (8.5 / 16.0);
                double z = (double) pos.getZ() + 0.5;
                double rx = rand.nextDouble() * 0.2D - 0.1D;
                double rz = rand.nextDouble() * 0.2D - 0.1D;

                int sides = rand.nextInt(16);
                if ((sides & 1) != 0)
                    worldIn.addParticle(ColoredSmokeData.withRandomColor(essentializer.remainingToConvert), x + rx + 0.4, y, z + rz, 0.0D, 0.05D, 0.0D);
                if ((sides & 2) != 0)
                    worldIn.addParticle(ColoredSmokeData.withRandomColor(essentializer.remainingToConvert), x + rx - 0.4, y, z + rz, 0.0D, 0.05D, 0.0D);
                if ((sides & 4) != 0)
                    worldIn.addParticle(ColoredSmokeData.withRandomColor(essentializer.remainingToConvert), x + rx, y, z + rz + 0.4, 0.0D, 0.05D, 0.0D);
                if ((sides & 8) != 0)
                    worldIn.addParticle(ColoredSmokeData.withRandomColor(essentializer.remainingToConvert), x + rx, y, z + rz - 0.4, 0.0D, 0.05D, 0.0D);
            }
        }
    }
}
