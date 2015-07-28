package gigaherz.elementsofpower.models;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
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
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@SuppressWarnings("deprecation")
public class ObjModelRegistrationHelper implements ICustomModelLoader
{
    public static final ObjModelRegistrationHelper instance = new ObjModelRegistrationHelper();

    private final Set<String> enabledDomains = new HashSet<String>();

    final Map<ResourceLocation, ObjModel.Loader> modelsToInject = new Hashtable<>();

    public ObjModelRegistrationHelper()
    {
        MinecraftForge.EVENT_BUS.register(this);
        ModelLoaderRegistry.registerLoader(this);
    }

    public void enableDomain(String domain)
    {
        enabledDomains.add(domain.toLowerCase());
    }

    public void registerCustomModel(ModelResourceLocation modelLocation, ResourceLocation resourceLocation)
    {
        ResourceLocation json = resourceLocation;
        ResourceLocation obj = ObjModelRegistrationHelper.ModelUtilities.getObjLocation(resourceLocation);

        ObjModel.Loader ldr = new ObjModel.Loader(json, obj);
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
                    for (ResourceLocation loc : e.getValue().getTextures())
                    {
                        // Workaround for a MC/Deobf bug,
                        // where the map lookup uses a ResourceLocation while the map stores Strings
                        if (event.map.getTextureExtry(loc.toString()) != null)
                            continue;
                        event.map.registerSprite(loc);
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

                ImmutableMap<TransformType, Matrix4f> transformations = ModelUtilities.loadModelTransforms(loader.jsonLocation);

                TextureAtlasSprite particle = event.modelManager.getTextureMap().getAtlasSprite(loader.textures.get("particle").toString());

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

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        if (!enabledDomains.contains(modelLocation.getResourceDomain()))
            return false;

        // This requires the model reference to include the extension,
        // which doesn't seem to be possible for items
        return modelLocation.getResourcePath().endsWith(".obj");

        // The code below accepts ANY resource location that can be mapped to a .obj filename,
        // but using exceptions as the "default" case with non-exception being the exception,
        // hurts my soul deeply, so it shall remain disabled.

        /*
        ResourceLocation obj = new ResourceLocation(modelLocation.toString() + ".obj");

        try {
            IResource dummy = Minecraft.getMinecraft().getResourceManager().getResource(obj);
            return dummy != null;
        }
        catch(FileNotFoundException e)
        {
            return false;
        }
        catch(IOException e)
        {
            throw new ReportedException(new CrashReport("Exception loading custom Model", e));
        }
        */
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws IOException
    {
        String path = modelLocation.getResourcePath();
        int start = "models/".length();
        int length = path.length() - "models/.obj".length();

        ResourceLocation obj = modelLocation;
        ResourceLocation json = new ResourceLocation(
                modelLocation.getResourceDomain(),
                path.substring(start, length));

        // Use the code below if also using the exception-driven accepts() above.
        /*
        ResourceLocation json = new ResourceLocation(modelLocation.getResourceDomain(), modelLocation.getResourcePath().substring("models/".length()));
        ResourceLocation obj = new ResourceLocation(modelLocation.toString() + ".obj");
        */

        ObjModel.Loader ldr = new ObjModel.Loader(json, obj);
        return new ModelInfo(ldr);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        ModelUtilities.clearCache();
    }

    public static class ModelInfo implements IModel
    {
        ObjModel.Loader loader;

        ModelInfo(ObjModel.Loader loader)
        {
            this.loader = loader;
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            return Collections.emptyList();
        }

        @Override
        public Collection<ResourceLocation> getTextures()
        {
            try
            {
                return loader.getTextures();
            }
            catch (IOException e)
            {
                return Collections.emptyList();
            }
        }

        @Override
        public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            try
            {
                ImmutableMap<TransformType, Matrix4f> transformations = ModelUtilities.loadModelTransforms(loader.jsonLocation);

                TextureAtlasSprite particle = bakedTextureGetter.apply(loader.textures.get("particle"));

                ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

                ObjModel model = loader.getModel();
                model.bake(bakedTextureGetter, loader.textures, builder);

                return new BakedModel(builder.build(), transformations, particle, model.getVertexFormat());
            }
            catch (IOException e)
            {
                throw new ReportedException(new CrashReport("Exception loading custom Model", e));
            }
        }

        @Override
        public IModelState getDefaultState()
        {
            return null;
        }
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

    static class ModelUtilities
    {
        static final Hashtable<ResourceLocation, ModelBlock> modelBlocks = new Hashtable<>();

        static void clearCache()
        {
            modelBlocks.clear();
        }

        static ImmutableMap<TransformType, Matrix4f> loadModelTransforms(final ResourceLocation loc)
        {
            ModelBlock modelblock = loadJsonModel(loc);
            if (modelblock == null)
                return ImmutableMap.of();

            Map<TransformType, Matrix4f> map = new HashMap<>();
            map.put(TransformType.THIRD_PERSON, getMatrix(modelblock.getThirdPersonTransform()));
            map.put(TransformType.FIRST_PERSON, getMatrix(modelblock.getFirstPersonTransform()));
            map.put(TransformType.HEAD, getMatrix(modelblock.getHeadTransform()));
            map.put(TransformType.GUI, getMatrix(modelblock.getInGuiTransform()));

            return Maps.immutableEnumMap(map);
        }

        static Matrix4f getMatrix(ItemTransformVec3f transform)
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

        static ModelBlock loadJsonModel(final ResourceLocation loc)
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
                    if (parentLoc != null && !parentLoc.getResourcePath().startsWith("builtin/"))
                    {
                        ModelBlock parentModel = loadJsonModel(parentLoc);
                        if (parentModel != null)
                        {
                            modelblock.getParentFromMap(modelBlocks);
                        }
                    }
                }
            }
            catch (IOException e)
            {
                throw new ReportedException(new CrashReport("Exception loading JSON Model: " + loc.toString(), e));
            }

            return modelblock;
        }

        static ResourceLocation getObjLocation(ResourceLocation loc)
        {
            return new ResourceLocation(loc.getResourceDomain(), "models/" + loc.getResourcePath() + ".obj");
        }

        static ResourceLocation getJsonLocation(ResourceLocation loc)
        {
            return new ResourceLocation(loc.getResourceDomain(), "models/" + loc.getResourcePath() + ".json");
        }
    }
}
