package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.essentializer.EssentializerTileEntity;
import gigaherz.elementsofpower.essentializer.gui.EssentializerContainer;
import gigaherz.elementsofpower.network.AddVelocityToPlayer;
import gigaherz.elementsofpower.network.UpdateEssentializerAmounts;
import gigaherz.elementsofpower.network.UpdateEssentializerTileEntity;
import gigaherz.elementsofpower.network.SynchronizeSpellcastState;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ClientPacketHandlers
{
    public static boolean handleSpellcastSync(SynchronizeSpellcastState message)
    {
        Minecraft.getInstance().execute(() ->
        {
            World world = Minecraft.getInstance().world;
            PlayerEntity player = (PlayerEntity) world.getEntityByID(message.casterID);
            SpellcastEntityData.get(player).ifPresent(data -> data.sync(message.changeMode, message.spellcast));
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
