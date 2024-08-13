package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class MagicOrbItem extends Item
{
    private final Element element;

    public MagicOrbItem(Element element, Properties properties)
    {
        super(properties);
        this.element = element;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable("text.elementsofpower.orb.use").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("text.elementsofpower.orb.cocoon").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    public Element getElement()
    {
        return element;
    }

    public MagicAmounts getMagicCharge()
    {
        return MagicAmounts.ofElement(element, 8);
    }
}
