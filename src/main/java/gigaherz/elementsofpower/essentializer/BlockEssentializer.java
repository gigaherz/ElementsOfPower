package gigaherz.elementsofpower.essentializer;

import gigaherz.elementsofpower.client.ParticleSmallCloud;
import gigaherz.elementsofpower.essentializer.gui.ContainerEssentializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockEssentializer extends Block
{

    public BlockEssentializer(Properties properties)
    {
        super(properties);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof TileEssentializer))
            return ActionResultType.FAIL;

        if (player.isShiftKeyDown())
            return ActionResultType.PASS;

        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        NetworkHooks.openGui((ServerPlayerEntity)player, new SimpleNamedContainerProvider((id,playerInventory,playerEntity) ->
                new ContainerEssentializer(id,(TileEssentializer) tileEntity,playerInventory),
                new TranslationTextComponent("container.elementsofpower.essentializer.title")), pos);

        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TileEssentializer();
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEssentializer)
        {
            TileEssentializer essentializer = (TileEssentializer) te;
            if (essentializer.remainingToConvert != null)
            {
                double x = (double) pos.getX() + 0.5;
                double y = (double) pos.getY() + (8.5 / 16.0);
                double z = (double) pos.getZ() + 0.5;
                double rx = rand.nextDouble() * 0.2D - 0.1D;
                double rz = rand.nextDouble() * 0.2D - 0.1D;

                int sides = rand.nextInt(16);
                if ((sides & 1) != 0)
                    ParticleSmallCloud.spawn(worldIn, x + rx + 0.4, y, z + rz, 0.0D, 0.05D, 0.0D);
                if ((sides & 2) != 0)
                    ParticleSmallCloud.spawn(worldIn, x + rx - 0.4, y, z + rz, 0.0D, 0.05D, 0.0D);
                if ((sides & 4) != 0)
                    ParticleSmallCloud.spawn(worldIn, x + rx, y, z + rz + 0.4, 0.0D, 0.05D, 0.0D);
                if ((sides & 8) != 0)
                    ParticleSmallCloud.spawn(worldIn, x + rx, y, z + rz - 0.4, 0.0D, 0.05D, 0.0D);
            }
        }
    }
}
