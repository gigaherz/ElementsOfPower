package gigaherz.elementsofpower.cocoons;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.entities.EntityEssence;
import gigaherz.elementsofpower.gemstones.Element;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockCocoon extends Block
{
    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);
    public static final PropertyInteger COLOR = PropertyInteger.create("color", 0, MagicAmounts.ELEMENTS);

    public BlockCocoon()
    {
        super(Material.cactus);
        setTickRandomly(true);
        setCreativeTab(ElementsOfPower.tabMagic);
        setLightOpacity(0);
        setLightLevel(5);
        setHardness(1);
        setUnlocalizedName(ElementsOfPower.MODID + ".cocoon");
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileCocoon();
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, FACING, COLOR);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(FACING, EnumFacing.VALUES[meta]);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (state.getBlock() != this)
            return state;

        if (!(te instanceof TileCocoon))
            return state;

        return state.withProperty(COLOR, ((TileCocoon) te).getDominantElement());
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        if (!worldIn.isRemote)
        {
            TileCocoon te = (TileCocoon) worldIn.getTileEntity(pos);

            if (!te.essenceContained.isEmpty())
            {
                MagicAmounts am = te.essenceContained.copy();
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    am.amounts[i] = (float) Math.floor(am.amounts[i] * random.nextFloat());
                }

                if (!am.isEmpty())
                {
                    EntityEssence e = new EntityEssence(worldIn, am);

                    BlockPos p = pos.offset(worldIn.getBlockState(pos).getValue(FACING).getOpposite());

                    e.setLocationAndAngles(p.getX(), p.getY(), p.getZ(), 0, 0);

                    worldIn.spawnEntityInWorld(e);
                }
            }
        }

        super.randomTick(worldIn, pos, state, random);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = playerIn.getHeldItem();
        if (stack != null && stack.getItem() == ElementsOfPower.magicOrb)
        {
            TileEntity te = worldIn.getTileEntity(pos);

            ((TileCocoon) te).addEssences(stack);

            if (!playerIn.capabilities.isCreativeMode)
                stack.stackSize--;

            return true;
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(world, pos, player, false);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        ArrayList<ItemStack> ret = Lists.newArrayList();

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileCocoon)
        {
            Random rand = world instanceof World ? ((World) world).rand : new Random();
            MagicAmounts am = ((TileCocoon) te).essenceContained;

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                float a = am.amounts[i];
                int whole = (int) Math.floor(a);
                if (rand.nextFloat() < (a - whole))
                    whole++;

                if (whole > 0)
                {
                    if (fortune >= 1)
                        whole = (int) (Math.pow(rand.nextFloat(), 1 / (fortune - 1)) * whole);
                    else
                        whole = (int) (Math.pow(rand.nextFloat(), 3 - fortune) * whole);

                    ret.add(new ItemStack(ElementsOfPower.magicOrb, whole, i));
                }
            }
        }

        return ret;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te)
    {
        super.harvestBlock(worldIn, player, pos, state, te);
        worldIn.setBlockToAir(pos);
    }

    public static class Generator implements IWorldGenerator
    {
        @Override
        public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
        {
            List<BlockPos> positions = Lists.newArrayList();
            for (int y = 0; y < 255; y++)
            {
                for (int z = 1; z < 15; z++)
                {
                    for (int x = 1; x < 15; x++)
                    {
                        BlockPos pos = new BlockPos(chunkX * 16 + x, y, chunkZ * 16 + z);
                        if (world.isAirBlock(pos))
                        {
                            for (EnumFacing f : EnumFacing.VALUES)
                            {
                                BlockPos pos1 = pos.offset(f);
                                if (!world.isAirBlock(pos1))
                                {
                                    //if(world.isSideSolid(pos1, f.getOpposite()))
                                    {
                                        positions.add(pos);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (positions.size() == 0)
                return;

            int num = Math.max(0, rand.nextInt(8) - 6);
            for (int k = 0; k < num; k++)
            {
                generateOne(positions, rand, world);
            }
        }

        private void generateOne(List<BlockPos> positions, Random rand, World world)
        {
            BlockPos pos = positions.get(rand.nextInt(positions.size()));

            for (EnumFacing f : EnumFacing.VALUES)
            {
                BlockPos pos1 = pos.offset(f);
                if (!world.isAirBlock(pos1))
                {
                    //if(world.isSideSolid(pos1, f.getOpposite()))
                    {
                        int size = 6 + rand.nextInt(10);

                        MagicAmounts am = new MagicAmounts();

                        int x0 = pos.getX();
                        int y0 = pos.getY();
                        int z0 = pos.getZ();
                        for (int y = Math.max(0, y0 - 2); y <= Math.min(255, y0 + 2); y++)
                        {
                            for (int z = z0 - 2; z < (z0 + 2); z++)
                            {
                                for (int x = x0 - 2; x < (x0 + 2); x++)
                                {
                                    BlockPos pos2 = new BlockPos(x, y, z);
                                    Block b = world.getBlockState(pos2).getBlock();
                                    am.light(Math.max(0, 0.25f * Math.min(1, (y - 64) / 64.0f)));
                                    am.darkness(Math.max(0, 0.25f * (64 - y) / 64.0f));

                                    if (b == Blocks.water || b == Blocks.flowing_water)
                                    {
                                        am.water(1.5f);
                                    }
                                    else if (b == Blocks.lava || b == Blocks.flowing_lava)
                                    {
                                        am.fire(1);
                                    }
                                    else if (b == Blocks.netherrack)
                                    {
                                        am.fire(0.5f);
                                        am.earth(1);
                                    }
                                    else if (b == Blocks.soul_sand)
                                    {
                                        am.death(1);
                                    }
                                    else if (b == Blocks.log || b == Blocks.log2
                                            || b == Blocks.leaves || b == Blocks.leaves2
                                            || b == Blocks.red_flower || b == Blocks.yellow_flower
                                            || b == Blocks.grass || b == Blocks.tallgrass)
                                    {
                                        am.life(1);
                                    }
                                    else if (world.isAirBlock(pos2))
                                    {
                                        am.air(0.25f);
                                    }
                                    else
                                    {
                                        am.earth(1);
                                    }
                                }
                            }
                        }

                        Element e = Element.values()[am.getDominantElement()];

                        List<Element> elements = Lists.newArrayList(Element.values());

                        elements.remove(e);

                        MagicAmounts am2 = new MagicAmounts();
                        am2.element(e, size);

                        while (size > 0 && elements.size() > 0)
                        {
                            size = rand.nextInt(size);

                            int i = rand.nextInt(elements.size());
                            e = elements.get(i);
                            elements.remove(i);

                            am2.element(e, size);
                        }

                        if (!am2.isEmpty())
                        {
                            world.setBlockState(pos, ElementsOfPower.cocoon.getDefaultState().withProperty(FACING, f), 2);
                            TileCocoon te = (TileCocoon) world.getTileEntity(pos);
                            te.essenceContained.add(am2);

                            ElementsOfPower.logger.warn("Generated at: " + pos + " near " + world.getBlockState(pos1).getBlock().getLocalizedName() + " with " + te.essenceContained);
                        }

                        return;
                    }
                }
            }
        }
    }
}