package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface IGemstoneContainer extends IGemstoneHolder
{
    ItemStack setGemstone(ItemStack stack, @Nullable Gemstone q);
}
