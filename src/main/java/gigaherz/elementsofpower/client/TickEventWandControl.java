package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.spells.SpellManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TickEventWandControl
{
    public static TickEventWandControl instance;

    public String sequence;
    public ItemStack itemInUse = null;
    public int slotInUse;
    public int itemInUseCount;

    final KeyBindingInterceptor[] interceptKeys = new KeyBindingInterceptor[8];

    public TickEventWandControl()
    {
        instance = this;

        GameSettings s = Minecraft.getMinecraft().gameSettings;

        int l = s.keyBindings.length;
        int[] indices = new int[MagicAmounts.ELEMENTS];
        int f = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            KeyBinding b = s.keyBindsHotbar[i];
            for (int j = 0; j < l; j++)
            {
                if (s.keyBindings[(f + j) % l] == b)
                {
                    f = f + j;
                    indices[i] = f;
                    break;
                }
            }
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            interceptKeys[i] = new KeyBindingInterceptor(s.keyBindsHotbar[i]);
            s.keyBindsHotbar[i] = interceptKeys[i];
            s.keyBindings[indices[i]] = interceptKeys[i];
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (itemInUse != null)
            {
                EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                if (player.getItemInUse() == null
                        || player.getItemInUseCount() > itemInUseCount)
                    player.setItemInUse(itemInUse, itemInUseCount);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
            return;

        if (itemInUse == null)
            return;

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null || player.inventory == null)
            return;

        ItemStack heldItem = player.inventory.getCurrentItem();
        int slotNumber = player.inventory.currentItem;


        if (!Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown())
        {
            endHoldingRightButton(false);
            return;
        }

        if (heldItem == null ||
                heldItem.getItem().shouldCauseReequipAnimation(itemInUse, heldItem, slotNumber != slotInUse))
        {
            endHoldingRightButton(true);
            return;
        }

        itemInUseCount--;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (interceptKeys[i].retrieveClick())
            {
                sequence += SpellManager.elementChars[i];
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (!event.entityPlayer.worldObj.isRemote)
            return;

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR
                && itemInUse == null)
        {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            int slotNumber = player.inventory.currentItem;
            ItemStack itemUsing = player.inventory.getCurrentItem();
            if (itemUsing == null || !(itemUsing.getItem() instanceof ItemWand))
                return;

            beginHoldingRightButton(slotNumber, itemUsing);

            event.setCanceled(true);
        }
    }

    private void beginHoldingRightButton(int slotNumber, ItemStack itemUsing)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        itemInUse = itemUsing;
        itemInUseCount = itemInUse.getMaxItemUseDuration();
        slotInUse = slotNumber;

        player.setItemInUse(itemInUse, itemInUseCount);

        sequence = "";
        ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.BEGIN, player, slotInUse, null));

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            interceptKeys[i].setInterceptionActive(true);
        }
    }

    private void endHoldingRightButton(boolean cancelMagicSetting)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (cancelMagicSetting)
        {
            ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.CANCEL, player, slotInUse, null));
        }
        else
        {
            ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.COMMIT, player, slotInUse, sequence.toString()));
        }
        itemInUse = null;
        itemInUseCount = 0;
        slotInUse = -1;
        sequence = null;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            interceptKeys[i].setInterceptionActive(false);
        }
    }
}
