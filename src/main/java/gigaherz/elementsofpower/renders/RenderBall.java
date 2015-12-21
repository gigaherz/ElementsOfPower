package gigaherz.elementsofpower.renders;

import gigaherz.elementsofpower.entities.EntityBallBase;
import gigaherz.elementsofpower.entities.IRenderStackProvider;
import gigaherz.elementsofpower.entities.IVariableSize;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
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

import static net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class RenderBall extends Render<EntityBallBase>
{
    IFlexibleBakedModel model;

    public RenderBall(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityBallBase entity, double x, double y, double z, float p_76986_8_, float partialTicks)
    {
        if(model == null)
        {
            try
            {
                IModel mod = ModelLoaderRegistry.getModel(new ResourceLocation("elementsofpower:entity/sphere.obj"));
                model = mod.bake(mod.getDefaultState(), Attributes.DEFAULT_BAKED_FORMAT,
                        (location) -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
            }
            catch(IOException e)
            {
                throw new ReportedException(new CrashReport("Error loading model for entity", e));
            }
        }

        float scale = entity.getScale();

        GlStateManager.disableLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(scale, scale, scale);

        bindTexture(TextureMap.locationBlocksTexture);

        renderModel(model, entity.getBallColor() | 0xFF000000);

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();

        GlStateManager.enableLighting();

        super.doRender(entity, x, y, z, p_76986_8_, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBallBase entity)
    {
        return TextureMap.locationBlocksTexture;
    }

    private void renderModel(IFlexibleBakedModel model)
    {
        renderModel(model, -1);
    }

    private void renderModel(IFlexibleBakedModel model, int color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, model.getFormat());

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            this.renderQuads(worldrenderer, model.getFaceQuads(enumfacing), color);
        }

        this.renderQuads(worldrenderer, model.getGeneralQuads(), color);
        tessellator.draw();
    }

    private void renderQuads(WorldRenderer renderer, List<BakedQuad> quads, int color)
    {
        for (BakedQuad bakedquad : quads)
            LightUtil.renderQuadColor(renderer, bakedquad, color);
    }
}