package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.*;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class GuiOverlayMagicContainer extends Gui {
    public static GuiOverlayMagicContainer instance;

    Minecraft mc;
    ItemStack itemInUse = null;
    int slotInUse;
    StringBuilder sequence = new StringBuilder();

    final KeyBindingInterceptor[] interceptKeys = new KeyBindingInterceptor[8];

    public GuiOverlayMagicContainer() {
        instance = this;
        mc = Minecraft.getMinecraft();

        for (int i = 0; i < 8; i++) {
            interceptKeys[i] = new KeyBindingInterceptor(Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i]);
            Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i] = interceptKeys[i];
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        //
        // We draw after the ExperienceBar has drawn.  The event raised by GuiIngameForge.pre()
        // will return true from isCancelable.  If you call event.setCanceled(true) in
        // that case, the portion of rendering which this event represents will be canceled.
        // We want to draw *after* the experience bar is drawn, so we make sure isCancelable() returns
        // false and that the eventType represents the ExperienceBar event.
        if (event.isCancelable() || event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        ItemStack heldItem = player.inventory.getCurrentItem();

        if (itemInUse != null && (heldItem != itemInUse || !player.isUsingItem())) {
            endHoldingRightButton(true);
        }

        // Contained essences
        int xPos = 2 + 10;
        int yPos = 2;

        MagicAmounts amounts = MagicDatabase.getContainedMagic(heldItem);
        if (amounts == null)
            return;

        int totalIcons = 0;
        for (int amount : amounts.amounts) {
            if (amount > 0)
                totalIcons++;
        }

        if (totalIcons == 0)
            return;

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft.getMinecraft().renderEngine.bindTexture(icons);

        // TODO: Figure out viewport size to adjust the initial xPos
        //Minecraft.getMinecraft().getRenderManager().vi

        for (int i = 0; i < 8; i++) {
            if (amounts.amounts[i] == 0)
                continue;

            renderItem.renderItemAndEffectIntoGUI(new ItemStack(ElementsOfPower.magicOrb, amounts.amounts[i], i), xPos, yPos);

            this.drawCenteredString(font, "" + amounts.amounts[i], xPos + 8, yPos + 16, (int) 0xFFC0C0C0);
            if (itemInUse != null)
                this.drawCenteredString(font, "K:" + (i + 1), xPos + 8, yPos + 28, (int) 0xFFC0C0C0);

            xPos += 22;
        }

        // Saved spell sequence
        xPos = 2 + 10;
        yPos = 40;

        NBTTagCompound nbt = heldItem.getTagCompound();
        if (nbt != null) {
            String savedSequence = nbt.getString(ItemWand.SPELL_SEQUENCE_TAG);
            for (char c : savedSequence.toCharArray()) {
                int i = SpellManager.elementIndices.get(c);
                renderItem.renderItemAndEffectIntoGUI(new ItemStack(ElementsOfPower.magicOrb, amounts.amounts[i], i), xPos, yPos);
                xPos += 6;
            }
        }

        if (itemInUse != null) {
            for (int i = 0; i < 8; i++) {
                if (interceptKeys[i].retrieveClick() && amounts.amounts[i] > 0) {
                    sequence.append(SpellManager.elementChars[i]);
                }
            }
        }

        if (sequence == null)
            return;

        // New spell sequence
        xPos = 2 + 10;
        yPos = 60;
        for (char c : sequence.toString().toCharArray()) {
            int i = SpellManager.elementIndices.get(c);
            renderItem.renderItemAndEffectIntoGUI(new ItemStack(ElementsOfPower.magicOrb, amounts.amounts[i], i), xPos, yPos);
            xPos += 6;
        }

    }

    public void beginHoldingRightButton(int slotNumber, ItemStack itemUsing) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        itemInUse = itemUsing;
        slotInUse = slotNumber;
        sequence = new StringBuilder();
        ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.BEGIN, player, slotInUse, null));

        for (int i = 0; i < 8; i++)
            interceptKeys[i].setInterceptionActive(true);
    }

    public void endHoldingRightButton(boolean cancelMagicSetting) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (cancelMagicSetting) {
            ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.CANCEL, player, slotInUse, null));
        } else {
            ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.COMMIT, player, slotInUse, sequence.toString()));
        }
        itemInUse = null;
        sequence = new StringBuilder();
        for (int i = 0; i < 8; i++)
            interceptKeys[i].setInterceptionActive(false);
    }
}
