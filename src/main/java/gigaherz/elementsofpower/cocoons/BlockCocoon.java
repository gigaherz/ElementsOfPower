package gigaherz.elementsofpower.cocoons;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.common.BlockRegistered;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.entities.EntityEssence;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BlockCocoon extends BlockRegistered
{
    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);
    public static final PropertyInteger COLOR = PropertyInteger.create("color", 0, MagicAmounts.ELEMENTS);

    public BlockCocoon(String name)
    {
        super(name, Material.CACTUS);
        setTickRandomly(true);
        setCreativeTab(ElementsOfPower.tabMagic);
        setLightOpacity(0);
        setLightLevel(5);
        setHardness(1);
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
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
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, COLOR);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).ordinal();
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        if (meta > EnumFacing.VALUES.length)
            return getDefaultState();
        return getDefaultState().withProperty(FACING, EnumFacing.VALUES[meta]);
    }

    @Deprecated
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

            assert te != null;

            if (!te.essenceContained.isEmpty())
            {
                MagicAmounts am = te.essenceContained;
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    am = am.with(i, (float) Math.floor(am.get(i) * random.nextFloat()));
                }

                if (!am.isEmpty())
                {
                    EntityEssence e = new EntityEssence(worldIn, am);

                    BlockPos p = pos.offset(worldIn.getBlockState(pos).getValue(FACING).getOpposite());

                    e.setLocationAndAngles(p.getX(), p.getY(), p.getZ(), 0, 0);

                    worldIn.spawnEntity(e);
                }
            }
        }

        super.randomTick(worldIn, pos, state, random);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack heldItem = playerIn.getHeldItem(hand);

        if (heldItem.getCount() > 0 && heldItem.getItem() == ElementsOfPower.orb)
        {
            TileEntity te = worldIn.getTileEntity(pos);

            if (!(te instanceof TileCocoon))
                return false;

            ((TileCocoon) te).addEssences(heldItem);

            if (!playerIn.capabilities.isCreativeMode)
                heldItem.shrink(1);

            return true;
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
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
                float a = am.get(i);
                int whole = (int) Math.floor(a);
                if (rand.nextFloat() < (a - whole))
                    whole++;

                if (whole > 0)
                {
                    if (fortune >= 1)
                        whole = (int) (Math.pow(rand.nextFloat(), 1 / (fortune - 1)) * whole);
                    else
                        whole = (int) (Math.pow(rand.nextFloat(), 3 - fortune) * whole);

                    ret.add(new ItemStack(ElementsOfPower.orb, whole, i));
                }
            }
        }

        return ret;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack)
    {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }

    public static class Generator implements IWorldGenerator
    {
        ThreadLocal<Set<BlockPos>> positionsTL = new ThreadLocal<>();

        @Override
        public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
        {
            int num = Math.max(0, rand.nextInt(7) - 5);
            if (num == 0)
                return;

            DimensionType worldType = world.provider.getDimensionType();

            Set<BlockPos> positions = positionsTL.get();

            if (positions == null)
                positionsTL.set(positions = Sets.newHashSet());

            positions.clear();
            for (int i = 0; i < 250 && positions.size() < num; i++)
            {
                int x = rand.nextInt(16);
                int z = rand.nextInt(16);
                int y = rand.nextInt(255);
                BlockPos pos = new BlockPos(chunkX * 16 + 8 + x, y, chunkZ * 16 + 8 + z);
                if (positions.contains(pos))
                    continue;

                if (!world.isAirBlock(pos))
                    continue;

                for (EnumFacing f : EnumFacing.VALUES)
                {
                    BlockPos pos1 = pos.offset(f);
                    if (!world.isAirBlock(pos1))
                    {
                        if (world.isSideSolid(pos1, f.getOpposite()))
                        {
                            positions.add(pos);
                            generateOne(pos, f, rand, world, worldType);
                            break;
                        }
                    }
                }
            }
        }

        private void generateOne(BlockPos pos, EnumFacing f, Random rand, World world, DimensionType worldType)
        {
            int size = 6 + rand.nextInt(10);

            MagicAmounts am = MagicAmounts.EMPTY;

            while (size-- > 0)
            {
                int y = pos.getY() + rand.nextInt(11) - 5;
                int x = pos.getX() + rand.nextInt(11) - 5;
                int z = pos.getZ() + rand.nextInt(11) - 5;
                if (y < 0)
                {
                    am = am.darkness(1);
                }
                else if (y >= world.getHeight())
                {
                    if (worldType == DimensionType.OVERWORLD)
                        am = am.light(1);
                    else
                        am = am.darkness(1);
                }
                else
                {
                    BlockPos pos1 = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(pos1);
                    Block b = state.getBlock();

                    if (worldType == DimensionType.OVERWORLD)
                    {
                        am = am.light(Math.max(0, 0.25f * Math.min(1, (y - 64) / 64.0f)));
                        am = am.darkness(Math.max(0, 0.25f * (64 - y) / 64.0f));
                    }
                    else if (worldType == DimensionType.NETHER)
                    {
                        am = am.fire(0.5f);
                    }
                    else if (worldType == DimensionType.THE_END)
                    {
                        am = am.darkness(0.5f);
                    }

                    Material mat = state.getMaterial();
                    if (mat == Material.AIR)
                    {
                        am = am.air(0.25f);
                    }
                    else if (mat == Material.WATER)
                    {
                        am = am.water(1);
                    }
                    else if (mat == Material.LAVA)
                    {
                        am = am.fire(1);
                        am = am.earth(0.5f);
                    }
                    else if (mat == Material.FIRE)
                    {
                        am = am.fire(1);
                        am = am.air(0.5f);
                    }
                    else if (mat == Material.ROCK)
                    {
                        am = am.earth(1);
                        if (b == Blocks.NETHERRACK)
                        {
                            am = am.fire(0.5f);
                        }
                        else if (b == Blocks.END_STONE || b == Blocks.END_BRICKS)
                        {
                            am = am.darkness(0.5f);
                        }
                    }
                    else if (mat == Material.SAND)
                    {
                        am = am.earth(0.5f);
                        if (b == Blocks.SOUL_SAND)
                        {
                            am = am.death(1);
                        }
                        else
                        {
                            am = am.air(1);
                        }
                    }
                    else if (mat == Material.WOOD)
                    {
                        am = am.life(1);
                        am = am.earth(0.5f);
                    }
                    else if (mat == Material.LEAVES)
                    {
                        am = am.life(1);
                    }
                    else if (mat == Material.PLANTS)
                    {
                        am = am.life(1);
                    }
                    else if (mat == Material.CACTUS)
                    {
                        am = am.life(1);
                        am = am.earth(0.5f);
                    }
                    else if (mat == Material.GRASS)
                    {
                        am = am.life(0.5f);
                        am = am.earth(1);
                    }
                    else if (mat == Material.GROUND)
                    {
                        am = am.earth(1);
                        if (b != Blocks.DIRT || state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL)
                        {
                            am = am.life(0.5f);
                        }
                    }
                    else if (mat == Material.IRON)
                    {
                        am = am.earth(1);
                    }
                    else if (mat == Material.GLASS)
                    {
                        am = am.earth(0.5f);
                        am = am.light(0.5f);
                        am = am.air(0.5f);
                    }
                    else if (mat == Material.REDSTONE_LIGHT)
                    {
                        am = am.earth(0.5f);
                        am = am.light(1);
                    }
                    else if (mat == Material.ICE || mat == Material.PACKED_ICE)
                    {
                        am = am.water(1);
                        am = am.darkness(0.5f);
                    }
                    else if (mat == Material.SNOW || mat == Material.CRAFTED_SNOW)
                    {
                        am = am.water(0.5f);
                        am = am.darkness(0.5f);
                    }
                    else if (mat == Material.CLAY)
                    {
                        am = am.earth(0.5f);
                        am = am.water(1);
                    }
                    else if (mat == Material.GOURD)
                    {
                        am = am.earth(0.5f);
                        am = am.life(0.25f);
                    }
                    else if (mat == Material.DRAGON_EGG)
                    {
                        am = am.darkness(1);
                    }
                }
            }

            if (!am.isEmpty())
            {
                world.setBlockState(pos, ElementsOfPower.cocoon.getDefaultState().withProperty(FACING, f), 2);
                TileCocoon te = (TileCocoon) world.getTileEntity(pos);

                assert te != null;

                te.essenceContained = te.essenceContained.add(am);

                ElementsOfPower.logger.warn("Generated at: " + pos + " near " + world.getBlockState(pos).getBlock().getLocalizedName() + " with " + te.essenceContained);
            }
        }
    }
}
