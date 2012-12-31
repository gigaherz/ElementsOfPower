package gigaherz.elementsofpower;

import gigaherz.elementsofpower.client.ClientProxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class CommonProxy implements IPacketHandler
{
    public static String ITEMS_PNG = "/gigaherz/elementsofpower/items.png";
    public static String BLOCK_PNG = "/gigaherz/elementsofpower/block.png";

    // Client stuff
    public void registerRenderers()
    {
        // Nothing here as this is the server side proxy
    }
    
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload payload, Player player)
    {
    	if (payload.channel.equals(ElementsOfPower.ChannelName))
        {
            this.handleMachineUpdate(payload, (EntityPlayer)player);
        }
    }

    private void handleMachineUpdate(Packet250CustomPayload payload, EntityPlayer sender)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(payload.data);
        DataInputStream ird = new DataInputStream(bis);

        try
        {
            int dim = ird.readInt();
            int x = ird.readInt();
            int y = ird.readInt();
            int z = ird.readInt();
            int index = ird.readInt();
            int value = ird.readInt();
            World world = DimensionManager.getWorld(dim);
            TileEntity tile = world.getBlockTileEntity(x, y, z);

            if (!(tile instanceof EssentializerTile))
            {
                return;
            }

            EssentializerTile grinder = (EssentializerTile)tile;
            grinder.updateProgressBar(index, value);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void sendProgressBarUpdate(TileEntity entity, int bar, int value)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
        DataOutputStream outputStream = new DataOutputStream(bos);

        try
        {
            outputStream.writeInt(entity.worldObj.getWorldInfo().getDimension());
            outputStream.writeInt(entity.xCoord);
            outputStream.writeInt(entity.yCoord);
            outputStream.writeInt(entity.zCoord);
            outputStream.writeInt(bar);
            outputStream.writeInt(value);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = ElementsOfPower.ChannelName;
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        
        if(this instanceof ClientProxy)
        {
        	PacketDispatcher.sendPacketToServer(packet);
        }
        else
        {
        	PacketDispatcher.sendPacketToAllAround(entity.xCoord, entity.yCoord, entity.zCoord, 12, entity.worldObj.provider.dimensionId, packet);
        }
    }    
}
