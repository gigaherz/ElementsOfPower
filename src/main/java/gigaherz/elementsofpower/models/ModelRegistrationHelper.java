package gigaherz.elementsofpower.models;

import com.google.common.base.Charsets;
import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Map;

public class ModelRegistrationHelper {

    ModelManager modelManager;
    IRegistry modelRegistry;
    ModelBakery modelBakery;

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        modelManager = event.modelManager;
        modelRegistry = event.modelRegistry;
        modelBakery = event.modelBakery;

        ElementsOfPower.instance.proxy.registerCustomBakedModels(this);
    }

    public void registerCustomModel(ResourceLocation location, IBakedModel bakedModel) {
        modelRegistry.putObject(location, bakedModel);
    }

    private ResourceLocation getModelLocation(ResourceLocation loc) {
        return new ResourceLocation(loc.getResourceDomain(), "models/" + loc.getResourcePath() + ".json");
    }

    private ModelBlock loadModelResource(Map<ResourceLocation, ModelBlock> map, final ResourceLocation loc) {
        Reader reader = null;
        ModelBlock modelblock = map.get(loc);

        if (modelblock != null)
            return modelblock;

        if (loc.getResourcePath().startsWith("builtin/"))
            return null;

        try {
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(getModelLocation(loc));
            if (iresource != null) {
                reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);

                modelblock = ModelBlock.deserialize(reader);
                modelblock.name = loc.toString();
                map.put(loc, modelblock);

                ResourceLocation parentLoc = modelblock.getParentLocation();
                if (parentLoc != null) {

                    ModelBlock parentModel = loadModelResource(map, parentLoc);
                    if (parentModel != null) {
                        modelblock.getParentFromMap(map);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return modelblock;
    }

    private ModelBlock loadModelResource(final ResourceLocation loc) {
        return loadModelResource(new Hashtable<ResourceLocation, ModelBlock>(), loc);
    }

    public void registerCustomItemModel(final Item item, int meta, final String itemName) {

        ItemCameraTransforms transforms = ItemCameraTransforms.DEFAULT;

        ResourceLocation icon = new ResourceLocation(ElementsOfPower.MODID, "item/" + itemName);
        ModelBlock modelblock = loadModelResource(icon);
        if (modelblock != null) {
            transforms = new ItemCameraTransforms(
                    modelblock.getThirdPersonTransform(),
                    modelblock.getFirstPersonTransform(),
                    modelblock.getHeadTransform(),
                    modelblock.getInGuiTransform());
        }

        ResourceLocation loc = new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, "inventory");
        registerCustomModel(loc, new CustomMeshModel(itemName, transforms, loc, modelManager));
        ModelBakery.addVariantName(item, ElementsOfPower.MODID + ":" + itemName);
    }

    public void registerCustomBlockModel(final String itemName, final String stateName) {

        ItemCameraTransforms transforms = ItemCameraTransforms.DEFAULT;

        ResourceLocation icon = new ResourceLocation(ElementsOfPower.MODID, "block/" + itemName);
        ModelBlock modelblock = loadModelResource(icon);
        if (modelblock != null) {
            transforms = new ItemCameraTransforms(
                    modelblock.getThirdPersonTransform(),
                    modelblock.getFirstPersonTransform(),
                    modelblock.getHeadTransform(),
                    modelblock.getInGuiTransform());
        }

        ResourceLocation loc = new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, stateName);
        registerCustomModel(loc, new CustomMeshModel(itemName, transforms, loc, modelManager));
    }

}
