package dev.gigaherz.elementsofpower.gemstones;

import dev.gigaherz.elementsofpower.items.MagicContainerItem;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
    {
        if (this.allowdedIn(group))
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
    public Component getName(ItemStack stack)
    {
        Component gemstoneName = super.getName(stack);

        Quality q = getQuality(stack);
        if (q == null)
            return gemstoneName;

        return new TranslatableComponent(q.getTranslationKey(), gemstoneName);
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
        CompoundTag tag = stack.getTag();
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
        CompoundTag tag = stack.getTag();

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
            tag = new CompoundTag();
            stack.setTag(tag);
        }

        tag.putInt("quality", q.ordinal());

        return stack;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return getQuality(stack) == null ? 64 : 1;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        if (getQuality(stack) == null)
            tooltip.add(new TranslatableComponent("text.elementsofpower.gemstone.use").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        else
            tooltip.add(new TranslatableComponent("text.elementsofpower.gemstone.combine").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    public ItemStack getStack(Quality quality)
    {
        return setQuality(new ItemStack(this), quality);
    }
}
