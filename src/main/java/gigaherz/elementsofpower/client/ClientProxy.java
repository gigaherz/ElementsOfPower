package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.ISideProxy;
import gigaherz.elementsofpower.Used;
import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.entities.EntityEssence;
import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import gigaherz.elementsofpower.essentializer.ContainerEssentializer;
import gigaherz.elementsofpower.essentializer.RenderEssentializer;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.GemstoneBlockType;
import gigaherz.elementsofpower.guidebook.GuiGuidebook;
import gigaherz.elementsofpower.items.ItemGemContainer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import gigaherz.elementsofpower.renders.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
        MinecraftForge.EVENT_BUS.register(new SpellRenderOverlay());
        MinecraftForge.EVENT_BUS.register(new TickEventWandControl());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        event.map.registerSprite(new ResourceLocation(ElementsOfPower.MODID + ":blocks/cone"));
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


    @Override
    public void handleEssentializerTileUpdate(EssentializerTileUpdate message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> handleEssentializerTileUpdate2(message));
    }

    @Override
    public void displayBook()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiGuidebook());
    }

    public void handleEssentializerTileUpdate2(EssentializerTileUpdate message)
    {
        TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.pos);
        if (te instanceof TileEssentializer)
        {
            TileEssentializer essentializer = (TileEssentializer) te;
            essentializer.setInventorySlotContents(0, message.activeItem);
            essentializer.remainingToConvert = message.remaining;
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
        registerBlockModelAsItem(ElementsOfPower.cocoon, "cocoon", "color=8,facing=down");

        registerItemModel(ElementsOfPower.analyzer, "analyzer");

        registerItemModel(ElementsOfPower.guidebook, "guidebook");

        registerItemModel(ElementsOfPower.fire, "magicOrb", "element=fire");
        registerItemModel(ElementsOfPower.water, "magicOrb", "element=water");
        registerItemModel(ElementsOfPower.air, "magicOrb", "element=air");
        registerItemModel(ElementsOfPower.earth, "magicOrb", "element=earth");
        registerItemModel(ElementsOfPower.light, "magicOrb", "element=light");
        registerItemModel(ElementsOfPower.darkness, "magicOrb", "element=dark");
        registerItemModel(ElementsOfPower.life, "magicOrb", "element=life");
        registerItemModel(ElementsOfPower.death, "magicOrb", "element=death");

        registerItemModel(ElementsOfPower.gemRuby, "gemstone", "gem=ruby");
        registerItemModel(ElementsOfPower.gemSapphire, "gemstone", "gem=sapphire");
        registerItemModel(ElementsOfPower.gemCitrine, "gemstone", "gem=citrine");
        registerItemModel(ElementsOfPower.gemAgate, "gemstone", "gem=agate");
        registerItemModel(ElementsOfPower.gemQuartz, "gemstone", "gem=quartz");
        registerItemModel(ElementsOfPower.gemSerendibite, "gemstone", "gem=serendibite");
        registerItemModel(ElementsOfPower.gemEmerald, "gemstone", "gem=emerald");
        registerItemModel(ElementsOfPower.gemAmethyst, "gemstone", "gem=amethyst");
        registerItemModel(ElementsOfPower.gemDiamond, "gemstone", "gem=diamond");

        for (GemstoneBlockType b : GemstoneBlockType.values)
        {
            registerBlockModelAsItem(ElementsOfPower.gemstoneBlock, b.ordinal(), "gemstoneBlock", "type=" + b.getName());
            registerBlockModelAsItem(ElementsOfPower.gemstoneOre, b.ordinal(), "gemstoneOre", "type=" + b.getName());
        }

        registerGemMeshDefinition(ElementsOfPower.magicRing, "magicRing");
        registerGemMeshDefinition(ElementsOfPower.magicWand, "magicWand");
        registerGemMeshDefinition(ElementsOfPower.magicStaff, "magicStaff");
    }

    private void registerGemMeshDefinition(Item item, String itemName)
    {
        ModelLoader.setCustomMeshDefinition(item, new GemContainerMeshDefinition(item, itemName));
    }

    public void registerBlockModelAsItem(final Block block, final String blockName)
    {
        registerBlockModelAsItem(block, 0, blockName);
    }

    public void registerBlockModelAsItem(final Block block, final String blockName, final String variantName)
    {
        registerBlockModelAsItem(block, 0, blockName, variantName);
    }

    public void registerBlockModelAsItem(final Block block, int meta, final String blockName)
    {
        registerBlockModelAsItem(block, meta, blockName, "inventory");
    }

    public void registerBlockModelAsItem(final Block block, int meta, final String blockName, final String variantName)
    {
        registerItemModel(Item.getItemFromBlock(block), meta, blockName, variantName);
    }

    public void registerItemModel(final ItemStack stack, final String itemName, final String variantName)
    {
        registerItemModel(stack.getItem(), stack.getMetadata(), itemName, variantName);
    }

    public void registerItemModel(final Item item, final String itemName)
    {
        registerItemModel(item, 0, itemName, "inventory");
    }

    public void registerItemModel(final Item item, int meta, final String itemName, final String variantName)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, variantName));
    }

    // ----------------------------------------------------------- Entity Renderers
    public void registerEntityRenderers()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEssentializer.class, new RenderEssentializer());

        RenderingRegistry.registerEntityRenderingHandler(EntityBall.class, RenderBall::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEssence.class, RenderEssence::new);

        RenderingStuffs.init();
    }

    private class GemContainerMeshDefinition implements ItemMeshDefinition
    {
        final String itemName;

        private GemContainerMeshDefinition(Item item, String itemName)
        {
            this.itemName = itemName;

            ResourceLocation[] resLocs = new ResourceLocation[Gemstone.values.length + 1];
            for (int i = 0; i < Gemstone.values.length; i++)
            { resLocs[i] = getModelResourceLocation(Gemstone.values[i]); }
            resLocs[Gemstone.values.length] = getModelResourceLocation(null);
            ModelBakery.registerItemVariants(item, resLocs);
        }

        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            Item item = stack.getItem();
            if (!(item instanceof ItemGemContainer))
                return null;
            ItemGemContainer c = (ItemGemContainer) item;

            Gemstone g = c.getGemstone(stack);

            return getModelResourceLocation(g);
        }

        private ModelResourceLocation getModelResourceLocation(Gemstone g)
        {
            String variantName = "gem=" + (g != null ? g.getName() : "unbound");

            return new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, variantName);
        }
    }
}
