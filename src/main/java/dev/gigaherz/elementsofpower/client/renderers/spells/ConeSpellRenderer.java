package dev.gigaherz.elementsofpower.client.renderers.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class ConeSpellRenderer extends SpellRenderer
{
    @Override
    public void render(SpellcastState state, Player player, EntityRenderDispatcher renderManager, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Vec3 offset)
    {
        float scale = 2 * state.scale();

        var hit = state.getHitPosition(partialTicks);

        Vec3 start = state.getStart();
        Vec3 end = state.getEnd();

        start = start.add(offset);

        Vec3 beam = end.subtract(start);
        Vec3 dir = beam.normalize();

        double beamPlane = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        double beamYaw = Math.atan2(dir.z, dir.x);
        double beamPitch = Math.atan2(dir.y, beamPlane);

        /*
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
         */

        matrixStackIn.pushPose();

        int alpha = 80; // 64;
        int color = (alpha << 24) | getColor(state);

        float time = (player.tickCount + partialTicks);

        RenderType rt = getRenderType(state, state.spellcast());
        for (int i = 0; i <= 4; i++)
        {
            float scale_xy = scale * (float) Math.pow(0.8, i);
            float scale_z = scale * (float) Math.pow(1.05, i);
            float offset_z = 0.5f + 0.005f * i;

            float angle = time * (6 + 3 * (4 - i)) * ((i & 1) == 0 ? 1 : -1);

            Quaternionf rot = Axis.YP.rotation((float) (Math.PI * 0.5 - beamYaw));
            rot.mul(Axis.XP.rotation((float) -beamPitch));
            rot.mul(Axis.ZP.rotationDegrees(angle));

            matrixStackIn.pushPose();
            matrixStackIn.translate((float) (offset.x), (float) (offset.y), (float) (offset.z));
            matrixStackIn.mulPose(rot);
            matrixStackIn.translate(0, 0, offset_z);
            matrixStackIn.scale(scale_xy, scale_xy, scale_z);

            modelCone.get().render(matrixStackIn, bufferIn, rt, 0x00F000F0, color);

            matrixStackIn.popPose();
        }

        matrixStackIn.popPose();
    }
}
