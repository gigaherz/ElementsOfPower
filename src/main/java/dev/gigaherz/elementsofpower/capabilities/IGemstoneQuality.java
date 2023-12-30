package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface IGemstoneQuality
{
    @Nullable
    Quality getQuality(ItemStack stack);

    ItemStack setQuality(ItemStack stack, @Nullable Quality q);
}
