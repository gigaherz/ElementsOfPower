package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class MagicTooltips
{
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event)
    {
        if (ContainerInformation.itemContainsMagic(event.getItemStack()))
            return;

        Item item = event.getItemStack().getItem();

        if (item == Items.DIAMOND || item == Items.EMERALD || item == Items.QUARTZ)
        {
            event.getToolTip().add(1, TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("text." + ElementsOfPower.MODID + ".gemstone.use"));
        }

        MagicAmounts amounts = EssenceConversions.getEssences(event.getItemStack(), false);
        if (amounts == null || amounts.isEmpty())
            return;

        event.getToolTip().add(TextFormatting.YELLOW + "Converts to Essences:");
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
        {
            event.getToolTip().add(TextFormatting.GRAY + "  (Hold SHIFT)");
            return;
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts.amounts[i] == 0)
            {
                continue;
            }

            String magicName = MagicAmounts.getMagicName(i);

            String str;
            if (ContainerInformation.isInfiniteContainer(event.getItemStack()))
                str = String.format("%s  %s x\u221E", TextFormatting.GRAY, magicName);
            else if (event.getItemStack().stackSize > 1)
                str = String.format("%s  %s x%s (stack %s)", TextFormatting.GRAY, magicName,
                        ElementsOfPower.prettyNumberFormatter2.format(amounts.amounts[i]),
                        ElementsOfPower.prettyNumberFormatter2.format(amounts.amounts[i] * event.getItemStack().stackSize));
            else
                str = String.format("%s  %s x%s", TextFormatting.GRAY, magicName,
                        ElementsOfPower.prettyNumberFormatter2.format(amounts.amounts[i]));
            event.getToolTip().add(str);
        }
    }
}
