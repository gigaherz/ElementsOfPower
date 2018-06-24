package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import net.minecraft.crash.CrashReport;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid=ElementsOfPower.MODID)
public class ContainerInformation
{
    public static boolean canItemContainMagic(ItemStack stack)
    {
        return stack.hasCapability(CapabilityMagicContainer.INSTANCE, null);
    }

    @Nullable
    public static IMagicContainer getMagic(ItemStack stack)
    {
        return stack.getCapability(CapabilityMagicContainer.INSTANCE, null);
    }

    public static boolean itemContainsMagic(ItemStack stack)
    {
        IMagicContainer cap = stack.getCapability(CapabilityMagicContainer.INSTANCE, null);
        return cap != null && (cap.isInfinite() || !cap.getContainedMagic().isEmpty());
    }

    public static boolean canTransferAnything(ItemStack stack, MagicAmounts self)
    {
        IMagicContainer cap = stack.getCapability(CapabilityMagicContainer.INSTANCE, null);
        return cap != null && !cap.isInfinite() && !cap.isFull();
    }
}
