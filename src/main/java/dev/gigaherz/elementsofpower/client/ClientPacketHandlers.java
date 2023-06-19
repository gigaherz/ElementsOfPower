package dev.gigaherz.elementsofpower.client;

import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerMenu;
import dev.gigaherz.elementsofpower.network.AddVelocityToPlayer;
import dev.gigaherz.elementsofpower.network.SynchronizeSpellcastState;
import dev.gigaherz.elementsofpower.network.UpdateEssentializerAmounts;
import dev.gigaherz.elementsofpower.network.UpdateEssentializerTile;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.SpellManager;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientPacketHandlers
{
    public static boolean handleSpellcastSync(SynchronizeSpellcastState message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            Level world = mc.level;
            Player player = (Player) world.getEntity(message.casterID);
            ListTag seq = message.spellcast.getList("sequence", Tag.TAG_STRING);
            Spellcast ccast = SpellManager.makeSpell(seq);
            if (ccast != null)
            {
                InitializedSpellcast spellcast = ccast.init(player.level(), player);
                spellcast.readFromNBT(message.spellcast);
                SpellcastEntityData.get(player).ifPresent(data -> data.onSync(message.changeMode, spellcast));
            }
        });
        return true;
    }

    public static boolean handleRemainingAmountsUpdate(UpdateEssentializerAmounts message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            Player player = mc.player;
            if (message.windowId != -1)
            {
                if (message.windowId == player.containerMenu.containerId)
                {
                    if ((player.containerMenu instanceof EssentializerMenu))
                    {
                        ((EssentializerMenu) player.containerMenu).updateAmounts(message.contained, message.remaining);
                    }
                }
            }
        });
        return true;
    }

    public static boolean handleEssentializerTileUpdate(UpdateEssentializerTile message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            if (mc.level == null)
                return;
            if (mc.level.getBlockEntity(message.pos) instanceof EssentializerBlockEntity essentializer)
            {
                essentializer.getInventory().setStackInSlot(0, message.activeItem);
                essentializer.remainingToConvert = message.remaining;
            }
        });
        return true;
    }

    public static boolean handleAddVelocityPlayer(AddVelocityToPlayer message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player == null)
                return;
            mc.player.push(message.vx, message.vy, message.vz);
        });
        return true;
    }
}
