package gigaherz.elementsofpower.renders.spellrender;

import gigaherz.elementsofpower.renders.RenderingStuffs;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import org.lwjgl.opengl.GL11;

public class RenderSphere extends RenderSpell
{
    @Override
    public void doRender(Spellcast cast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset, String tex)
    {
        int color = cast.getColor();
        float scale = cast.getScale();

        if (tex != null)
        {
            color = 0xFFFFFF;
        }

        IFlexibleBakedModel modelSphere = RenderingStuffs.loadModelRetextured("elementsofpower:entity/sphere.obj",
                "#Default", tex);

        float time = ((cast.totalCastTime - cast.remainingCastTime) + partialTicks);
        float progress = (time / cast.totalCastTime);

        scale = scale * progress;

        if (scale <= 0)
            return;

        int alpha = (int) (255 * (1 - progress));
        color = (alpha << 24) | color;

        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        GlStateManager.pushMatrix();

        GlStateManager.translate(
                (float) (x + offset.xCoord),
                (float) (y + offset.yCoord - player.getEyeHeight() * 0.5f),
                (float) (z + offset.zCoord));
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.cullFace(GL11.GL_FRONT);
        RenderingStuffs.renderModel(modelSphere, color);
        GlStateManager.cullFace(GL11.GL_BACK);
        RenderingStuffs.renderModel(modelSphere, color);

        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();
    }
}
