package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.essentializer.gui.ContainerEssentializer;
import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ClientPacketHandlers
{
    public static boolean handleSpellcastSync(SpellcastSync message)
    {
        Minecraft.getInstance().execute(() ->
        {
            World world = Minecraft.getInstance().world;
            PlayerEntity player = (PlayerEntity) world.getEntityByID(message.casterID);
            SpellcastEntityData data = SpellcastEntityData.get(player);

            data.sync(message.changeMode, message.spellcast);
        });
        return true;
    }

    public static boolean handleRemainingAmountsUpdate(EssentializerAmountsUpdate message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            PlayerEntity player = mc.player;

            if (message.windowId != -1)
            {
                if (message.windowId == player.openContainer.windowId)
                {
                    if ((player.openContainer instanceof ContainerEssentializer))
                    {
                        ((ContainerEssentializer) player.openContainer).updateAmounts(message.contained, message.remaining);
                    }
                }
            }
        });
        return true;
    }

    public static boolean handleEssentializerTileUpdate(EssentializerTileUpdate message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            TileEntity te = mc.world.getTileEntity(message.pos);
            if (te instanceof TileEssentializer)
            {
                TileEssentializer essentializer = (TileEssentializer) te;
                essentializer.getInventory().setStackInSlot(0, message.activeItem);
                essentializer.remainingToConvert = message.remaining;
            }
        });
        return true;
    }

    public static boolean handleAddVelocityPlayer(AddVelocityPlayer message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.player.addVelocity(message.vx, message.vy, message.vz));
        return true;
    }
}
