package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;

public class MagicContainerCapability
{
    public static Capability<IMagicContainer> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(IMagicContainer.class);
    }

    public static LazyOptional<IMagicContainer> getContainer(ItemStack stack)
    {
        return stack.getCapability(INSTANCE);
    }

    public static boolean hasContainer(ItemStack stack)
    {
        return stack.getCapability(INSTANCE).isPresent();
    }

    public static boolean containsMagic(ItemStack stack)
    {
        return stack.getCapability(INSTANCE).map(cap -> (cap.isInfinite() || !cap.getContainedMagic().isEmpty())).orElse(false);
    }

    public static boolean isNotFull(ItemStack stack)
    {
        return stack.getCapability(INSTANCE).map(cap -> !cap.isInfinite() && !cap.isFull() && !cap.getCapacity().isEmpty()).orElse(false);
    }

    public static class Storage
    {
        public static Tag writeNBT(Capability<IMagicContainer> capability, IMagicContainer instance, Direction side)
        {
            CompoundTag nbt = new CompoundTag();

            MagicAmounts amounts = instance.getContainedMagic();

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                nbt.putFloat(Integer.toString(i), amounts.get(i));
            }

            return nbt;
        }

        public static void readNBT(Capability<IMagicContainer> capability, IMagicContainer instance, Direction side, Tag nbt)
        {
            CompoundTag tag = (CompoundTag) nbt;

            MagicAmounts amounts = MagicAmounts.EMPTY;

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                try
                {
                    float amount = tag.getFloat(Integer.toString(i));

                    amounts = amounts.with(i, amount);
                }
                catch (NumberFormatException ex)
                {
                    throw new ReportedException(new CrashReport("Exception while parsing NBT magic infromation", ex));
                }
            }

            instance.setContainedMagic(amounts);
        }
    }

    public static class Impl implements IMagicContainer
    {
        private MagicAmounts capacity;
        private MagicAmounts containedMagic;

        public Impl()
        {
        }

        @Override
        public MagicAmounts getCapacity()
        {
            return capacity;
        }

        @Override
        public void setCapacity(MagicAmounts capacity)
        {
            this.capacity = capacity;
        }

        @Override
        public MagicAmounts getContainedMagic()
        {
            return containedMagic;
        }

        @Override
        public void setContainedMagic(MagicAmounts containedMagic)
        {
            this.containedMagic = containedMagic;
        }
    }
}
