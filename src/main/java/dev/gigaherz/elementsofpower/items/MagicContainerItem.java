package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
            final LazyOptional<IMagicContainer> containerGetter = LazyOptional.of(() -> new IMagicContainer()
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
                    return MagicContainerItem.this.getContainedMagic(stack);
                }

                @Override
                public void setContainedMagic(MagicAmounts containedMagic)
                {
                    MagicContainerItem.this.setContainedMagic(stack, containedMagic);
                }
            });

            @NotNull
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
}
