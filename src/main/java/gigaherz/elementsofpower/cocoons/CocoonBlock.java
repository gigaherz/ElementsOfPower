package gigaherz.elementsofpower.cocoons;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.entities.EssenceEntity;
import gigaherz.elementsofpower.items.MagicOrbItem;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class CocoonBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private final Element type;

    public CocoonBlock(Element type, Properties properties)
    {
        super(properties);
        this.type = type;
    }

    public Element getType()
    {
        return type;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new CocoonTileEntity();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random)
    {
        if (!worldIn.isRemote)
        {
            CocoonTileEntity te = (CocoonTileEntity) worldIn.getTileEntity(pos);

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
                    EssenceEntity e = new EssenceEntity(worldIn, am);

                    BlockPos p = pos.offset(worldIn.getBlockState(pos).get(FACING).getOpposite());

                    e.setLocationAndAngles(p.getX(), p.getY(), p.getZ(), 0, 0);

                    worldIn.addEntity(e);
                }
            }
        }

        super.randomTick(state, worldIn, pos, random);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        ItemStack heldItem = player.getHeldItem(hand);

        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof MagicOrbItem)
        {
            TileEntity te = worldIn.getTileEntity(pos);

            if (!(te instanceof CocoonTileEntity))
                return ActionResultType.FAIL;

            ((CocoonTileEntity) te).addEssences(heldItem);

            if (!player.abilities.isCreativeMode)
                heldItem.shrink(1);

            return ActionResultType.SUCCESS;
        }

        return super.onBlockActivated(state, worldIn, pos, player, hand, rayTraceResult);
    }

    /*public static class Generator implements IWorldGenerator
    {
        ThreadLocal<Set<BlockPos>> positionsTL = new ThreadLocal<>();

        @Override
        public void generate(Random rand, int chunkX, int chunkZ, World world, ChunkGenerator chunkGenerator, AbstractChunkProvider chunkProvider)
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

                for (Direction f : Direction.VALUES)
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

        private void generateOne(BlockPos pos, Direction f, Random rand, World world, DimensionType worldType)
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
                    BlockState state = world.getBlockState(pos1);
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
            }
        }
    }*/
}
