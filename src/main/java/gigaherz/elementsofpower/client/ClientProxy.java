package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.CommonProxy;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.models.CustomMeshModel;
import gigaherz.elementsofpower.models.ModelRegistrationHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerCustomBakedModels() {
        ModelRegistrationHelper helper = ElementsOfPower.modelRegistrationHelper;

        registerCustomItemModel(helper, "wand_lapis");
        registerCustomItemModel(helper, "wand_emerald");
        registerCustomItemModel(helper, "wand_diamond");
        registerCustomItemModel(helper, "wand_creative");
        registerCustomItemModel(helper, "staff_lapis");
        registerCustomItemModel(helper, "staff_emerald");
        registerCustomItemModel(helper, "staff_diamond");
        registerCustomItemModel(helper, "staff_creative");

        //registerCustomBlockModel(helper, "essentializer", "normal");
        //registerCustomBlockModel(helper, "essentializer", "inventory");
    }

    public void registerCustomItemModel(ModelRegistrationHelper helper, final String itemName) {

        ResourceLocation loc = new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory");
        IBakedModel model = new CustomMeshModel(itemName);

        helper.registerCustomItemModel(loc, model, itemName);
    }

    public void registerCustomBlockModel(ModelRegistrationHelper helper, final String blockName, final String stateName) {

        ResourceLocation loc = new ModelResourceLocation(ElementsOfPower.MODID + ":" + blockName, stateName);
        IBakedModel model = new CustomMeshModel(blockName);

        helper.registerCustomBlockModel(loc, model, blockName);
    }

    @Override
    public void registerRenderers() {
        registerBlockTexture(ElementsOfPower.essentializer, "essentializer");
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

    public void registerBlockTexture(final Block block, final String blockName) {
        registerBlockTexture(block, 0, blockName);
    }

    public void registerBlockTexture(final Block block, int meta, final String blockName) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(block), meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + blockName, "inventory"));
    }

    public void registerItemTexture(final Item item, final String itemName) {
        registerItemTexture(item, 0, itemName);
    }

    public void registerItemTexture(final Item item, int meta, final String itemName) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.getItemModelMesher().register(item, meta, new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory"));
        ModelBakery.addVariantName(item, ElementsOfPower.MODID + ":" + itemName);
    }
}
