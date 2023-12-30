package dev.gigaherz.elementsofpower.client.renderers.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SphereSpellRenderer extends SpellRenderer
{
    @Override
    public void render(SpellcastState state, Player player, EntityRenderDispatcher renderManager,
                       float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Vec3 offset)
    {
        float scale = state.scale();

        float time = ((state.totalCastTime - state.remainingCastTime) + partialTicks);
        float progress = (time / state.totalCastTime);

        scale = scale * progress;

        if (scale <= 0)
            return;

        int alpha = (int) (255 * (1 - progress));
        int color = (alpha << 24) | getColor(state);

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

        modelSphereInside.get().render(matrixStackIn, bufferIn, getSphereRenderType(state, state.spellcast()), 0x00F000F0, color);
        modelSphere.get().render(matrixStackIn, bufferIn, getSphereRenderType(state, state.spellcast()), 0x00F000F0, color);

        matrixStackIn.popPose();
    }

    private static RenderType getSphereRenderType(@Nullable SpellcastState state, @Nullable Spellcast spellcast)
    {
        return RenderType.entityTranslucentCull(getTexture(state, spellcast));
    }
}
