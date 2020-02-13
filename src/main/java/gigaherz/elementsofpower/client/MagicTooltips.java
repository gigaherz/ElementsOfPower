package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ElementsOfPowerMod.MODID)
public class MagicTooltips
{
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
            toolTip.add(1, new TranslationTextComponent("text.elementsofpower.gemstone.use").applyTextStyles(TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
        }

        MagicAmounts amounts = EssenceConversions.getEssences(stack, false);
        if (amounts.isEmpty())
            return;

        Minecraft mc = Minecraft.getInstance();

        toolTip.add(new StringTextComponent("Converts to Essences:").applyTextStyle(TextFormatting.YELLOW));
        if (!InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) && !InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT))
        {
            toolTip.add(new StringTextComponent("  (Hold SHIFT)").applyTextStyle(TextFormatting.GRAY));
            return;
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts.get(i) == 0)
            {
                continue;
            }

            String magicName = MagicAmounts.getMagicName(i);

            String str;
            /*if (magic.isInfinite())
                str = String.format("%s  %s x\u221E", TextFormatting.GRAY, magicName);
            else */if (stack.getCount() > 1)
                str = String.format("  %s x%s (stack %s)", magicName,
                        ElementsOfPowerMod.prettyNumberFormatter2.format(amounts.get(i)),
                        ElementsOfPowerMod.prettyNumberFormatter2.format(amounts.get(i) * stack.getCount()));
            else
                str = String.format(" %s x%s", magicName,
                        ElementsOfPowerMod.prettyNumberFormatter2.format(amounts.get(i)));
            toolTip.add(new StringTextComponent(str).applyTextStyle(TextFormatting.GRAY));
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

            Minecraft mc = Minecraft.getInstance();

            toolTip.add(new StringTextComponent("Contains magic:").applyTextStyle(TextFormatting.YELLOW));
            if (!InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) &&
                    !InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT))
            {
                toolTip.add(new StringTextComponent("  (Hold SHIFT)").applyTextStyle(TextFormatting.GRAY));
                return;
            }

            if(magic.isInfinite())
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    String magicName = MagicAmounts.getMagicName(i);
                    toolTip.add(new StringTextComponent(magicName).applyTextStyle(TextFormatting.GRAY));
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

                    String magicName = MagicAmounts.getMagicName(i);
                    String str = String.format("  %s x%s", magicName, ElementsOfPowerMod.prettyNumberFormatter2.format(amounts.get(i)));
                    toolTip.add(new StringTextComponent(str).applyTextStyle(TextFormatting.GRAY));
                }
            }
        });
    }
}
