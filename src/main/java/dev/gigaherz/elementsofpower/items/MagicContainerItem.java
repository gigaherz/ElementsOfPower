package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class MagicContainerItem extends Item
{
    public MagicContainerItem(Properties properties)
    {
        super(properties);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack _stack, @Nullable CompoundTag nbt)
    {
        return new ICapabilityProvider()
        {
            final ItemStack stack = _stack;
            final IMagicContainer container = new IMagicContainer()
            {

                @Override
                public boolean isInfinite()
                {
                    return MagicContainerItem.this.isInfinite(stack);
                }

                @Override
                public MagicAmounts getCapacity()
                {
                    return MagicContainerItem.this.getCapacity(stack);
                }

                @Override
                public void setCapacity(MagicAmounts capacity)
                {
                    // do nothing
                }

                @Override
                public MagicAmounts getContainedMagic()
                {
                    if (isInfinite())
                        return MagicAmounts.INFINITE;
                    CompoundTag compound = stack.getTag();
                    if (compound == null || !compound.contains("ContainedMagic"))
                        return MagicAmounts.EMPTY;
                    return new MagicAmounts(compound.getCompound("ContainedMagic"));
                }

                @Override
                public void setContainedMagic(MagicAmounts containedMagic)
                {
                    if (isInfinite())
                        return;
                    MagicAmounts am = MagicAmounts.min(getCapacity(), containedMagic);
                    CompoundTag compound = stack.getOrCreateTag();
                    compound.put("ContainedMagic", am.serializeNBT());
                }
            };
            final LazyOptional<IMagicContainer> containerGetter = LazyOptional.of(() -> container);

            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
            {
                if (capability == MagicContainerCapability.INSTANCE && canContainMagic(stack))
                    return containerGetter.cast();
                return LazyOptional.empty();
            }
        };
    }

    public abstract boolean canContainMagic(ItemStack stack);

    public abstract boolean isInfinite(ItemStack stack);

    public abstract MagicAmounts getCapacity(ItemStack stack);

    @Override
    public boolean isFoil(ItemStack stack)
    {
        return MagicContainerCapability.getContainer(stack).map(magic -> {
            if (magic.isInfinite())
                return true;

            return !magic.getContainedMagic().isEmpty();
        }).orElse(false);
    }

    public ItemStack addContainedMagic(ItemStack stack, List<ItemStack> orbs)
    {
        if (stack.getCount() <= 0)
            return ItemStack.EMPTY;
        if (orbs.size() == 0)
            return ItemStack.EMPTY;

        return MagicContainerCapability.getContainer(stack).map(magic -> {
            if (magic.isFull() || magic.isInfinite())
                return ItemStack.EMPTY;

            MagicAmounts totalMagic = MagicAmounts.EMPTY;

            for (ItemStack orb : orbs)
            {
                if (orb.getCount() <= 0)
                    continue;

                totalMagic = totalMagic.add(((MagicOrbItem) orb.getItem()).getElement(), 8);
            }

            if (!magic.insertMagic(totalMagic, true).isEmpty())
                return ItemStack.EMPTY;

            magic.insertMagic(totalMagic, false);
            return stack;
        }).orElse(ItemStack.EMPTY);
    }
}
