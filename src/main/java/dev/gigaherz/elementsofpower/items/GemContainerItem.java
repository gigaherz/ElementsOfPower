package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
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

    @Nullable
    public Quality getQuality(ItemStack stack)
    {
        return stack.get(ElementsOfPowerMod.GEMSTONE_QUALITY);
    }

    public ItemStack setQuality(ItemStack stack, @Nullable Quality q)
    {
        if (q!= null)
        {
            stack.set(ElementsOfPowerMod.GEMSTONE_QUALITY, q);
            stack.set(DataComponents.RARITY, q != null ? q.getRarity() : null);
        }
        else
        {
            stack.remove(ElementsOfPowerMod.GEMSTONE_QUALITY);
            stack.remove(DataComponents.RARITY);
        }
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
    public int getUseDuration(ItemStack stack, LivingEntity user)
    {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.BOW;
    }
}
