package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.ISideProxy;
import gigaherz.elementsofpower.entities.EntityBallBase;
import gigaherz.elementsofpower.entities.EntityTeleporter;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.models.ObjModelLoader;
import gigaherz.elementsofpower.renders.PlayerBeamRenderOverlay;
import gigaherz.elementsofpower.renders.RenderBall;
import gigaherz.elementsofpower.renders.RenderEssentializer;
import gigaherz.elementsofpower.util.Used;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Used
public class ClientProxy implements ISideProxy
{
    public void preInit()
    {
        OBJLoader.instance.addDomain(ElementsOfPower.MODID);

        registerClientEvents();
        registerCustomBakedModels();
        registerModels();
    }

    public void init()
    {
        registerEntityRenderers();
        registerParticle();
    }

    public void registerParticle()
    {
        Minecraft.getMinecraft().effectRenderer.registerParticle(ElementsOfPower.SMALL_CLOUD_PARTICLE_ID, new EntitySmallCloudFX.Factory());
    }

    public void registerClientEvents()
    {
        MinecraftForge.EVENT_BUS.register(new GuiOverlayMagicContainer());
        MinecraftForge.EVENT_BUS.register(new MagicTooltips());
        MinecraftForge.EVENT_BUS.register(new PlayerBeamRenderOverlay());
    }

    // ----------------------------------------------------------- Item/Block Custom OBJ Models
    public void registerCustomBakedModels()
    {
        ObjModelLoader.instance.setExplicitOverride(new ResourceLocation(ElementsOfPower.MODID, "models/item/thing"));
        ObjModelLoader.instance.setExplicitOverride(new ResourceLocation(ElementsOfPower.MODID, "models/item/wand_lapis"));
        ObjModelLoader.instance.setExplicitOverride(new ResourceLocation(ElementsOfPower.MODID, "models/item/wand_emerald"));
        ObjModelLoader.instance.setExplicitOverride(new ResourceLocation(ElementsOfPower.MODID, "models/item/wand_diamond"));
        ObjModelLoader.instance.setExplicitOverride(new ResourceLocation(ElementsOfPower.MODID, "models/item/wand_creative"));
        ObjModelLoader.instance.setExplicitOverride(new ResourceLocation(ElementsOfPower.MODID, "models/item/staff_lapis"));
        ObjModelLoader.instance.setExplicitOverride(new ResourceLocation(ElementsOfPower.MODID, "models/item/staff_emerald"));
        ObjModelLoader.instance.setExplicitOverride(new ResourceLocation(ElementsOfPower.MODID, "models/item/staff_diamond"));
        ObjModelLoader.instance.setExplicitOverride(new ResourceLocation(ElementsOfPower.MODID, "models/item/staff_creative"));
    }

    // ----------------------------------------------------------- Item/Block Models
    public void registerModels()
    {
        registerBlockModelAsItem(ElementsOfPower.essentializer, "essentializer");
        registerBlockModelAsItem(ElementsOfPower.dust, "dust");

        registerItemModel(ElementsOfPower.magicOrb, 0, "orb_fire");
        registerItemModel(ElementsOfPower.magicOrb, 1, "orb_water");
        registerItemModel(ElementsOfPower.magicOrb, 2, "orb_air");
        registerItemModel(ElementsOfPower.magicOrb, 3, "orb_earth");
        registerItemModel(ElementsOfPower.magicOrb, 4, "orb_light");
        registerItemModel(ElementsOfPower.magicOrb, 5, "orb_dark");
        registerItemModel(ElementsOfPower.magicOrb, 6, "orb_life");
        registerItemModel(ElementsOfPower.magicOrb, 7, "orb_death");
        registerItemModel(ElementsOfPower.magicWand, 0, "wand_lapis");
        registerItemModel(ElementsOfPower.magicWand, 1, "wand_emerald");
        registerItemModel(ElementsOfPower.magicWand, 2, "wand_diamond");
        registerItemModel(ElementsOfPower.magicWand, 3, "wand_creative");
        registerItemModel(ElementsOfPower.magicWand, 4, "staff_lapis");
        registerItemModel(ElementsOfPower.magicWand, 5, "staff_emerald");
        registerItemModel(ElementsOfPower.magicWand, 6, "staff_diamond");
        registerItemModel(ElementsOfPower.magicWand, 7, "staff_creative");
        registerItemModel(ElementsOfPower.magicContainer, 0, "container_lapis");
        registerItemModel(ElementsOfPower.magicContainer, 1, "container_emerald");
        registerItemModel(ElementsOfPower.magicContainer, 2, "container_diamond");
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
        ModelBakery.addVariantName(item, ElementsOfPower.MODID + ":" + itemName);
    }

    // ----------------------------------------------------------- Entity Renderers
    public void registerEntityRenderers()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEssentializer.class, new RenderEssentializer());

        registerEntityRenderingHandler(EntityTeleporter.class,
                new RenderBall(Minecraft.getMinecraft().getRenderManager()));
        registerEntityRenderingHandler(EntityBallBase.class,
                new RenderBall(Minecraft.getMinecraft().getRenderManager()));

        //RendererLivingEntity rplayer = ReflectionHelper.getPrivateValue(RenderManager.class, Minecraft.getMinecraft().getRenderManager(), "field_178637_m", "playerRenderer");
        //rplayer.addLayer(new PlayerBeamRenderOverlay());
    }

    public void registerEntityRenderingHandler(Class<? extends Entity> entityClass, Render<? extends Entity> render)
    {
        RenderingRegistry.registerEntityRenderingHandler(entityClass, render);
    }
}
