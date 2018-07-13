package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.crash.CrashReport;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class CapabilityMagicContainer
{
    @CapabilityInject(IMagicContainer.class)
    public static Capability<IMagicContainer> INSTANCE;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMagicContainer.class, new Storage(), MagicContainer::new);
    }

    @Nullable
    public static IMagicContainer getContainer(ItemStack stack)
    {
        return stack.getCapability(INSTANCE, null);
    }

    public static boolean hasContainer(ItemStack stack)
    {
        return stack.hasCapability(INSTANCE, null);
    }

    public static boolean containsMagic(ItemStack stack)
    {
        IMagicContainer cap = stack.getCapability(INSTANCE, null);
        return cap != null && (cap.isInfinite() || !cap.getContainedMagic().isEmpty());
    }

    public static boolean isNotFull(ItemStack stack, MagicAmounts self)
    {
        IMagicContainer cap = stack.getCapability(INSTANCE, null);
        return cap != null && !cap.isInfinite() && !cap.isFull() && !cap.getCapacity().isEmpty();
    }

    private static class Storage implements Capability.IStorage<IMagicContainer>
    {
        @Override
        public NBTBase writeNBT(Capability<IMagicContainer> capability, IMagicContainer instance, EnumFacing side)
        {
            NBTTagCompound nbt = new NBTTagCompound();

            MagicAmounts amounts = instance.getContainedMagic();

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                nbt.setFloat(Integer.toString(i), amounts.get(i));
            }

            return nbt;
        }

        @Override
        public void readNBT(Capability<IMagicContainer> capability, IMagicContainer instance, EnumFacing side, NBTBase nbt)
        {
            NBTTagCompound tag = (NBTTagCompound) nbt;

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
}
