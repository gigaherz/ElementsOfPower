package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;

public class MagicContainerCapability
{
    @CapabilityInject(IMagicContainer.class)
    public static Capability<IMagicContainer> INSTANCE;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMagicContainer.class, new Storage(), Impl::new);
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

    private static class Storage implements Capability.IStorage<IMagicContainer>
    {
        @Override
        public INBT writeNBT(Capability<IMagicContainer> capability, IMagicContainer instance, Direction side)
        {
            CompoundNBT nbt = new CompoundNBT();

            MagicAmounts amounts = instance.getContainedMagic();

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                nbt.putFloat(Integer.toString(i), amounts.get(i));
            }

            return nbt;
        }

        @Override
        public void readNBT(Capability<IMagicContainer> capability, IMagicContainer instance, Direction side, INBT nbt)
        {
            CompoundNBT tag = (CompoundNBT) nbt;

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
