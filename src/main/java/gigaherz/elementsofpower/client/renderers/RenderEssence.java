package gigaherz.elementsofpower.client.renderers;

import gigaherz.elementsofpower.entities.EntityEssence;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderEssence extends Render<EntityEssence>
{
    public RenderEssence(RenderManager renderManager)
    {
        super(renderManager);
    }

    ModelHandle handle = ModelHandle.of("elementsofpower:entity/sphere.obj");

    @Override
    public void doRender(EntityEssence entity, double x, double y, double z, float p_76986_8_, float partialTicks)
    {
        IBakedModel model = handle.get();

        float scale = entity.getScale();

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.scale(scale, scale, scale);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        float cycle = (entity.ticksExisted % 10 + partialTicks) / 11.0f;

        int ball_color = entity.getColor(entity.ticksExisted + partialTicks);
        for (int i = 0; i <= 8; i++)
        {
            float tt = (i + cycle) / 9.0f;
            float subScale = EntityEssence.lerp(0.01f, 1.0f, tt);

            float rtt = (1 - tt);
            int alpha = (i == 0 ? 255 : (int) (rtt * 255));
            if (alpha >= 128) alpha = 255 - alpha;
            alpha /= 2;
            int color = (alpha << 24) | ball_color;

            GlStateManager.pushMatrix();
            GlStateManager.scale(subScale, subScale, subScale);

            ModelHandle.renderModel(model, color);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();

        super.doRender(entity, x, y, z, p_76986_8_, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEssence entity)
    {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}