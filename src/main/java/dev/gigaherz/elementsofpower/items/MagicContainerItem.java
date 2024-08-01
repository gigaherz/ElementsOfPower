package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.ElementsOfPowerItems;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Objects;

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
        var amounts = stack.get(ElementsOfPowerMod.CONTAINED_MAGIC);
        return Objects.requireNonNullElse(amounts, MagicAmounts.EMPTY);
    }


    public void setContainedMagic(ItemStack stack, MagicAmounts containedMagic)
    {
        if (isInfinite(stack))
            return;
        MagicAmounts am = MagicAmounts.min(getCapacity(stack), containedMagic);
        stack.set(ElementsOfPowerMod.CONTAINED_MAGIC, am);
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
