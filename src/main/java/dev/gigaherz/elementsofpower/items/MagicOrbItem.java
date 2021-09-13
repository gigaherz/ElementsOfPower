package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        tooltip.add(new TranslatableComponent("text.elementsofpower.orb.use").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(new TranslatableComponent("text.elementsofpower.orb.cocoon").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    public Element getElement()
    {
        return element;
    }
}
