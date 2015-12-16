package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class GuiOverlayMagicContainer extends Gui
{
    public static GuiOverlayMagicContainer instance;

    Minecraft mc;
    ItemStack itemInUse = null;
    int slotInUse;
    StringBuilder sequence = new StringBuilder();

    final KeyBindingInterceptor[] interceptKeys = new KeyBindingInterceptor[8];

    public GuiOverlayMagicContainer()
    {
        instance = this;
        mc = Minecraft.getMinecraft();

        GameSettings s = Minecraft.getMinecraft().gameSettings;

        int l = s.keyBindings.length;
        int[] indices = new int[8];
        int f = 0;
        for(int i=0;i<8;i++)
        {
            KeyBinding b = s.keyBindsHotbar[i];
            for(int j =0;j<l;j++)
            {
                if(s.keyBindings[(f+j)%l] == b)
                {
                    f=f+j;
                    indices[i]=f;
                    break;
                }
            }
        }

        for (int i = 0; i < 8; i++)
        {
            interceptKeys[i] = new KeyBindingInterceptor(s.keyBindsHotbar[i]);
            s.keyBindsHotbar[i] = interceptKeys[i];
            s.keyBindings[indices[i]] = interceptKeys[i];
        }
    }

    /**
     * @param event
     */
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE)
        {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        ItemStack heldItem = player.inventory.getCurrentItem();

        if (itemInUse != null && (heldItem != itemInUse || !player.isUsingItem()))
        {
            endHoldingRightButton(true);
        }

        // Contained essences

        MagicAmounts amounts = MagicDatabase.getContainedMagic(heldItem);
        if (amounts == null)
            return;

        int totalIcons = 0;
        for (int amount : amounts.amounts)
        {
            if (amount > 0)
                totalIcons++;
        }

        if (totalIcons == 0)
            return;

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(icons);

        float rescale = 1;
        int rescaledWidth = (int)(event.resolution.getScaledWidth() / rescale);
        int rescaledHeight = (int)(event.resolution.getScaledHeight() / rescale);
        GL11.glScalef(rescale,rescale,1);

        int xPos = (rescaledWidth - (totalIcons-1) * 22 - 16) / 2;
        int yPos = 2;
        for (int i = 0; i < 8; i++)
        {
            if(amounts.amounts[i] != 0)
                GL11.glColor4f(1,1,1,0.5f);

            renderItem.renderItemAndEffectIntoGUI(ElementsOfPower.magicOrb.getStack(amounts.amounts[i], i), xPos, yPos);

            this.drawCenteredString(font, "" + amounts.amounts[i], xPos + 8, yPos + 16, 0xFFC0C0C0);
            if (itemInUse != null)
                this.drawCenteredString(font, "K:" + (i + 1), xPos + 8, yPos + 28, 0xFFC0C0C0);

            if(amounts.amounts[i] != 0)
                GL11.glColor4f(1,1,1,1);

            xPos += 22;
        }

        NBTTagCompound nbt = heldItem.getTagCompound();
        if (nbt != null)
        {
            String savedSequence = nbt.getString(ItemWand.SPELL_SEQUENCE_TAG);

            if(savedSequence != null && savedSequence.length() > 0)
            {
                // Saved spell sequence
                xPos = (rescaledWidth - 6 * (savedSequence.length() - 1) - 14) / 2;
                yPos = rescaledHeight / 2 - 16 - 16;
                for (char c : savedSequence.toCharArray())
                {
                    int i = SpellManager.elementIndices.get(c);
                    renderItem.renderItemAndEffectIntoGUI(ElementsOfPower.magicOrb.getStack(amounts.amounts[i], i), xPos, yPos);
                    xPos += 6;
                }
            }
        }

        if (sequence != null)
        {
            // New spell sequence
            xPos = (rescaledWidth - 6 * (sequence.length() - 1) - 14) / 2;
            yPos = rescaledHeight / 2 + 16;
            for (char c : sequence.toString().toCharArray())
            {
                int i = SpellManager.elementIndices.get(c);
                renderItem.renderItemAndEffectIntoGUI(ElementsOfPower.magicOrb.getStack(amounts.amounts[i], i), xPos, yPos);
                xPos += 6;
            }
        }

        GL11.glPopMatrix();

        // This doesn't belong here, but meh.
        if (itemInUse != null)
        {
            for (int i = 0; i < 8; i++)
            {
                if (interceptKeys[i].retrieveClick() && amounts.amounts[i] > 0)
                {
                    sequence.append(SpellManager.elementChars[i]);
                }
            }
        }
    }

    public void beginHoldingRightButton(int slotNumber, ItemStack itemUsing)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        itemInUse = itemUsing;
        slotInUse = slotNumber;
        sequence = new StringBuilder();
        ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.BEGIN, player, slotInUse, null));

        for (int i = 0; i < 8; i++)
        {
            interceptKeys[i].setInterceptionActive(true);
        }
    }

    public void endHoldingRightButton(boolean cancelMagicSetting)
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
        sequence = new StringBuilder();
        for (int i = 0; i < 8; i++)
        {
            interceptKeys[i].setInterceptionActive(false);
        }
    }
}
