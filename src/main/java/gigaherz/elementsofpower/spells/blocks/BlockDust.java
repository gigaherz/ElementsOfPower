package gigaherz.elementsofpower.spells.blocks;

import gigaherz.elementsofpower.common.BlockRegistered;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockDust extends BlockRegistered
{
    public static final PropertyInteger DENSITY = PropertyInteger.create("density", 1, 16);
    private static final AxisAlignedBB DUMMY_AABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    public BlockDust(String name)
    {
        this(name, Material.CLAY);
    }

    public BlockDust(String name, Material mat)
    {
        super(name, mat);
        setHardness(0.1F);
        setBlockUnbreakable();
        setSoundType(SoundType.CLOTH);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(DENSITY, 16));
    }

    @Deprecated
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Deprecated
    @Override
    public int getLightOpacity(IBlockState state)
    {
        if (state.getBlock() != this)
            return 16;
        return state.getValue(DENSITY);
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
        return true;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);

        rescheduleUpdate(worldIn, pos, worldIn.rand);
    }

    private void rescheduleUpdate(World worldIn, BlockPos pos, Random rand)
    {
        worldIn.scheduleUpdate(pos, this, 4 + rand.nextInt(12));
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
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

        rescheduleUpdate(worldIn, pos, rand);
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
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, DENSITY);
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
    public AxisAlignedBB getSelectedBoundingBox(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return DUMMY_AABB;
    }

    @Deprecated
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }

    @Deprecated
    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state)
    {
        return EnumPushReaction.IGNORE;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
    }
}
