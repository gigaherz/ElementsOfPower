package gigaherz.elementsofpower;

import gigaherz.elementsofpower.models.ModelRegistrationHelper;
import gigaherz.elementsofpower.network.ProgressUpdatePacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CommonProxy
        implements IMessageHandler {

    // Client stuff
    public void registerRenderers() {
        // Nothing here as this is the server side proxy
    }

    public void registerCustomBakedModels(ModelRegistrationHelper helper) {
        // Nothing here, client-only
    }

    @Override
    public IMessage onMessage(IMessage message, MessageContext ctx) {
        if (message instanceof ProgressUpdatePacket) {
            ProgressUpdatePacket packet = (ProgressUpdatePacket) message;

            TileEntity tile = packet.getTileEntityTarget();

            if (!(tile instanceof TileEssentializer)) {
                return null;
            }

            TileEssentializer essentializer = (TileEssentializer) tile;
            essentializer.updateProgressBar(packet.barIndex, packet.barValue);
        }
        return null;
    }

/*
    public static final int MSGID_PROGRESS = 0;
    public static final int MSGID_MAGIC = 1;

    public void onPacketData(INetworkManager manager, Packet250CustomPayload payload, EntityPlayer player)
    {
        if (payload.channel.equals(ElementsOfPower.ChannelName))
        {
        	if(payload.data[0] == MSGID_PROGRESS)
        		this.handleProgressPacket(payload, (EntityPlayer)player);
        	else if(payload.data[0] == MSGID_MAGIC)
        		this.handleMagicPacket(payload, (EntityPlayer)player);
        }
    }
    private void handleProgressPacket(Packet250CustomPayload payload, EntityPlayer sender)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(payload.data);
        DataInputStream ird = new DataInputStream(bis);
        
        try
        {
            ird.skip(1);
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
        	outputStream.writeByte(MSGID_PROGRESS);
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

        if (this instanceof ClientProxy)
        {
            PacketDispatcher.sendPacketToServer(packet);
        }
        else
        {
            PacketDispatcher.sendPacketToAllAround(entity.xCoord, entity.yCoord, entity.zCoord, 12, entity.worldObj.provider.dimensionId, packet);
        }
    }

    private void handleMagicPacket(Packet250CustomPayload payload, EntityPlayer sender)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(payload.data);
        DataInputStream ird = new DataInputStream(bis);
        
        try
        {
            ird.skip(1);
            int dim = ird.readInt();
            int charge = ird.readInt();
            World world = DimensionManager.getWorld(dim);

            ItemStack stack = sender.inventory.getCurrentItem();
            
            Item item = stack.getItem();
            
            if (!(item instanceof ItemMagicContainer))
            {
                return;
            }

            ItemMagicContainer magic = (ItemMagicContainer)item;
            

            magic.onMagicItemReleased(stack, world, sender, charge);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

	public void sendMagicItemPacket(ItemStack stack, World world,
			EntityPlayer player, int remaining)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
        DataOutputStream outputStream = new DataOutputStream(bos);

        try
        {
        	outputStream.writeByte(MSGID_MAGIC);
            outputStream.writeInt(world.getWorldInfo().getDimension());
            outputStream.writeInt(remaining);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = ElementsOfPower.ChannelName;
        packet.data = bos.toByteArray();
        packet.length = bos.size();

        if (this instanceof ClientProxy)
        {
            PacketDispatcher.sendPacketToServer(packet);
        }
	}*/
}
