package gigaherz.elementsofpower.models;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomMeshModel
        implements IFlexibleBakedModel, ISmartItemModel, ISmartBlockModel, IInitializeBakedModel , IPerspectiveAwareModel {

    String variant;
    ResourceLocation model;

    TRSRTransformation thirdPerson;
    TRSRTransformation firstPerson;
    TRSRTransformation head;
    TRSRTransformation gui;

    List<BakedQuad> faceQuads;
    List<BakedQuad> generalQuads;
    MeshModel sourceMesh;

    TextureAtlasSprite iconSprite;

    public CustomMeshModel(String variant) {
        this.variant = variant;
        this.model = new ResourceLocation(ElementsOfPower.MODID, "models/obj/" + variant + ".obj");
        this.faceQuads = new ArrayList<BakedQuad>();
        this.generalQuads = new ArrayList<BakedQuad>();

        try {
            generalQuads.clear();
            sourceMesh = new MeshLoader().loadFromResource(model);
        } catch (IOException e) {
            throw new ReportedException(new CrashReport("Exception loading custom Model", e));
        }
    }

    @Override
    public void initialize(
            TRSRTransformation thirdPerson,
            TRSRTransformation firstPerson,
            TRSRTransformation head,
            TRSRTransformation gui,
            ResourceLocation icon, ModelManager modelManager) {

        this.thirdPerson = thirdPerson;
        this.firstPerson = firstPerson;
        this.head = head;
        this.gui = gui;

        this.iconSprite = modelManager.getTextureMap().getAtlasSprite(icon.toString());

        generalQuads = sourceMesh.bakeModel(modelManager);
    }

    @Override
    public IFlexibleBakedModel handleItemState(ItemStack stack) {
        return this;
    }

    @Override
    public IFlexibleBakedModel handleBlockState(IBlockState state) {
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
    public VertexFormat getFormat() {
        return Attributes.DEFAULT_BAKED_FORMAT;
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
        return null;
    }

    @Override
    public Pair<IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        switch(cameraTransformType)
        {
            case FIRST_PERSON:
                return Pair.of((IBakedModel)this, firstPerson.getMatrix());
            case THIRD_PERSON:
                return Pair.of((IBakedModel)this, thirdPerson.getMatrix());
            case GUI:
                return Pair.of((IBakedModel)this, gui.getMatrix());
            case HEAD:
                return Pair.of((IBakedModel)this, head.getMatrix());
            case NONE:
                return Pair.of((IBakedModel)this, TRSRTransformation.identity().getMatrix());
        }
        return null;
    }
}
