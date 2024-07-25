package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class StaffItem extends WandItem
{
    public StaffItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        MagicAmounts magic = super.getCapacity(stack);
        magic = magic.add(magic);
        return magic;
    }

    @Override
    protected MagicAmounts adjustInsertedMagic(MagicAmounts am)
    {
        return am.multiply(2.0f);
    }

    @Override
    protected MagicAmounts adjustRemovedMagic(MagicAmounts am)
    {
        return am.multiply(0.5f);
    }

    public Component getAugmentName(ItemStack stack)
    {
        //Component gemstoneName = getGemstoneName(stack);

        //Gemstone g = getAugment(stack);
        //if (g == null)
            return Component.literal("none");

        //return Component.translatable("text.elementsofpower.staff.augmented", g.getItem().getDefaultInstance().getHoverName(), gemstoneName);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        Component augmentName = getAugmentName(stack);

        Quality q = getQuality(stack);
        if (q == null)
            return augmentName;

        return Component.translatable(q.getContainerTranslationKey(), augmentName);
    }

    /*@Nullable
    public Gemstone getAugment(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null)
            return null;

        if (tag.contains("augment", Tag.TAG_STRING))
        {
            String g = tag.getString("augment");
            return Gemstone.byName(g);
        }

        return null;
    }

    public ItemStack setAugment(ItemStack stack, @Nullable Gemstone gemstone)
    {
        CompoundTag tag = stack.getTag();
        if (gemstone == null)
        {
            if (tag != null)
            {
                tag.remove("augment");
            }
        }
        else
        {
            if (tag == null)
            {
                tag = new CompoundTag();
                stack.setTag(tag);
            }

            tag.putString("augment", gemstone.getSerializedName());
        }
        return stack;
    }*/
}
