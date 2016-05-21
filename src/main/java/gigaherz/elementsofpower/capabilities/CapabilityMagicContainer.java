package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.crash.CrashReport;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.Callable;

public class CapabilityMagicContainer
{
    @CapabilityInject(IMagicContainer.class)
    public static Capability<IMagicContainer> INSTANCE;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMagicContainer.class, new Storage(), new Factory());
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
                nbt.setFloat("" + i, amounts.amounts[i]);
            }

            return nbt;
        }

        @Override
        public void readNBT(Capability<IMagicContainer> capability, IMagicContainer instance, EnumFacing side, NBTBase nbt)
        {
            NBTTagCompound tag = (NBTTagCompound) nbt;

            MagicAmounts amounts = new MagicAmounts();

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                try
                {
                    float amount = tag.getFloat("" + i);

                    amounts.amounts[i] = amount;
                }
                catch (NumberFormatException ex)
                {
                    throw new ReportedException(new CrashReport("Exception while parsing NBT magic infromation", ex));
                }
            }

            instance.setContainedMagic(amounts);
        }
    }

    private static class Factory implements Callable<IMagicContainer>
    {
        @Override
        public IMagicContainer call() throws Exception
        {
            throw new UnsupportedOperationException("Default implementation is not available for IMagicContainer. Please create your own instances of MagicContainer.");
        }
    }
}
