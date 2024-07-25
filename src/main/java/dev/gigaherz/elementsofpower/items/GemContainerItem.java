package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;

import org.jetbrains.annotations.Nullable;

public abstract class GemContainerItem extends MagicContainerItem
{
    public GemContainerItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean canContainMagic(ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean isInfinite(ItemStack stack)
    {
        return false; // TODO
    }

    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        Quality q = getQuality(stack);
        if (q == null)
            return MagicAmounts.EMPTY;

        MagicAmounts magic = MagicAmounts.EMPTY.all(100);

        return magic;
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

        if (!tag.contains("quality", Tag.TAG_INT))
            return null;

        int q = tag.getInt("quality");
        return Quality.byIndex(q);
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

        tag.putInt("quality", q.getIndex());

        return stack;
    }

    protected MagicAmounts adjustInsertedMagic(MagicAmounts am)
    {
        return am;
    }

    protected MagicAmounts adjustRemovedMagic(MagicAmounts am)
    {
        return am;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || oldStack.getItem() != newStack.getItem()
                || getQuality(oldStack) != getQuality(newStack);
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.BOW;
    }
}
