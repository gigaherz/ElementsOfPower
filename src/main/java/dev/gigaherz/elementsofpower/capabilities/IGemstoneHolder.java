package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface IGemstoneHolder
{
    @Nullable
    Gemstone getGemstone(ItemStack stack);
}
