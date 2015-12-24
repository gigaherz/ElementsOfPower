package gigaherz.elementsofpower.renders;

import gigaherz.elementsofpower.entities.EntityBall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public class RenderBall extends Render<EntityBall>
{
    public RenderBall(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityBall entity, double x, double y, double z, float p_76986_8_, float partialTicks)
    {
        IFlexibleBakedModel model = RenderingStuffs.loadModel("elementsofpower:entity/sphere.obj");

        float scale = entity.getScale() * 0.25f;

        GlStateManager.disableLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(scale, scale, scale);

        bindTexture(TextureMap.locationBlocksTexture);

        int ball_color = entity.getSpellcast().getEffect().getColor();
        for(int i=0;i<=4;i++)
        {
            float tt = (i+(entity.ticksExisted % 10 + partialTicks) / 11.0f)/5.0f;
            float subScale = (1 + 0.5f * tt);

            int alpha = 255 - (i==0 ? 0 : (int)(tt*255));
            int color = (alpha << 24) | ball_color;

            GlStateManager.pushMatrix();
            GlStateManager.scale(subScale, subScale, subScale);

            RenderingStuffs.renderModel(model, color);

            GlStateManager.popMatrix();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();

        GlStateManager.enableLighting();

        super.doRender(entity, x, y, z, p_76986_8_, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBall entity)
    {
        return TextureMap.locationBlocksTexture;
    }

}