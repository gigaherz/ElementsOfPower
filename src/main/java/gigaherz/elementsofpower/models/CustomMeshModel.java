package gigaherz.elementsofpower.models;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ISmartItemModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomMeshModel
        implements ISmartItemModel {

    ResourceLocation model;
    ItemCameraTransforms transforms;

    List<BakedQuad> faceQuads;
    List<BakedQuad> generalQuads;
    MeshModel sourceMesh;

    TextureAtlasSprite iconSprite;

    public CustomMeshModel(String variant, ItemCameraTransforms cameraTransforms, ResourceLocation icon, ModelManager modelManager) {
        this.model = new ResourceLocation(ElementsOfPower.MODID, "models/obj/" + variant + ".obj");
        this.transforms = cameraTransforms;
        this.faceQuads = new ArrayList<BakedQuad>();
        this.generalQuads = new ArrayList<BakedQuad>();
        this.iconSprite = modelManager.getTextureMap().getAtlasSprite(icon.toString());

        //ItemCameraTransforms.TransformType
        //WeightedBakedModel

        try {
            generalQuads.clear();
            sourceMesh = new MeshLoader().loadFromResource(modelManager, model);
            generalQuads = sourceMesh.bakeModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack) {
        // TODO: Handle GUI rendering separately
        return this;
    }

    @Override
    public List getFaceQuads(EnumFacing face) {
        return faceQuads;
    }

    @Override
    public List getGeneralQuads() {
        return generalQuads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getTexture() {
        return iconSprite;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return transforms;
    }

}
