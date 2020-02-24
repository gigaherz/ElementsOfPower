package gigaherz.elementsofpower.gemstones;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.items.MagicContainerItem;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class GemstoneItem extends MagicContainerItem
{
    public static final MagicAmounts[] capacities = {
            MagicAmounts.EMPTY.all(10),
            MagicAmounts.EMPTY.all(50),
            MagicAmounts.EMPTY.all(100),
            MagicAmounts.EMPTY.all(250),
            MagicAmounts.EMPTY.all(500),
            MagicAmounts.INFINITE,
    };

    private final Gemstone gem;

    public GemstoneItem(Gemstone gem, Properties properties)
    {
        super(properties);
        this.gem = gem;
    }

    @Nullable
    public Gemstone getGemstone()
    {
        return gem;
    }

    @Override
    public boolean canContainMagic(ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean isInfinite(ItemStack stack)
    {
        return getGemstone() == Gemstone.CREATIVITE;
    }

    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        Gemstone g = getGemstone();
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
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
    {
        if (this.isInGroup(group))
        {
            // Unexamined
            items.add(new ItemStack(this));
            // Qualities
            for (Quality q : Quality.values)
            {
                items.add(setQuality(new ItemStack(this), q));
            }
        }
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack)
    {
        ITextComponent gemstoneName = super.getDisplayName(stack);

        Quality q = getQuality(stack);
        if (q == null)
            return gemstoneName;

        return new TranslationTextComponent(q.getTranslationKey(), gemstoneName);
    }

    @Override
    public Rarity getRarity(ItemStack stack)
    {
        Quality q = getQuality(stack);
        if (q == null)
            return Rarity.COMMON;
        return q.getRarity();
    }

    @Nullable
    public Quality getQuality(ItemStack stack)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return null;

        if (!tag.contains("quality", Constants.NBT.TAG_INT))
            return null;

        int q = tag.getInt("quality");
        if (q < 0 || q > Quality.values.length)
            return null;

        return Quality.values[q];
    }

    public ItemStack setQuality(ItemStack stack, @Nullable Quality q)
    {
        CompoundNBT tag = stack.getTag();

        if (q == null)
        {
            if (tag != null)
            {
                tag.remove("quality");
            }
            return stack;
        }

        if (tag == null)
        {
            tag = new CompoundNBT();
            stack.setTag(tag);
        }

        tag.putInt("quality", q.ordinal());

        return stack;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        if (getQuality(stack) == null)
            tooltip.add(new TranslationTextComponent("text.elementsofpower.gemstone.use").applyTextStyles(TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
        else
            tooltip.add(new TranslationTextComponent("text.elementsofpower.gemstone.combine").applyTextStyles(TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
    }

    public ItemStack getStack(Quality quality)
    {
        return setQuality(new ItemStack(this), quality);
    }
}
