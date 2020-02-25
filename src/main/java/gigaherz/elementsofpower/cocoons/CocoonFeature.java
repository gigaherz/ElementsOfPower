package gigaherz.elementsofpower.cocoons;

import com.mojang.datafixers.Dynamic;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class CocoonFeature extends Feature<NoFeatureConfig>
{
    public static final CocoonFeature INSTANCE = new CocoonFeature(NoFeatureConfig::deserialize);

    public CocoonFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactoryIn)
    {
        super(configFactoryIn);
    }

    /*public void generate(Random rand, int chunkX, int chunkZ, World world, ChunkGenerator chunkGenerator, AbstractChunkProvider chunkProvider)
    {
        int num = Math.max(0, rand.nextInt(7) - 5);
        if (num == 0)
            return;

        DimensionType worldType = world.dimension.getType();

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

        }
    }*/

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config)
    {
        if (worldIn.isAirBlock(pos) || worldIn.getBlockState(pos).getBlock() == Blocks.WATER)
        {
            for (Direction f : Direction.values())
            {
                BlockPos pos1 = pos.offset(f);
                if (worldIn.getBlockState(pos1).isSolidSide(worldIn, pos1, f.getOpposite()))
                {
                    generateOne(pos, f, rand, worldIn, worldIn.getDimension().getType());
                    return true;
                }
            }
        }
        return false;
    }

    private void generateOne(BlockPos pos, Direction f, Random rand, IWorld world, DimensionType worldType)
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
                else if (worldType == DimensionType.THE_NETHER)
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
                    am = am.water(1.5f);
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
                    else if (b == Blocks.END_STONE || b == Blocks.END_STONE_BRICKS)
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
                else if (mat == Material.ORGANIC)
                {
                    am = am.life(0.5f);
                    am = am.earth(1);
                }
                else if (mat == Material.EARTH)
                {
                    am = am.earth(1);
                    if (b == Blocks.PODZOL)
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
                else if (mat == Material.SNOW || mat == Material.SNOW_BLOCK)
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
            IFluidState fluidState = world.getFluidState(pos);
            world.setBlockState(pos, am.getDominantElement().getCocoon().getDefaultState().with(CocoonBlock.FACING, f).with(CocoonBlock.WATERLOGGED, fluidState.getFluid() == Fluids.WATER), 2);
            CocoonTileEntity te = Objects.requireNonNull((CocoonTileEntity) world.getTileEntity(pos));
            te.essenceContained = te.essenceContained.add(am);

            ElementsOfPowerMod.LOGGER.debug("Generated cocoon at {} with amounts {}", pos, am);
        }
    }
}
