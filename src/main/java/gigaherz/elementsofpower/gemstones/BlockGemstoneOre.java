package gigaherz.elementsofpower.gemstones;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gemstones.GemstoneBlockType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.List;
import java.util.Random;

public class BlockGemstoneOre extends Block
{
    public static final PropertyEnum<GemstoneBlockType> TYPE = PropertyEnum.create("type", GemstoneBlockType.class);

    public BlockGemstoneOre()
    {
        super(Material.rock, Material.rock.getMaterialMapColor());
        setHardness(3.0F);
        setResistance(5.0F);
        setStepSound(soundTypePiston);
        setCreativeTab(CreativeTabs.tabBlock);
        setUnlocalizedName(ElementsOfPower.MODID + ".gemstoneOre");
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, TYPE);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        if(meta > GemstoneBlockType.values.length)
            return getDefaultState();
        return getDefaultState().withProperty(TYPE, GemstoneBlockType.values[meta]);
    }

    public ItemStack getStack(int quantity, GemstoneBlockType gemstoneBlockType)
    {
        return new ItemStack(ItemForm.getItemFromBlock(this), quantity, getMetaFromState(getDefaultState().withProperty(TYPE, gemstoneBlockType)));
    }

    @Override
    public void getSubBlocks(net.minecraft.item.Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (GemstoneBlockType type : GemstoneBlockType.values)
        {
            list.add(getStack(1, type));
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return ElementsOfPower.gemstone;
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 1;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        int multiplier = fortune <= 0 ? 0 : Math.max(0, random.nextInt(fortune + 2) - 1);

        return this.quantityDropped(random) * (multiplier + 1);
    }

    @Override
    public int getExpDrop(net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune)
    {
        Random rand = world instanceof World ? ((World) world).rand : new Random();
        return MathHelper.getRandomIntegerInRange(rand, 3, 7);
    }

    @Override
    public int getDamageValue(World worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos).getValue(TYPE).ordinal();
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(TYPE).getGemstone().ordinal();
    }

    public static class ItemForm extends ItemBlock
    {
        public ItemForm(Block block)
        {
            super(block);
            setHasSubtypes(true);
        }

        @Override
        public int getMetadata(int damage)
        {
            return damage;
        }

        @Override
        public String getUnlocalizedName(ItemStack stack)
        {
            if (stack.getMetadata() > GemstoneBlockType.values.length)
                return block.getUnlocalizedName();
            return "tile." + ElementsOfPower.MODID + "." + GemstoneBlockType.values[stack.getMetadata()] + "Ore";
        }
    }

    public static class Generator implements IWorldGenerator
    {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
        {
            switch (world.provider.getDimensionId())
            {
                case -1:
                    break;
                case 0:
                    generateSurface(world, random, chunkX * 16, chunkZ * 16);
                    break;
                case 1:
                    break;
            }
        }

        private void generateSurface(World world, Random rand, int chunkX, int chunkZ)
        {
            for (GemstoneBlockType g : GemstoneBlockType.values)
            {
                for (int k = 0; k < 2; k++)
                {
                    int firstBlockXCoord = chunkX + rand.nextInt(16);
                    int firstBlockYCoord = rand.nextInt(18);
                    int firstBlockZCoord = chunkZ + rand.nextInt(16);

                    (new WorldGenMinable(ElementsOfPower.gemstoneOre.getDefaultState().withProperty(TYPE, g), 5)).generate(world, rand, new BlockPos(firstBlockXCoord, firstBlockYCoord, firstBlockZCoord));
                }
            }
        }
    }
}
