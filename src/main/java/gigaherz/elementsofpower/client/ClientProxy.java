package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.renderers.MagicContainerOverlay;
import gigaherz.elementsofpower.common.IModProxy;
import gigaherz.elementsofpower.common.Used;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.essentializer.gui.ContainerEssentializer;
import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;

@Used
@Mod.EventBusSubscriber(value=Side.CLIENT, modid=ElementsOfPower.MODID)
public class ClientProxy implements IModProxy
{
    public ClientProxy()
    {
        registerClientEvents();
    }

    private void registerClientEvents()
    {
        MinecraftForge.EVENT_BUS.register(new TickEventWandControl());
        MinecraftForge.EVENT_BUS.register(new MagicContainerOverlay());
    }

    @Override
    public void handleSpellcastSync(SpellcastSync message)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            World world = Minecraft.getMinecraft().world;
            EntityPlayer player = (EntityPlayer) world.getEntityByID(message.casterID);
            SpellcastEntityData data = SpellcastEntityData.get(player);

            data.sync(message.changeMode, message.spellcast);
        });
    }

    @Override
    public void handleRemainingAmountsUpdate(EssentializerAmountsUpdate message)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            EntityPlayer player = Minecraft.getMinecraft().player;

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
    }

    @Override
    public void handleEssentializerTileUpdate(EssentializerTileUpdate message)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
            if (te instanceof TileEssentializer)
            {
                TileEssentializer essentializer = (TileEssentializer) te;
                essentializer.getInventory().setStackInSlot(0, message.activeItem);
                essentializer.remainingToConvert = message.remaining;
            }
        });
    }

    @Override
    public void handleAddVelocity(AddVelocityPlayer message)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
                Minecraft.getMinecraft().player.addVelocity(message.vx, message.vy, message.vz));
    }

    @Override
    public void beginTracking(EntityPlayer playerIn, EnumHand hand)
    {
        TickEventWandControl.instance.handInUse = hand;
        playerIn.setActiveHand(hand);
    }
}
