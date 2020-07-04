package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.spells.Element;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class MagicOrbItem extends Item
{
    private final Element element;

    public MagicOrbItem(Element element, Properties properties)
    {
        super(properties);
        this.element = element;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TranslationTextComponent("text.elementsofpower.orb.use").func_240701_a_(TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
        tooltip.add(new TranslationTextComponent("text.elementsofpower.orb.cocoon").func_240701_a_(TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
    }

    public Element getElement()
    {
        return element;
    }
}
