package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface IGemstoneQuality
{
    @Nullable
    Quality getQuality(ItemStack stack);

    ItemStack setQuality(ItemStack stack, @Nullable Quality q);
}
