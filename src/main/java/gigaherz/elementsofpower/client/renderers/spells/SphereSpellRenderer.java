package gigaherz.elementsofpower.client.renderers.spells;

import com.mojang.blaze3d.matrix.MatrixStack;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class SphereSpellRenderer extends SpellRenderer
{
    @Override
    public void render(Spellcast cast, PlayerEntity player, EntityRendererManager renderManager, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, Vec3d offset)
    {
        float scale = cast.getScale();

        float time = ((cast.totalCastTime - cast.remainingCastTime) + partialTicks);
        float progress = (time / cast.totalCastTime);

        scale = scale * progress;

        if (scale <= 0)
            return;

        int alpha = (int) (255 * (1 - progress));
        int color = (alpha << 24) | getColor(cast);

        /*
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
         */

        matrixStackIn.push();

        matrixStackIn.translate( (float) (offset.x), (float) (offset.y - player.getEyeHeight() * 0.5f), (float) (offset.z));
        matrixStackIn.scale(scale, scale, scale);

        modelSphere.get().render(bufferIn, getRenderType(cast, true), matrixStackIn, 0x00F000F0, color);
        modelSphere.get().render(bufferIn, getRenderType(cast, false), matrixStackIn, 0x00F000F0, color);

        matrixStackIn.pop();
    }

    private RenderType getRenderType(Spellcast cast, boolean cullFront)
    {
        return null;
    }
}
