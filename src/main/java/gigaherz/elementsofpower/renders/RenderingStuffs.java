package gigaherz.elementsofpower.renders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RenderingStuffs
{
    static Map<String, IFlexibleBakedModel> loadedModels = new HashMap<>();

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

    public static void renderModel(IFlexibleBakedModel model)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, model.getFormat());
        for (BakedQuad bakedquad : model.getGeneralQuads())
        {
            worldrenderer.addVertexData(bakedquad.getVertexData());
        }
        tessellator.draw();
    }

    public static void renderModel(IFlexibleBakedModel model, int color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, model.getFormat());
        for (BakedQuad bakedquad : model.getGeneralQuads())
        {
            LightUtil.renderQuadColor(worldrenderer, bakedquad, color);
        }
        tessellator.draw();
    }

    public static IFlexibleBakedModel loadModel(String resourceName)
    {
        return loadModel(resourceName, Attributes.DEFAULT_BAKED_FORMAT);
    }

    public static IFlexibleBakedModel loadModel(String resourceName, VertexFormat fmt)
    {
        IFlexibleBakedModel model = loadedModels.get(resourceName);
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
        catch (IOException e)
        {
            throw new ReportedException(new CrashReport("Error loading custom model " + resourceName, e));
        }
    }

    public static IFlexibleBakedModel loadModelRetextured(String resourceName, String... textureSwaps)
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

        IFlexibleBakedModel model = loadedModels.get(key);
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
        catch (IOException e)
        {
            throw new ReportedException(new CrashReport("Error loading custom model " + resourceName, e));
        }
    }
}
