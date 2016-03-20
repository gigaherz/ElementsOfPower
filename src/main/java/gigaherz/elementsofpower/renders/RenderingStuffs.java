package gigaherz.elementsofpower.renders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class RenderingStuffs
{
    static Map<String, IBakedModel> loadedModels = new HashMap<>();

    public static void init()
    {
        IResourceManager rm = Minecraft.getMinecraft().getResourceManager();
        if (rm instanceof IReloadableResourceManager)
        {
            ((IReloadableResourceManager) rm).registerReloadListener(new IResourceManagerReloadListener()
            {
                @Override
                public void onResourceManagerReload(IResourceManager __)
                {
                    loadedModels.clear();
                }
            });
        }
    }

    public static void renderModel(IBakedModel model)
    {
        renderModel(model, DefaultVertexFormats.ITEM);
    }

    public static void renderModel(IBakedModel model, VertexFormat fmt)
    {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(GL11.GL_QUADS, fmt);
        for (BakedQuad bakedquad : model.getQuads(null, null, 0))
        {
            worldrenderer.addVertexData(bakedquad.getVertexData());
        }
        tessellator.draw();
    }

    public static void renderModel(IBakedModel model, int color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        for (BakedQuad bakedquad : model.getQuads(null, null, 0))
        {
            LightUtil.renderQuadColor(worldrenderer, bakedquad, color);
        }
        tessellator.draw();
    }

    public static IBakedModel loadModel(String resourceName)
    {
        return loadModel(resourceName, DefaultVertexFormats.ITEM);
    }

    public static IBakedModel loadModel(String resourceName, VertexFormat fmt)
    {
        IBakedModel model = loadedModels.get(resourceName);
        if (model != null)
            return model;

        try
        {
            TextureMap textures = Minecraft.getMinecraft().getTextureMapBlocks();
            IModel mod = ModelLoaderRegistry.getModel(new ResourceLocation(resourceName));
            model = mod.bake(mod.getDefaultState(), fmt,
                    (location) -> textures.getAtlasSprite(location.toString()));
            loadedModels.put(resourceName, model);
            return model;
        }
        catch (Exception e)
        {
            throw new ReportedException(new CrashReport("Error loading custom model " + resourceName, e));
        }
    }

    public static IBakedModel loadModelRetextured(String resourceName, String... textureSwaps)
    {
        if (textureSwaps.length % 2 != 0)
        {
            throw new ReportedException(new CrashReport("Retexturing model", new IllegalArgumentException("textureSwaps must have and even number of elements")));
        }

        String key = resourceName;
        for (int i = 0; i < textureSwaps.length; i += 2)
        {
            key += "//" + textureSwaps[i] + "/" + textureSwaps[i + 1];
        }

        IBakedModel model = loadedModels.get(key);
        if (model != null)
            return model;

        try
        {
            TextureMap textures = Minecraft.getMinecraft().getTextureMapBlocks();
            IModel mod = ModelLoaderRegistry.getModel(new ResourceLocation(resourceName));
            if (mod instanceof IRetexturableModel)
            {
                IRetexturableModel rtm = (IRetexturableModel) mod;
                Map<String, String> s = Maps.newHashMap();
                for (int i = 0; i < textureSwaps.length; i += 2)
                {
                    s.put(textureSwaps[i], textureSwaps[i + 1]);
                }
                mod = rtm.retexture(ImmutableMap.copyOf(s));
            }
            model = mod.bake(mod.getDefaultState(), Attributes.DEFAULT_BAKED_FORMAT,
                    (location) -> textures.getAtlasSprite(location.toString()));
            loadedModels.put(key, model);
            return model;
        }
        catch (Exception e)
        {
            throw new ReportedException(new CrashReport("Error loading custom model " + resourceName, e));
        }
    }
}
