package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.essentializer.EssentializerTileEntity;
import gigaherz.elementsofpower.essentializer.gui.EssentializerContainer;
import gigaherz.elementsofpower.network.AddVelocityToPlayer;
import gigaherz.elementsofpower.network.SynchronizeSpellcastState;
import gigaherz.elementsofpower.network.UpdateEssentializerAmounts;
import gigaherz.elementsofpower.network.UpdateEssentializerTileEntity;
import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ClientPacketHandlers
{
    public static boolean handleSpellcastSync(SynchronizeSpellcastState message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            World world = mc.world;
            PlayerEntity player = (PlayerEntity) world.getEntityByID(message.casterID);
            ListNBT seq = message.spellcast.getList("sequence", Constants.NBT.TAG_STRING);
            Spellcast ccast = SpellManager.makeSpell(seq);
            if (ccast != null)
            {
                InitializedSpellcast spellcast = ccast.init(player.world, player);
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
            PlayerEntity player = mc.player;
            if (message.windowId != -1)
            {
                if (message.windowId == player.openContainer.windowId)
                {
                    if ((player.openContainer instanceof EssentializerContainer))
                    {
                        ((EssentializerContainer) player.openContainer).updateAmounts(message.contained, message.remaining);
                    }
                }
            }
        });
        return true;
    }

    public static boolean handleEssentializerTileUpdate(UpdateEssentializerTileEntity message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            TileEntity te = mc.world.getTileEntity(message.pos);
            if (te instanceof EssentializerTileEntity)
            {
                EssentializerTileEntity essentializer = (EssentializerTileEntity) te;
                essentializer.getInventory().setStackInSlot(0, message.activeItem);
                essentializer.remainingToConvert = message.remaining;
            }
        });
        return true;
    }

    public static boolean handleAddVelocityPlayer(AddVelocityToPlayer message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.player.addVelocity(message.vx, message.vy, message.vz));
        return true;
    }
}
