package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IGemstoneContainer extends IGemstoneHolder
{
    ItemStack setGemstone(ItemStack stack, @Nullable Gemstone q);
}
