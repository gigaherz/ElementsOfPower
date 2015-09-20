package gigaherz.elementsofpower.blocks;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockDust extends Block
{

    public static final PropertyInteger DENSITY = PropertyInteger.create("density", 1, 16);

    public BlockDust()
    {
        super(Material.clay);
        setUnlocalizedName(ElementsOfPower.MODID + ".dust");
        setCreativeTab(CreativeTabs.tabMisc);
        setHardness(0.1F);
        setBlockUnbreakable();
        setStepSound(Block.soundTypeCloth);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(DENSITY, 16));
    }

    @Override
    public int getLightOpacity(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this)
            return 16;
        return (Integer) state.getValue(DENSITY);
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 0;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        IBlockState current = worldIn.getBlockState(pos);
        IBlockState opposite = worldIn.getBlockState(pos.offset(side.getOpposite()));

        return opposite != current;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        int density = (Integer) state.getValue(DENSITY) - 1;
        int maxGive = (int) Math.sqrt(density);

        for (EnumFacing f : EnumFacing.VALUES)
        {
            BlockPos bp = pos.offset(f);
            IBlockState neighbour = worldIn.getBlockState(bp);
            if (neighbour.getBlock() == Blocks.air
                    || neighbour.getBlock() == Blocks.fire)
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
                    int od = (Integer) neighbour.getValue(DENSITY);
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

    @Override
    public int getMobilityFlag()
    {
        return 1;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);

        worldIn.scheduleUpdate(pos, this, worldIn.rand.nextInt(10));
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(DENSITY, 16 - meta);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 16 - (Integer) state.getValue(DENSITY);
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, DENSITY);
    }

    @Override
    public boolean isReplaceable(World worldIn, BlockPos pos)
    {
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return null;
    }
}
