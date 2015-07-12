package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.ISideProxy;
import gigaherz.elementsofpower.client.render.RenderEntityProvidedStack;
import gigaherz.elementsofpower.entities.EntityBallBase;
import gigaherz.elementsofpower.models.ObjModelRegistrationHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.b3d.B3DLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy implements ISideProxy
{
    public void preInit()
    {
        registerClientEvents();
        registerCustomBakedModels();
        registerModels();

        B3DLoader l;
    }

    public void init()
    {
        registerEntityRenderers();
    }

    public void registerClientEvents()
    {
        MinecraftForge.EVENT_BUS.register(new GuiOverlayMagicContainer());
    }

    // ----------------------------------------------------------- Item/Block Custom OBJ Models
    public void registerCustomBakedModels()
    {
        registerCustomItemModel("wand_lapis");
        registerCustomItemModel("wand_emerald");
        registerCustomItemModel("wand_diamond");
        registerCustomItemModel("wand_creative");
        registerCustomItemModel("staff_lapis");
        registerCustomItemModel("staff_emerald");
        registerCustomItemModel("staff_diamond");
        registerCustomItemModel("staff_creative");
    }

    public void registerCustomItemModel(final String itemName)
    {
        ObjModelRegistrationHelper.instance.registerCustomModel(
                new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory"),
                new ResourceLocation(ElementsOfPower.MODID + ":item/" + itemName));
    }

    public void registerCustomBlockModel(final String blockName, final String stateName)
    {
        ObjModelRegistrationHelper.instance.registerCustomModel(
                new ModelResourceLocation(ElementsOfPower.MODID + ":" + blockName, stateName),
                new ResourceLocation(ElementsOfPower.MODID + ":block/" + blockName));
    }

    // ----------------------------------------------------------- Item/Block Models
    public void registerModels()
    {

        MinecraftForge.EVENT_BUS.register(new MagicTooltips());

        registerBlockTexture(ElementsOfPower.essentializer, "essentializer");
        registerBlockTexture(ElementsOfPower.dust, "dust");

        registerItemTexture(ElementsOfPower.magicOrb, 0, "orb_fire");
        registerItemTexture(ElementsOfPower.magicOrb, 1, "orb_water");
        registerItemTexture(ElementsOfPower.magicOrb, 2, "orb_air");
        registerItemTexture(ElementsOfPower.magicOrb, 3, "orb_earth");
        registerItemTexture(ElementsOfPower.magicOrb, 4, "orb_light");
        registerItemTexture(ElementsOfPower.magicOrb, 5, "orb_dark");
        registerItemTexture(ElementsOfPower.magicOrb, 6, "orb_life");
        registerItemTexture(ElementsOfPower.magicOrb, 7, "orb_death");
        registerItemTexture(ElementsOfPower.magicWand, 0, "wand_lapis");
        registerItemTexture(ElementsOfPower.magicWand, 1, "wand_emerald");
        registerItemTexture(ElementsOfPower.magicWand, 2, "wand_diamond");
        registerItemTexture(ElementsOfPower.magicWand, 3, "wand_creative");
        registerItemTexture(ElementsOfPower.magicWand, 4, "staff_lapis");
        registerItemTexture(ElementsOfPower.magicWand, 5, "staff_emerald");
        registerItemTexture(ElementsOfPower.magicWand, 6, "staff_diamond");
        registerItemTexture(ElementsOfPower.magicWand, 7, "staff_creative");
        registerItemTexture(ElementsOfPower.magicContainer, 0, "container_lapis");
        registerItemTexture(ElementsOfPower.magicContainer, 1, "container_emerald");
        registerItemTexture(ElementsOfPower.magicContainer, 2, "container_diamond");
    }

    public void registerBlockTexture(final Block block, final String blockName)
    {
        registerBlockTexture(block, 0, blockName);
    }

    public void registerBlockTexture(final Block block, int meta, final String blockName)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + blockName, "inventory"));
    }

    public void registerItemTexture(final Item item, int meta, final String itemName)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory"));
        ModelBakery.addVariantName(item, ElementsOfPower.MODID + ":" + itemName);
    }

    // ----------------------------------------------------------- Entity Renderers
    public void registerEntityRenderers()
    {
        registerEntityRenderingHandler(EntityBallBase.class);
    }

    public void registerEntityRenderingHandler(Class<? extends Entity> entityClass)
    {
        registerEntityRenderingHandler(entityClass,
                new RenderEntityProvidedStack(
                        Minecraft.getMinecraft().getRenderManager(),
                        Minecraft.getMinecraft().getRenderItem()));
    }

    public void registerEntityRenderingHandler(Class<? extends Entity> entityClass, Render render)
    {
        RenderingRegistry.registerEntityRenderingHandler(entityClass, render);
    }
}
