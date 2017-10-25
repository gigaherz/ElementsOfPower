package gigaherz.elementsofpower.gemstones;

import gigaherz.common.state.IItemState;
import gigaherz.common.state.IItemStateManager;
import gigaherz.common.state.implementation.ItemStateManager;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGemstone extends ItemMagicContainer
{
    public static final PropertyEnum<Gemstone> GEM = PropertyEnum.create("gem", Gemstone.class);

    public static final MagicAmounts[] capacities = {
            MagicAmounts.EMPTY.all(10),
            MagicAmounts.EMPTY.all(50),
            MagicAmounts.EMPTY.all(100),
            MagicAmounts.EMPTY.all(250),
            MagicAmounts.EMPTY.all(500),
            MagicAmounts.EMPTY.infinite(),
    };

    public ItemGemstone(String name)
    {
        super(name);
        setMaxStackSize(64);
        setHasSubtypes(true);

        // FIXME: Change to a gemstones tab
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public IItemStateManager createStateManager()
    {
        return new ItemStateManager(this, GEM);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        for (Gemstone type : GEM.getAllowedValues())
        {
            IItemState state = getDefaultState().withProperty(GEM, type);
            for (Quality q : Quality.values)
            {
                subItems.add(setQuality(state.getStack(), q));
            }
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        IItemState state = getStateManager().get(stack.getMetadata());

        if (state == null)
            return getUnlocalizedName();

        String subName = state.getValue(GEM).getUnlocalizedName();

        return "item." + ElementsOfPower.MODID + subName;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        @SuppressWarnings("deprecation")
        String gemPart = net.minecraft.util.text.translation.I18n.translateToLocal(getUnlocalizedName(stack) + ".name");

        Quality q = getQuality(stack);

        String qName = ElementsOfPower.MODID + ".gemstone.quality";
        if (q == null)
            return gemPart.trim();

        qName += q.getUnlocalizedName();

        @SuppressWarnings("deprecation")
        String qualityPart = net.minecraft.util.text.translation.I18n.translateToLocal(qName);

        return (qualityPart + " " + gemPart).trim();
    }

    public void getUnexamined(List<ItemStack> subItems)
    {
        for (int meta = 0; meta < Gemstone.values.size(); meta++)
        {
            subItems.add(new ItemStack(this, 1, meta));
        }
    }

    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        Gemstone g = getGemstone(stack);
        if (g == null)
            return MagicAmounts.EMPTY;

        Quality q = getQuality(stack);
        if (q == null)
            return MagicAmounts.EMPTY;

        MagicAmounts magic = capacities[q.ordinal()];

        Element e = g.getElement();
        if (e == null)
            magic = magic.all(magic.get(0) * 0.1f);
        else
            magic = magic.add(g.getElement(), magic.get(g.getElement()) * 0.25f);

        return magic;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack)
    {
        Quality q = getQuality(stack);
        if (q == null)
            return EnumRarity.COMMON;
        return q.getRarity();
    }

    @Nullable
    public Gemstone getGemstone(ItemStack stack)
    {
        int meta = stack.getMetadata();

        if (meta >= Gemstone.values.size())
            return null;

        return Gemstone.values.get(meta);
    }

    @Nullable
    public Quality getQuality(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return null;

        if (!tag.hasKey("quality", Constants.NBT.TAG_INT))
            return null;

        int q = tag.getInteger("quality");
        if (q < 0 || q > Quality.values.length)
            return null;

        return Quality.values[q];
    }

    public ItemStack setQuality(ItemStack stack, Quality q)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        tag.setInteger("quality", q.ordinal());

        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        if (getQuality(stack) == null)
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("text." + ElementsOfPower.MODID + ".gemstone.use"));
        else
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("text." + ElementsOfPower.MODID + ".gemstone.combine"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
