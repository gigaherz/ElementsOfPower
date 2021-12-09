package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class GemstoneHolderCapability
{
    public static Capability<IGemstoneHolder> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(IGemstoneHolder.class);
    }

    public static LazyOptional<IGemstoneHolder> get(ItemStack stack)
    {
        return stack.getCapability(INSTANCE);
    }

    public static class Impl implements IGemstoneHolder
    {
        @Nullable
        @Override
        public Gemstone getGemstone(ItemStack stack)
        {
            CompoundTag tag = stack.getTagElement("GemstoneHolder");
            if (tag != null)
            {
                return Gemstone.byName(tag.getString("Gemstone"));
            }
            return null;
        }
    }
}
