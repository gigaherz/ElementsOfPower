package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
        List<ITextComponent> toolTip = event.getToolTip();

        addConversionTooltip(toolTip, stack);
        addContainedTooltip(toolTip, stack);
    }

    private static void addConversionTooltip(List<ITextComponent> toolTip, ItemStack stack)
    {
        Item item = stack.getItem();

        if (item == Items.DIAMOND || item == Items.EMERALD || item == Items.QUARTZ)
        {
            toolTip.add(1, new TranslationTextComponent("text.elementsofpower.gemstone.use").mergeStyle(TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
        }

        MagicAmounts amounts = EssenceConversions.CLIENT.getEssences(stack, false);
        if (amounts.isEmpty())
            return;

        toolTip.add(new TranslationTextComponent("elementsofpower.magic.converts").mergeStyle(TextFormatting.YELLOW));
        if (!Screen.hasShiftDown())
        {
            toolTip.add(new TranslationTextComponent("elementsofpower.magic.more_info").mergeStyle(TextFormatting.GRAY));
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

            ITextComponent magicName = MagicAmounts.getMagicName(i);

            IFormattableTextComponent magicAmount = stack.getCount() > 1
                    ? new TranslationTextComponent("elementsofpower.magic.amount_stacked", magicName, str, str2)
                    : new TranslationTextComponent("elementsofpower.magic.amount", magicName, str);

            toolTip.add(magicAmount.mergeStyle(TextFormatting.GRAY));
        }
    }

    public static void addContainedTooltip(List<ITextComponent> toolTip, ItemStack stack)
    {
        MagicContainerCapability.getContainer(stack).ifPresent(magic -> {

            MagicAmounts amounts = magic.getContainedMagic();
            if (amounts.isEmpty() && !magic.isInfinite())
            {
                return;
            }

            toolTip.add(new TranslationTextComponent("elementsofpower.magic.contains").mergeStyle(TextFormatting.YELLOW));
            if (!Screen.hasShiftDown())
            {
                toolTip.add(new TranslationTextComponent("elementsofpower.magic.more_info").mergeStyle(TextFormatting.GRAY));
                return;
            }

            if (magic.isInfinite())
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    ITextComponent magicName = MagicAmounts.getMagicName(i);
                    IFormattableTextComponent magicAmount = new TranslationTextComponent("elementsofpower.magic.amount_infinite", magicName);

                    toolTip.add(magicAmount.mergeStyle(TextFormatting.GRAY));
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

                    ITextComponent magicName = MagicAmounts.getMagicName(i);
                    IFormattableTextComponent magicAmount = new TranslationTextComponent("elementsofpower.magic.amount", magicName, str);

                    toolTip.add(magicAmount.mergeStyle(TextFormatting.GRAY));
                }
            }
        });
    }
}
