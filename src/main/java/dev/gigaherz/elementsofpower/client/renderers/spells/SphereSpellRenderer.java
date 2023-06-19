package dev.gigaherz.elementsofpower.client.renderers.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SphereSpellRenderer extends SpellRenderer
{
    @Override
    public void render(InitializedSpellcast cast, Player player, EntityRenderDispatcher renderManager,
                       float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Vec3 offset)
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

        matrixStackIn.pushPose();

        matrixStackIn.translate((float) (offset.x), (float) (offset.y), (float) (offset.z));
        matrixStackIn.scale(scale, scale, scale);

        modelSphereInside.get().render(matrixStackIn, bufferIn, getSphereRenderType(cast), 0x00F000F0, color);
        modelSphere.get().render(matrixStackIn, bufferIn, getSphereRenderType(cast), 0x00F000F0, color);

        matrixStackIn.popPose();
    }

    private static RenderType getSphereRenderType(InitializedSpellcast cast)
    {
        return RenderType.entityTranslucentCull(getTexture(cast));
    }
}
