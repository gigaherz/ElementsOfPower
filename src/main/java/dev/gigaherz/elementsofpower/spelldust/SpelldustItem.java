package dev.gigaherz.elementsofpower.spelldust;

import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpelldustItem extends Item
{
    private final Gemstone type;

    public SpelldustItem(Gemstone type, Properties properties)
    {
        super(properties);
        this.type = type;
    }

    public Gemstone getType()
    {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced)
    {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        pTooltipComponents.add(new TextComponent("DEPRECATED: TO BE REMOVED").withStyle(ChatFormatting.RED));
    }
}
