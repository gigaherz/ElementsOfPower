package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import javax.swing.text.JTextComponent;
import java.util.List;

public class MagicTooltips {
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event)
    {
        addInformation(event.itemStack, event.entityPlayer, event.toolTip, event.showAdvancedItemTooltips);
    }

    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltipList, boolean showAdvancedInfo)
    {
        if(stack.getItem() instanceof ItemMagicContainer)
            return;

        MagicAmounts amounts = MagicDatabase.getEssences(stack);

        if (amounts == null || amounts.isEmpty())
            return;

        tooltipList.add(EnumChatFormatting.YELLOW + "Converts to Essences:");
        if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
        {
            tooltipList.add(EnumChatFormatting.GRAY + "  (Hold SHIFT)");
            return;
        }

        for (int i = 0; i < 8; i++) {
            if (amounts.amounts[i] == 0) {
                continue;
            }

            String magicName = MagicDatabase.getMagicName(i);
            String str = String.format("%s  %s x%d", EnumChatFormatting.GRAY, magicName, amounts.amounts[i]);
            tooltipList.add(str);
        }
    }
}
