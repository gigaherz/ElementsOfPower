package gigaherz.elementsofpower.client;

import com.google.common.base.Charsets;
import gigaherz.elementsofpower.CommonProxy;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.models.CustomMeshModel;
import gigaherz.elementsofpower.models.IModelRegistrationHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.io.*;
import java.util.Hashtable;
import java.util.Map;

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

    private ResourceLocation getModelLocation(ResourceLocation loc)
    {
        return new ResourceLocation(loc.getResourceDomain(), "models/" + loc.getResourcePath() + ".json");
    }

    private ModelBlock loadModelResource(Map<ResourceLocation, ModelBlock> map, final ResourceLocation loc)
    {
        Reader reader = null;
        ModelBlock modelblock = map.get(loc);

        if(modelblock != null)
            return modelblock;

        if(loc.getResourcePath().startsWith("builtin/"))
            return null;

        try
        {
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(getModelLocation(loc));
            if(iresource != null) {
                reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);

                modelblock = ModelBlock.deserialize(reader);
                modelblock.name = loc.toString();
                map.put(loc, modelblock);

                ResourceLocation parentLoc = modelblock.getParentLocation();
                if(parentLoc != null) {

                    ModelBlock parentModel = loadModelResource(map, parentLoc);
                    if(parentModel != null) {
                        modelblock.getParentFromMap(map);
                    }
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return modelblock;
    }
    private ModelBlock loadModelResource(final ResourceLocation loc)
    {
        return loadModelResource(new Hashtable<ResourceLocation, ModelBlock>(), loc);
    }

    private void registerCustomModel(IModelRegistrationHelper registrationHelper, final Item item, int meta, final String itemName) {

        ItemCameraTransforms transforms = ItemCameraTransforms.DEFAULT;

        ResourceLocation icon = new ResourceLocation(ElementsOfPower.MODID, "item/" + itemName);
        ModelBlock modelblock = loadModelResource(icon);
        if(modelblock != null) {
            transforms = new ItemCameraTransforms(
                    modelblock.getThirdPersonTransform(),
                    modelblock.getFirstPersonTransform(),
                    modelblock.getHeadTransform(),
                    modelblock.getInGuiTransform());
        }

        ResourceLocation loc = new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory");
        registrationHelper.registerCustomModel(loc,
                new CustomMeshModel(itemName, transforms, loc, registrationHelper.getModelManager()));
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
