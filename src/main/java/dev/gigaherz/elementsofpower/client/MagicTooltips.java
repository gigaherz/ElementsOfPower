package dev.gigaherz.elementsofpower.client;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.database.EssenceConversionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT, modid = ElementsOfPowerMod.MODID)
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
        Level world = Minecraft.getInstance().level;
        MagicAmounts amounts = ModList.get().isLoaded("aequivaleo") ? EssenceConversionManager.getEssences(world, stack, false).orElse(MagicAmounts.EMPTY) : MagicAmounts.EMPTY;
        if (amounts.isEmpty())
            return;

        toolTip.add(Component.translatable("text.elementsofpower.magic.converts").withStyle(ChatFormatting.YELLOW));
        if (!Screen.hasShiftDown())
        {
            toolTip.add(Component.translatable("text.elementsofpower.magic.more_info").withStyle(ChatFormatting.GRAY));
            return;
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (Math.abs(amounts.get(i)) < 0.000001f)
            {
                continue;
            }

            String str = PRETTY_NUMBER_FORMATTER_2.format(amounts.get(i));
            String str2 = PRETTY_NUMBER_FORMATTER_2.format(amounts.get(i) * stack.getCount());

            Component magicName = MagicAmounts.getMagicName(i);

            MutableComponent magicAmount = stack.getCount() > 1
                    ? Component.translatable("text.elementsofpower.magic.amount_stacked", magicName, str, str2)
                    : Component.translatable("text.elementsofpower.magic.amount", magicName, str);

            toolTip.add(magicAmount.withStyle(ChatFormatting.GRAY));
        }
    }

    public static void addContainedTooltip(List<Component> toolTip, ItemStack stack)
    {
        var magic = MagicContainerCapability.getContainer(stack);
        if (magic != null)
        {
            MagicAmounts amounts = magic.getContainedMagic();
            if (amounts.isEmpty() && !magic.isInfinite())
            {
                return;
            }

            toolTip.add(Component.translatable("text.elementsofpower.magic.contains").withStyle(ChatFormatting.YELLOW));
            if (!Screen.hasShiftDown())
            {
                toolTip.add(Component.translatable("text.elementsofpower.magic.more_info").withStyle(ChatFormatting.GRAY));
                return;
            }

            if (magic.isInfinite())
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    Component magicName = MagicAmounts.getMagicName(i);
                    MutableComponent magicAmount = Component.translatable("text.elementsofpower.magic.amount_infinite", magicName);

                    toolTip.add(magicAmount.withStyle(ChatFormatting.GRAY));
                }
            }
            else
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    if (Math.abs(amounts.get(i)) < 0.000001f)
                    {
                        continue;
                    }

                    String str = PRETTY_NUMBER_FORMATTER_2.format(amounts.get(i));

                    Component magicName = MagicAmounts.getMagicName(i);
                    MutableComponent magicAmount = Component.translatable("text.elementsofpower.magic.amount", magicName, str);

                    toolTip.add(magicAmount.withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }
}
