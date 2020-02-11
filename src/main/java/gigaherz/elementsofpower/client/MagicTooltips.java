package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;

import java.util.List;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ElementsOfPowerMod.MODID)
public class MagicTooltips
{
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        List<String> toolTip = event.getToolTip();

        addConversionTooltip(toolTip, stack);
        addContainedTooltip(toolTip, stack);
    }

    private static void addConversionTooltip(List<String> toolTip, ItemStack stack)
    {
        Item item = stack.getItem();

        if (item == Items.DIAMOND || item == Items.EMERALD || item == Items.QUARTZ)
        {
            toolTip.add(1, TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("text." + ElementsOfPowerMod.MODID + ".gemstone.use"));
        }

        MagicAmounts amounts = EssenceConversions.getEssences(stack, false);
        if (amounts.isEmpty())
            return;

        Minecraft mc = Minecraft.getInstance();

        toolTip.add(TextFormatting.YELLOW + "Converts to Essences:");
        if (!InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) && !InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT))
        {
            toolTip.add(TextFormatting.GRAY + "  (Hold SHIFT)");
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
                str = String.format("%s  %s x%s (stack %s)", TextFormatting.GRAY, magicName,
                        ElementsOfPowerMod.prettyNumberFormatter2.format(amounts.get(i)),
                        ElementsOfPowerMod.prettyNumberFormatter2.format(amounts.get(i) * stack.getCount()));
            else
                str = String.format("%s  %s x%s", TextFormatting.GRAY, magicName,
                        ElementsOfPowerMod.prettyNumberFormatter2.format(amounts.get(i)));
            toolTip.add(str);
        }
    }

    public static void addContainedTooltip(List<String> toolTip, ItemStack stack)
    {
        IMagicContainer magic = CapabilityMagicContainer.getContainer(stack);
        if (magic == null)
            return;

        MagicAmounts amounts = magic.getContainedMagic();
        if (amounts.isEmpty() && !magic.isInfinite())
        {
            return;
        }

        toolTip.add(TextFormatting.YELLOW + "Contains magic:");
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
        {
            toolTip.add(TextFormatting.GRAY + "  (Hold SHIFT)");
            return;
        }

        if(magic.isInfinite())
        {
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                String magicName = MagicAmounts.getMagicName(i);
                String str = String.format("%s  %s x\u221E", TextFormatting.GRAY, magicName);
                toolTip.add(str);
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
                String str = String.format("%s  %s x%s", TextFormatting.GRAY, magicName,
                            ElementsOfPowerMod.prettyNumberFormatter2.format(amounts.get(i)));
                toolTip.add(str);
            }
        }
    }

}
