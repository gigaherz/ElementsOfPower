package gigaherz.elementsofpower.models;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@SuppressWarnings("deprecation")
public class ObjModelRegistrationHelper
{
    public static final ObjModelRegistrationHelper instance = new ObjModelRegistrationHelper();

    protected final Map<ResourceLocation, ObjModel.Loader> modelsToInject = new Hashtable<ResourceLocation, ObjModel.Loader>();
    protected final Hashtable<ResourceLocation, ModelBlock> modelBlocks = new Hashtable<ResourceLocation, ModelBlock>();

    public ObjModelRegistrationHelper()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void registerCustomModel(ModelResourceLocation modelLocation, ResourceLocation resourceLocation)
    {
        ObjModel.Loader ldr = new ObjModel.Loader(resourceLocation);
        modelsToInject.put(modelLocation, ldr);
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent event)
    {
        if (event.map == Minecraft.getMinecraft().getTextureMapBlocks())
        {
            try
            {
                for (Map.Entry<ResourceLocation, ObjModel.Loader> e : modelsToInject.entrySet())
                {
                    for (String loc : e.getValue().getTextures(this))
                    {
                        // Workaround for a MC/Deobf bug,
                        // where the map lookup uses a ResourceLocation where the map stores Strings
                        if (event.map.getTextureExtry(loc) != null)
                            continue;
                        event.map.registerSprite(new ResourceLocation(loc));
                    }
                }
            }
            catch (IOException e)
            {
                throw new ReportedException(new CrashReport("Exception loading custom Model", e));
            }
        }
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event)
    {
        try
        {
            for (Map.Entry<ResourceLocation, ObjModel.Loader> entry : modelsToInject.entrySet())
            {
                ObjModel.Loader loader = entry.getValue();

                ImmutableMap<TransformType, Matrix4f> transformations = loadModelTransforms(loader.baseLocation);

                TextureAtlasSprite particle = event.modelManager.getTextureMap().getAtlasSprite(loader.textures.get("particle"));

                ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

                ObjModel model = loader.getModel();
                model.bake(event.modelManager, loader.textures, builder);

                BakedModel bakedModel = new BakedModel(builder.build(), transformations, particle, model.getVertexFormat());

                event.modelRegistry.putObject(entry.getKey(), bakedModel);
            }
        }
        catch (IOException e)
        {
            throw new ReportedException(new CrashReport("Exception loading custom Model", e));
        }
    }

    protected ImmutableMap<TransformType, Matrix4f> loadModelTransforms(final ResourceLocation loc)
    {
        ModelBlock modelblock = loadModel(loc);
        if (modelblock == null)
            return ImmutableMap.of();

        Map<TransformType, Matrix4f> map = new HashMap<TransformType, Matrix4f>();
        map.put(TransformType.THIRD_PERSON, getMatrix(modelblock.getThirdPersonTransform()));
        map.put(TransformType.FIRST_PERSON, getMatrix(modelblock.getFirstPersonTransform()));
        map.put(TransformType.HEAD, getMatrix(modelblock.getHeadTransform()));
        map.put(TransformType.GUI, getMatrix(modelblock.getInGuiTransform()));

        return Maps.immutableEnumMap(map);
    }

    public static Matrix4f getMatrix(ItemTransformVec3f transform)
    {
        javax.vecmath.Matrix4f m = new javax.vecmath.Matrix4f(), t = new javax.vecmath.Matrix4f();
        m.setIdentity();
        m.setTranslation(transform.translation);
        t.setIdentity();
        t.rotY((float) Math.toRadians(transform.rotation.y));
        m.mul(t);
        t.setIdentity();
        t.rotX((float) Math.toRadians(transform.rotation.x));
        m.mul(t);
        t.setIdentity();
        t.rotZ((float) Math.toRadians(transform.rotation.z));
        m.mul(t);
        t.setIdentity();
        t.m00 = transform.scale.x;
        t.m11 = transform.scale.y;
        t.m22 = transform.scale.z;
        m.mul(t);
        return m;
    }

    protected ModelBlock loadModel(final ResourceLocation loc)
    {
        ModelBlock modelblock = modelBlocks.get(loc);

        if (modelblock != null)
        {
            return modelblock;
        }

        if (loc.getResourcePath().startsWith("builtin/"))
            return null;

        try
        {
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(getJsonLocation(loc));
            if (iresource != null)
            {
                Reader reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);

                modelblock = ModelBlock.deserialize(reader);
                modelblock.name = loc.toString();
                modelBlocks.put(loc, modelblock);

                ResourceLocation parentLoc = modelblock.getParentLocation();
                if (parentLoc != null)
                {
                    ModelBlock parentModel = loadModel(parentLoc);
                    if (parentModel != null)
                    {
                        modelblock.getParentFromMap(modelBlocks);
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new ReportedException(new CrashReport("Exception loading custom Model", e));
        }

        return modelblock;
    }

    protected static ResourceLocation getObjLocation(ResourceLocation loc)
    {
        return new ResourceLocation(loc.getResourceDomain(), "models/" + loc.getResourcePath() + ".obj");
    }

    protected ResourceLocation getJsonLocation(ResourceLocation loc)
    {
        return new ResourceLocation(loc.getResourceDomain(), "models/" + loc.getResourcePath() + ".json");
    }

    public static class BakedModel
            implements IFlexibleBakedModel, IPerspectiveAwareModel
    {
        final static Matrix4f identity;

        static
        {
            identity = new Matrix4f();
            identity.setIdentity();
        }

        final ImmutableMap<TransformType, Matrix4f> transformations;
        final ImmutableList<BakedQuad> generalQuads;
        final TextureAtlasSprite iconSprite;
        final VertexFormat format;

        public BakedModel(ImmutableList<BakedQuad> generalQuads,
                          ImmutableMap<TransformType, Matrix4f> transformations,
                          TextureAtlasSprite particle,
                          VertexFormat format)
        {
            this.transformations = transformations;
            this.iconSprite = particle;
            this.generalQuads = generalQuads;
            this.format = format;
        }

        public List<BakedQuad> getFaceQuads(EnumFacing face)
        {
            return Collections.emptyList();
        }

        public List<BakedQuad> getGeneralQuads()
        {
            return generalQuads;
        }

        public VertexFormat getFormat()
        {
            return format;
        }

        public boolean isAmbientOcclusion()
        {
            return false;
        }

        public boolean isGui3d()
        {
            return false;
        }

        public boolean isBuiltInRenderer()
        {
            return false;
        }

        public TextureAtlasSprite getTexture()
        {
            return iconSprite;
        }

        public ItemCameraTransforms getItemCameraTransforms()
        {
            return null;
        }

        public Pair<IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
        {
            return Pair.<IBakedModel, Matrix4f>of(this, transformations.getOrDefault(cameraTransformType, identity));
        }
    }
}
