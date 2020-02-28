package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface IGemstoneHolder
{
    @Nullable
    Gemstone getGemstone(ItemStack stack);
}
