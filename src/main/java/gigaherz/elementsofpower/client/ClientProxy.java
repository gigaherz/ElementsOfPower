package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.CommonProxy;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.models.CustomMeshModel;
import gigaherz.elementsofpower.models.IModelRegistrationHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerCustomBakedModels(IModelRegistrationHelper registrationHelper) {
        registerCustomModel(registrationHelper, ElementsOfPower.magicWand, 0, "wand_lapis");
        registerCustomModel(registrationHelper, ElementsOfPower.magicWand, 1, "wand_emerald");
        registerCustomModel(registrationHelper, ElementsOfPower.magicWand, 2, "wand_diamond");
        registerCustomModel(registrationHelper, ElementsOfPower.magicWand, 3, "wand_creative");
        registerCustomModel(registrationHelper, ElementsOfPower.magicWand, 4, "staff_lapis");
        registerCustomModel(registrationHelper, ElementsOfPower.magicWand, 5, "staff_emerald");
        registerCustomModel(registrationHelper, ElementsOfPower.magicWand, 6, "staff_diamond");
        registerCustomModel(registrationHelper, ElementsOfPower.magicWand, 7, "staff_creative");
    }

    public void registerCustomModel(IModelRegistrationHelper registrationHelper, final Item item, int meta, final String itemName) {

        ItemCameraTransforms transforms = ItemCameraTransforms.DEFAULT;

        ItemTransformVec3f firstPerson = new ItemTransformVec3f(new Vector3f(-30f,0,-20f), new Vector3f(0,0,0), new Vector3f(2.5f,2.5f,2.5f));
        ItemTransformVec3f thirdPerson = new ItemTransformVec3f(new Vector3f(-30f,0,-20f), new Vector3f(0,0,0), new Vector3f(1.5f,1.5f,1.5f));
        ItemTransformVec3f gui = new ItemTransformVec3f(new Vector3f(0,0,-45f), new Vector3f(0,0,0), new Vector3f(1.2f,1.2f,1.2f));

        transforms = new ItemCameraTransforms(thirdPerson, firstPerson, transforms.head, gui);

        ResourceLocation icon = new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory");
        registrationHelper.registerCustomModel(icon,
                new CustomMeshModel(itemName, transforms, icon, registrationHelper.getModelManager()));
        ModelBakery.addVariantName(item, ElementsOfPower.MODID + ":" + itemName);
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
