package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.ISideProxy;
import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.entities.EntityEssence;
import gigaherz.elementsofpower.entities.EntityTeleporter;
import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import gigaherz.elementsofpower.essentializer.ContainerEssentializer;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import gigaherz.elementsofpower.renders.*;
import gigaherz.elementsofpower.util.Used;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

@Used
public class ClientProxy implements ISideProxy
{
    public void preInit()
    {
        OBJLoader.instance.addDomain(ElementsOfPower.MODID);

        registerClientEvents();
        registerModels();
        registerEntityRenderers();
    }

    public void init()
    {
        registerParticle();
    }

    public void registerClientEvents()
    {
        MinecraftForge.EVENT_BUS.register(new MagicContainerOverlay());
        MinecraftForge.EVENT_BUS.register(new MagicTooltips());
        MinecraftForge.EVENT_BUS.register(new PlayerBeamRenderOverlay());
        MinecraftForge.EVENT_BUS.register(new TickEventWandControl());
    }

    @Override
    public void handleSpellcastSync(SpellcastSync message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> handleSpellcastSync2(message));
    }

    public void handleSpellcastSync2(SpellcastSync message)
    {
        World world = Minecraft.getMinecraft().theWorld;
        EntityPlayer player = (EntityPlayer) world.getEntityByID(message.casterID);
        SpellcastEntityData data = SpellcastEntityData.get(player);

        if (data != null)
            data.sync(message.changeMode, message.spellcast);
    }

    @Override
    public void handleRemainingAmountsUpdate(EssentializerAmountsUpdate message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> handleRemainingAmountsUpdate2(message));
    }

    private void handleRemainingAmountsUpdate2(EssentializerAmountsUpdate message)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        if (message.windowId == -1)
            return;

        if (message.windowId == player.openContainer.windowId)
        {
            if (!(player.openContainer instanceof ContainerEssentializer))
                return;
            ((ContainerEssentializer) player.openContainer).updateAmounts(message.contained, message.remaining);
        }
    }

    public void registerParticle()
    {
        Minecraft.getMinecraft().effectRenderer.registerParticle(ElementsOfPower.SMALL_CLOUD_PARTICLE_ID, new EntitySmallCloudFX.Factory());
    }

    // ----------------------------------------------------------- Item/Block Models
    public void registerModels()
    {
        registerBlockModelAsItem(ElementsOfPower.essentializer, "essentializer");
        registerBlockModelAsItem(ElementsOfPower.dust, "dust");

        registerItemModel(ElementsOfPower.magicOrb, 0, "magicOrb", "element=fire");
        registerItemModel(ElementsOfPower.magicOrb, 1, "magicOrb", "element=water");
        registerItemModel(ElementsOfPower.magicOrb, 2, "magicOrb", "element=air");
        registerItemModel(ElementsOfPower.magicOrb, 3, "magicOrb", "element=earth");
        registerItemModel(ElementsOfPower.magicOrb, 4, "magicOrb", "element=light");
        registerItemModel(ElementsOfPower.magicOrb, 5, "magicOrb", "element=dark");
        registerItemModel(ElementsOfPower.magicOrb, 6, "magicOrb", "element=life");
        registerItemModel(ElementsOfPower.magicOrb, 7, "magicOrb", "element=death");
        registerItemModel(ElementsOfPower.magicWand, 0, "magicWand", "gem=lapis,type=wand");
        registerItemModel(ElementsOfPower.magicWand, 1, "magicWand", "gem=emerald,type=wand");
        registerItemModel(ElementsOfPower.magicWand, 2, "magicWand", "gem=diamond,type=wand");
        registerItemModel(ElementsOfPower.magicWand, 3, "magicWand", "gem=creative,type=wand");
        registerItemModel(ElementsOfPower.magicWand, 4, "magicWand", "gem=lapis,type=staff");
        registerItemModel(ElementsOfPower.magicWand, 5, "magicWand", "gem=emerald,type=staff");
        registerItemModel(ElementsOfPower.magicWand, 6, "magicWand", "gem=diamond,type=staff");
        registerItemModel(ElementsOfPower.magicWand, 7, "magicWand", "gem=creative,type=staff");
        registerItemModel(ElementsOfPower.magicContainer, 0, "magicContainer", "gem=lapis");
        registerItemModel(ElementsOfPower.magicContainer, 1, "magicContainer", "gem=emerald");
        registerItemModel(ElementsOfPower.magicContainer, 2, "magicContainer", "gem=diamond");
        registerItemModel(ElementsOfPower.magicRing, 0, "magicRing", "gem=lapis");
        registerItemModel(ElementsOfPower.magicRing, 1, "magicRing", "gem=emerald");
        registerItemModel(ElementsOfPower.magicRing, 2, "magicRing", "gem=diamond");
        registerItemModel(ElementsOfPower.magicRing, 3, "magicRing", "gem=creative");
    }

    public void registerBlockModelAsItem(final Block block, final String blockName)
    {
        registerBlockModelAsItem(block, 0, blockName);
    }

    public void registerBlockModelAsItem(final Block block, int meta, final String blockName)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + blockName, "inventory"));
    }

    public void registerItemModel(final Item item, int meta, final String itemName)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory"));
    }

    public void registerItemModel(final Item item, int meta, final String itemName, final String variantName)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, variantName));
    }

    // ----------------------------------------------------------- Entity Renderers
    public void registerEntityRenderers()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEssentializer.class, new RenderEssentializer());

        RenderingRegistry.registerEntityRenderingHandler(EntityTeleporter.class, RenderBall::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBall.class, RenderBall::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEssence.class, RenderEssence::new);

        RenderingStuffs.init();
    }
}
