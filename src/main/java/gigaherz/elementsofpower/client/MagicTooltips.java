package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.MagicDatabase;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class MagicTooltips
{
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event)
    {
        if (MagicDatabase.itemContainsMagic(event.itemStack))
            return;

        MagicAmounts amounts = MagicDatabase.getEssences(event.itemStack);
        if (amounts == null || amounts.isEmpty())
            return;

        event.toolTip.add(EnumChatFormatting.YELLOW + "Converts to Essences:");
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
        {
            event.toolTip.add(EnumChatFormatting.GRAY + "  (Hold SHIFT)");
            return;
        }

        for (int i = 0; i < 8; i++)
        {
            if (amounts.amounts[i] == 0)
            {
                continue;
            }

            String magicName = MagicDatabase.getMagicName(i);

            String str;
            if (event.itemStack.stackSize > 1)
                str = String.format("%s  %s x%d (stack %d)", EnumChatFormatting.GRAY, magicName, amounts.amounts[i], amounts.amounts[i] * event.itemStack.stackSize);
            else
                str = String.format("%s  %s x%d", EnumChatFormatting.GRAY, magicName, amounts.amounts[i]);
            event.toolTip.add(str);
        }
    }
}
