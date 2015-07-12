package gigaherz.elementsofpower.models;

import gigaherz.elementsofpower.models.obj.MeshLoader;
import gigaherz.elementsofpower.models.obj.MeshModel;
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

public class ObjBakedModel
        implements IFlexibleBakedModel, ISmartItemModel, ISmartBlockModel, IPerspectiveAwareModel
{
    Matrix4f thirdPerson;
    Matrix4f firstPerson;
    Matrix4f head;
    Matrix4f gui;

    List<BakedQuad> faceQuads;
    List<BakedQuad> generalQuads;
    MeshModel sourceMesh;

    TextureAtlasSprite iconSprite;

    public ObjBakedModel(ResourceLocation modelLocation,
                         Matrix4f[] transformations,
                         TextureAtlasSprite sprite, ModelManager modelManager)
    {
        this.faceQuads = new ArrayList<BakedQuad>();
        this.generalQuads = new ArrayList<BakedQuad>();

        this.thirdPerson = (transformations[0]);
        this.firstPerson = (transformations[1]);
        this.head = (transformations[2]);
        this.gui = (transformations[3]);

        this.iconSprite = sprite;

        try
        {
            generalQuads.clear();
            sourceMesh = new MeshLoader().loadFromResource(modelLocation);
        } catch (IOException e)
        {
            throw new ReportedException(new CrashReport("Exception loading custom Model", e));
        }

        generalQuads = sourceMesh.bakeModel(modelManager);
    }

    @Override
    public IFlexibleBakedModel handleItemState(ItemStack stack)
    {
        return this;
    }

    @Override
    public IFlexibleBakedModel handleBlockState(IBlockState state)
    {
        return this;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing face)
    {
        return faceQuads;
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        return generalQuads;
    }

    @Override
    public VertexFormat getFormat()
    {
        return Attributes.DEFAULT_BAKED_FORMAT;
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return false;
    }

    @Override
    public boolean isGui3d()
    {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getTexture()
    {
        return iconSprite;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return null;
    }

    @Override
    public Pair<IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType)
    {
        switch (cameraTransformType)
        {
            case FIRST_PERSON:
                return Pair.of((IBakedModel) this, firstPerson);
            case THIRD_PERSON:
                return Pair.of((IBakedModel) this, thirdPerson);
            case GUI:
                return Pair.of((IBakedModel) this, gui);
            case HEAD:
                return Pair.of((IBakedModel) this, head);
            case NONE:
                return Pair.of((IBakedModel) this, TRSRTransformation.identity().getMatrix());
        }
        return null;
    }
}
