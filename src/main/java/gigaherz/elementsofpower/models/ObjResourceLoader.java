package gigaherz.elementsofpower.models;

import com.google.common.base.Charsets;
import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.vecmath.Matrix4f;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class ObjResourceLoader
{
    public static final ObjResourceLoader instance = new ObjResourceLoader();

    protected final List<ResourceLocation> itemTextures = new ArrayList<ResourceLocation>();
    protected final List<ResourceLocation> blockTextures = new ArrayList<ResourceLocation>();

    protected final Map<ResourceLocation, ResourceLocation> itemsToInject = new Hashtable<ResourceLocation, ResourceLocation>();
    protected final Map<ResourceLocation, ResourceLocation> blocksToInject = new Hashtable<ResourceLocation, ResourceLocation>();

    public ObjResourceLoader()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void registerCustomItemModel(ResourceLocation resourceLocation, final String itemName)
    {
        ResourceLocation modelLocation = new ResourceLocation(ElementsOfPower.MODID, "models/obj/" + itemName + ".obj");
        itemsToInject.put(resourceLocation, modelLocation);
    }

    public void registerCustomBlockModel(ResourceLocation resourceLocation, final String blockName)
    {
        ResourceLocation modelLocation = new ResourceLocation(ElementsOfPower.MODID, "models/obj/" + blockName + ".obj");
        itemsToInject.put(resourceLocation, modelLocation);
    }

    public void registerItemSprite(ResourceLocation resourceLocation)
    {
        if (!itemTextures.contains(resourceLocation))
            itemTextures.add(resourceLocation);
    }

    public void registerBlockSprite(ResourceLocation resourceLocation)
    {
        if (!blockTextures.contains(resourceLocation))
            blockTextures.add(resourceLocation);
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent event)
    {
        if (event.map == Minecraft.getMinecraft().getTextureMapBlocks())
        {
            for (ResourceLocation loc : blockTextures)
            {
                event.map.registerSprite(loc);
            }
            for (ResourceLocation loc : itemTextures)
            {
                event.map.registerSprite(loc);
            }
        }
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event)
    {
        processModelSet(event.modelManager, event.modelRegistry, "item/", itemsToInject);
        processModelSet(event.modelManager, event.modelRegistry, "block/", blocksToInject);
    }

    private void processModelSet(ModelManager modelManager, IRegistry modelRegistry, String prefix, Map<ResourceLocation, ResourceLocation> map)
    {
        for (Map.Entry<ResourceLocation, ResourceLocation> entry : map.entrySet())
        {
            ResourceLocation loc = entry.getKey();
            ResourceLocation modelLocation = entry.getValue();

            ResourceLocation icon = new ResourceLocation(loc.getResourceDomain(), prefix + loc.getResourcePath());

            Matrix4f[] transformations = loadModelTransforms(icon);

            TextureAtlasSprite sprite = modelManager.getTextureMap().getAtlasSprite(icon.toString());

            ObjBakedModel model = new ObjBakedModel(modelLocation, transformations, sprite, modelManager);

            modelRegistry.putObject(loc, model);
        }
    }

    protected Matrix4f[] loadModelTransforms(final ResourceLocation loc)
    {
        ModelBlock modelblock = loadModelTransforms(new Hashtable<ResourceLocation, ModelBlock>(), loc);
        if(modelblock == null)
        {
            Matrix4f id = new Matrix4f();
            id.setIdentity();

            return new Matrix4f[]{id,id,id,id};
        }
        else
        {
            return new Matrix4f[]{
                    getMatrix(modelblock.getThirdPersonTransform()),
                    getMatrix(modelblock.getFirstPersonTransform()),
                    getMatrix(modelblock.getHeadTransform()),
                    getMatrix(modelblock.getInGuiTransform())
            };
        }
    }

    public static Matrix4f getMatrix(ItemTransformVec3f transform)
    {
        javax.vecmath.Matrix4f m = new javax.vecmath.Matrix4f(), t = new javax.vecmath.Matrix4f();
        m.setIdentity();
        m.setTranslation(transform.translation);
        t.setIdentity();
        t.rotY((float)Math.toRadians(transform.rotation.y));
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

    protected ModelBlock loadModelTransforms(Map<ResourceLocation, ModelBlock> map, final ResourceLocation loc)
    {
        Reader reader = null;
        ModelBlock modelblock = map.get(loc);

        if (modelblock != null)
        {
            return modelblock;
        }

        if (loc.getResourcePath().startsWith("builtin/"))
            return null;

        try
        {
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(getModelLocation(loc));
            if (iresource != null)
            {
                reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);

                modelblock = ModelBlock.deserialize(reader);
                modelblock.name = loc.toString();
                map.put(loc, modelblock);

                ResourceLocation parentLoc = modelblock.getParentLocation();
                if (parentLoc != null)
                {
                    ModelBlock parentModel = loadModelTransforms(map, parentLoc);
                    if (parentModel != null)
                    {
                        modelblock.getParentFromMap(map);
                    }
                }
            }
        } catch (IOException e)
        {
            throw new ReportedException(new CrashReport("Exception loading custom Model", e));
        }

        return modelblock;
    }

    protected ResourceLocation getModelLocation(ResourceLocation loc)
    {
        return new ResourceLocation(loc.getResourceDomain(), "models/" + loc.getResourcePath() + ".json");
    }
}
