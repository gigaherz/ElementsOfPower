package gigaherz.elementsofpower.gemstones;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGemstone extends ItemMagicContainer
{
    public static final MagicAmounts[] capacities = {
            new MagicAmounts().all(10),
            new MagicAmounts().all(50),
            new MagicAmounts().all(100),
            new MagicAmounts().all(250),
            new MagicAmounts().all(500),
            new MagicAmounts().infinite(),
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
    public String getUnlocalizedName(ItemStack stack)
    {
        int sub = stack.getItemDamage();

        if (sub >= Gemstone.values.length)
            return getUnlocalizedName();

        return "item." + ElementsOfPower.MODID + Gemstone.values[sub].getUnlocalizedName();
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

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int meta = 0; meta < Gemstone.values.length; meta++)
        {
            for (Quality q : Quality.values)
            {
                subItems.add(setQuality(new ItemStack(itemIn, 1, meta), q));
            }
        }
    }

    public void getUnexamined(List<ItemStack> subItems)
    {
        for (int meta = 0; meta < Gemstone.values.length; meta++)
        {
            subItems.add(new ItemStack(this, 1, meta));
        }
    }

    @Nullable
    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        Gemstone g = getGemstone(stack);
        if (g == null)
            return null;

        Quality q = getQuality(stack);
        if (q == null)
            return null;

        MagicAmounts magic = capacities[q.ordinal()].copy();

        Element e = g.getElement();
        if (e == null)
            magic.all(magic.amounts[0] * 0.1f);
        else
            magic.element(g.getElement(), magic.amount(g.getElement()) * 0.25f);

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

        if (meta >= Gemstone.values.length)
            return null;

        return Gemstone.values[meta];
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
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        if (getQuality(stack) == null)
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("text." + ElementsOfPower.MODID + ".gemstone.use"));
        else
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("text." + ElementsOfPower.MODID + ".gemstone.combine"));
        super.addInformation(stack, playerIn, tooltip, advanced);
    }
}
