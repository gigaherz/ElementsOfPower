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
}
