package dev.gigaherz.elementsofpower.client;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ElementsOfPowerMod.MODID)
public class MagicTooltips
{
    public static final Format PRETTY_NUMBER_FORMATTER = new DecimalFormat("#.#");
    public static final Format PRETTY_NUMBER_FORMATTER_2 = new DecimalFormat("#0.0");

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        List<Component> toolTip = event.getToolTip();

        addConversionTooltip(toolTip, stack);
        addContainedTooltip(toolTip, stack);
    }

    private static void addConversionTooltip(List<Component> toolTip, ItemStack stack)
    {
        Item item = stack.getItem();

        if (item == Items.DIAMOND || item == Items.EMERALD || item == Items.QUARTZ)
        {
            toolTip.add(1, new TranslatableComponent("text.elementsofpower.gemstone.use").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        Level world = Minecraft.getInstance().level;
        MagicAmounts amounts = AequivaleoPlugin.getEssences(world, stack, false).orElse(MagicAmounts.EMPTY);
        if (amounts.isEmpty())
            return;

        toolTip.add(new TranslatableComponent("elementsofpower.magic.converts").withStyle(ChatFormatting.YELLOW));
        if (!Screen.hasShiftDown())
        {
            toolTip.add(new TranslatableComponent("elementsofpower.magic.more_info").withStyle(ChatFormatting.GRAY));
            return;
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts.get(i) == 0)
            {
                continue;
            }

            String str = PRETTY_NUMBER_FORMATTER_2.format(amounts.get(i));
            String str2 = PRETTY_NUMBER_FORMATTER_2.format(amounts.get(i) * stack.getCount());

            Component magicName = MagicAmounts.getMagicName(i);

            MutableComponent magicAmount = stack.getCount() > 1
                    ? new TranslatableComponent("elementsofpower.magic.amount_stacked", magicName, str, str2)
                    : new TranslatableComponent("elementsofpower.magic.amount", magicName, str);

            toolTip.add(magicAmount.withStyle(ChatFormatting.GRAY));
        }
    }

    public static void addContainedTooltip(List<Component> toolTip, ItemStack stack)
    {
        MagicContainerCapability.getContainer(stack).ifPresent(magic -> {

            MagicAmounts amounts = magic.getContainedMagic();
            if (amounts.isEmpty() && !magic.isInfinite())
            {
                return;
            }

            toolTip.add(new TranslatableComponent("elementsofpower.magic.contains").withStyle(ChatFormatting.YELLOW));
            if (!Screen.hasShiftDown())
            {
                toolTip.add(new TranslatableComponent("elementsofpower.magic.more_info").withStyle(ChatFormatting.GRAY));
                return;
            }

            if (magic.isInfinite())
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    Component magicName = MagicAmounts.getMagicName(i);
                    MutableComponent magicAmount = new TranslatableComponent("elementsofpower.magic.amount_infinite", magicName);

                    toolTip.add(magicAmount.withStyle(ChatFormatting.GRAY));
                }
            }
            else
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    if (amounts.get(i) == 0)
                    {
                        continue;
                    }

                    String str = PRETTY_NUMBER_FORMATTER_2.format(amounts.get(i));

                    Component magicName = MagicAmounts.getMagicName(i);
                    MutableComponent magicAmount = new TranslatableComponent("elementsofpower.magic.amount", magicName, str);

                    toolTip.add(magicAmount.withStyle(ChatFormatting.GRAY));
                }
            }
        });
    }
}
