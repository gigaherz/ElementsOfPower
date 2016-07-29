package gigaherz.elementsofpower.spells.blocks;

import gigaherz.elementsofpower.common.BlockRegistered;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockMist extends BlockRegistered
{
    public static final PropertyInteger DENSITY = PropertyInteger.create("density", 1, 16);

    public BlockMist(String name)
    {
        super(name, Material.AIR);
        setHardness(0.1F);
        setBlockUnbreakable();
        setSoundType(SoundType.CLOTH);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(DENSITY, 16));
        setTickRandomly(true);
        setLightOpacity(0);
    }

    @Override
    public boolean isVisuallyOpaque()
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, DENSITY);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(DENSITY, 16 - meta);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 16 - state.getValue(DENSITY);
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 0;
    }

    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Deprecated
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        IBlockState current = blockAccess.getBlockState(pos);
        IBlockState opposite = blockAccess.getBlockState(pos.offset(side.getOpposite()));

        return opposite != current;
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        int density = state.getValue(DENSITY) - 1;
        int maxGive = (int) Math.sqrt(density);

        for (EnumFacing f : EnumFacing.VALUES)
        {
            BlockPos bp = pos.offset(f);
            IBlockState neighbour = worldIn.getBlockState(bp);
            if (neighbour.getBlock().isAir(neighbour, worldIn, bp)
                    || neighbour.getBlock() == Blocks.FIRE)
            {
                boolean given = false;
                if (density > maxGive)
                {
                    int d = rand.nextInt(maxGive);
                    if (d > 0)
                    {
                        worldIn.setBlockState(bp, getDefaultState().withProperty(DENSITY, d));
                        density -= d;
                        given = true;
                    }
                }

                if (!given)
                    worldIn.setBlockToAir(bp);
            }
            else if (neighbour.getBlock() == this)
            {
                if (density > maxGive)
                {
                    int od = neighbour.getValue(DENSITY);
                    if (od < 16)
                    {
                        int d = rand.nextInt(Math.min(16 - od, maxGive));
                        if (d > 0)
                        {
                            worldIn.setBlockState(bp, getDefaultState().withProperty(DENSITY, od + d));
                            density -= d;
                        }
                    }
                }
            }
        }

        if (density <= 0)
        {
            worldIn.setBlockToAir(pos);
        }
        else
        {
            worldIn.setBlockState(pos, state.withProperty(DENSITY, density));
        }

        worldIn.scheduleUpdate(pos, this, rand.nextInt(10));
    }

    @Deprecated
    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state)
    {
        return EnumPushReaction.IGNORE;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }

    @Deprecated
    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB p_185477_4_, List<AxisAlignedBB> p_185477_5_, @Nullable Entity p_185477_6_)
    {
        //super.addCollisionBoxToList(state, worldIn, pos, p_185477_4_, p_185477_5_, p_185477_6_);
    }
}
