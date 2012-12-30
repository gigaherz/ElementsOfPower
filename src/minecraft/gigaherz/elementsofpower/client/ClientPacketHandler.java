package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.EssentializerTile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload payload, Player player)
    {
        if (payload.channel.equals("WorkerCommand"))
        {
            this.handleMachineUpdate(payload);
        }
    }

    private void handleMachineUpdate(Packet250CustomPayload payload)
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
}