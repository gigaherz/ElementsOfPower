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

public class GemstoneContainerCapability
{
    public static Capability<IGemstoneContainer> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(IGemstoneContainer.class);
    }

    public static LazyOptional<IGemstoneContainer> get(ItemStack stack)
    {
        return stack.getCapability(INSTANCE);
    }

    public static class Impl implements IGemstoneContainer
    {
        @Override
        public ItemStack setGemstone(ItemStack stack, @Nullable Gemstone g)
        {
            if (g != null)
            {
                CompoundTag tag = stack.getOrCreateTagElement("GemstoneContainer");
                tag.putString("Gemstone", g.getSerializedName());
            }
            else
            {
                CompoundTag tag = stack.getTagElement("GemstoneContainer");
                if (tag != null)
                {
                    tag.remove("Gemstone");
                }
            }
            return stack;
        }

        @Nullable
        @Override
        public Gemstone getGemstone(ItemStack stack)
        {
            CompoundTag tag = stack.getTagElement("GemstoneContainer");
            if (tag != null)
            {
                return Gemstone.byName(tag.getString("Gemstone"));
            }
            return null;
        }
    }
}
