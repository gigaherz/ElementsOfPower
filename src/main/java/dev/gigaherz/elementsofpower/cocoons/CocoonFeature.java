package dev.gigaherz.elementsofpower.cocoons;

import com.mojang.serialization.Codec;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.magic.MagicGradient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.Objects;

public class CocoonFeature extends Feature<NoneFeatureConfiguration>
{
    public static final TagKey<Block> REPLACEABLE_TAG = TagKey.create(Registries.BLOCK,new ResourceLocation("elementsofpower:can_cocoon_replace"));

    public static final MagicGradient OVERWORLD = new MagicGradient.Builder()
            .addPoint(0, MagicAmounts.EMPTY.darkness(0.25f))
            .addPoint(1, MagicAmounts.EMPTY.light(0.25f))
            .addPoint(1, MagicAmounts.EMPTY.light(1))
            .build();

    public static final MagicGradient THE_NETHER = new MagicGradient.Builder()
            .addPoint(0, MagicAmounts.EMPTY.fire(0.5f))
            .addPoint(1, MagicAmounts.EMPTY.fire(0.5f))
            .addPoint(1, MagicAmounts.EMPTY.darkness(1))
            .build();

    public static final MagicGradient THE_END = new MagicGradient.Builder()
            .addPoint(0, MagicAmounts.EMPTY.darkness(0.5f))
            .addPoint(1, MagicAmounts.EMPTY.darkness(0.5f))
            .addPoint(1, MagicAmounts.EMPTY.darkness(1))
            .build();

    public static final MagicGradient DEFAULT = new MagicGradient.Builder()
            .addPoint(0, MagicAmounts.EMPTY)
            .addPoint(1, MagicAmounts.EMPTY)
            .build();

    public CocoonFeature(Codec<NoneFeatureConfiguration> configFactoryIn)
    {
        super(configFactoryIn);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        var worldIn = context.level();
        var pos = context.origin();
        int top = worldIn.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        if (pos.getY() < top && (worldIn.isEmptyBlock(pos) || worldIn.getBlockState(pos).getBlock() == Blocks.WATER))
        {
            for (Direction f : Direction.values())
            {
                BlockPos pos1 = pos.relative(f);
                if (worldIn.getBlockState(pos1).isFaceSturdy(worldIn, pos1, f.getOpposite()))
                {
                    generateOne(pos, f, context.random(), worldIn, context.config());
                    return true;
                }
            }
        }
        return false;
    }

    private void generateOne(BlockPos pos, Direction f, RandomSource rand, LevelAccessor world, NoneFeatureConfiguration config)
    {
        int size = 6 + rand.nextInt(10);

        MagicAmounts am = MagicAmounts.EMPTY;

        var biome = world.getBiome(pos);

        var gradient = DEFAULT;
        if (biome.is(BiomeTags.IS_END))
            gradient = THE_END;
        else if (biome.is(BiomeTags.IS_NETHER))
            gradient = THE_NETHER;
        else if (biome.is(BiomeTags.IS_OVERWORLD))
            gradient = OVERWORLD;

        while (size-- > 0)
        {
            int y = pos.getY() + rand.nextInt(11) - 5;
            int x = pos.getX() + rand.nextInt(11) - 5;
            int z = pos.getZ() + rand.nextInt(11) - 5;
            if (y < 0)
            {
                am = am.darkness(1);
            }
            else if (y >= world.getMaxBuildHeight())
            {
                am = am.add(gradient.getAt(1.01f));
            }
            else
            {
                BlockPos pos1 = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(pos1);
                Block b = state.getBlock();

                am = am.add(gradient.getAt(y / (float) world.getMaxBuildHeight()));

                /* TODO: convert to aqeuivaleo query
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
                else if (mat == Material.STONE)
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
                else if (mat == Material.PLANT)
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
                else if (mat == Material.DIRT)
                {
                    am = am.earth(1);
                    if (b == Blocks.PODZOL)
                    {
                        am = am.life(0.5f);
                    }
                }
                else if (mat == Material.METAL)
                {
                    am = am.earth(1);
                }
                else if (mat == Material.GLASS)
                {
                    am = am.earth(0.5f);
                    am = am.light(0.5f);
                    am = am.air(0.5f);
                }
                else if (mat == Material.BUILDABLE_GLASS)
                {
                    am = am.earth(0.5f);
                    am = am.light(1);
                }
                else if (mat == Material.ICE || mat == Material.ICE_SOLID)
                {
                    am = am.water(1);
                    am = am.darkness(0.5f);
                }
                else if (mat == Material.TOP_SNOW || mat == Material.SNOW)
                {
                    am = am.water(0.5f);
                    am = am.darkness(0.5f);
                }
                else if (mat == Material.CLAY)
                {
                    am = am.earth(0.5f);
                    am = am.water(1);
                }
                else if (mat == Material.VEGETABLE)
                {
                    am = am.earth(0.5f);
                    am = am.life(0.25f);
                }
                else if (mat == Material.EGG)
                {
                    am = am.darkness(1);
                }
                 */
            }
        }

        if (!am.isEmpty())
        {
            FluidState fluidState = world.getFluidState(pos);
            world.setBlock(pos, Objects.requireNonNull(am.getDominantElement().getCocoon()).defaultBlockState().setValue(CocoonBlock.FACING, f).setValue(CocoonBlock.WATERLOGGED, fluidState.getType() == Fluids.WATER), 2);
            CocoonTileEntity te = Objects.requireNonNull((CocoonTileEntity) world.getBlockEntity(pos));
            te.essenceContained = te.essenceContained.add(am);

            //ElementsOfPowerMod.LOGGER.debug("Generated cocoon at {} with amounts {}", pos, am);
        }
    }
}
