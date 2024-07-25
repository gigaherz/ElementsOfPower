package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.ElementsOfPowerItems;
import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class MagicContainerItem extends Item
{
    public MagicContainerItem(Properties properties)
    {
        super(properties);
    }

    public abstract boolean canContainMagic(ItemStack stack);

    public abstract boolean isInfinite(ItemStack stack);

    public abstract MagicAmounts getCapacity(ItemStack stack);

    @Override
    public boolean isFoil(ItemStack stack)
    {
        var magic = MagicContainerCapability.getContainer(stack);

        return magic != null && (magic.isInfinite() || !magic.getContainedMagic().isEmpty());
    }

    public MagicAmounts getContainedMagic(ItemStack stack)
    {
        if (isInfinite(stack))
            return MagicAmounts.INFINITE;
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("ContainedMagic"))
            return MagicAmounts.EMPTY;
        return new MagicAmounts(compound.getCompound("ContainedMagic"));
    }

    public void setContainedMagic(ItemStack stack, MagicAmounts containedMagic)
    {
        if (isInfinite(stack))
            return;
        MagicAmounts am = MagicAmounts.min(getCapacity(stack), containedMagic);
        CompoundTag compound = stack.getOrCreateTag();
        compound.put("ContainedMagic", am.serializeNBT());
    }

    public MagicAmounts addMagic(ItemStack stack, MagicAmounts toAdd)
    {
        if (!isInfinite(stack))
        {
            MagicAmounts capacity = getCapacity(stack);
            MagicAmounts contained = getContainedMagic(stack);

            MagicAmounts empty = capacity.subtract(contained);

            if (!empty.isEmpty())
            {
                MagicAmounts willAdd = MagicAmounts.min(toAdd, empty);
                MagicAmounts remaining = toAdd.subtract(willAdd);
                setContainedMagic(stack, contained.add(willAdd));
                return remaining;
            }
        }
        return toAdd;
    }
}
