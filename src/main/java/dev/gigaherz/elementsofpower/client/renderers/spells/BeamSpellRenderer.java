package dev.gigaherz.elementsofpower.client.renderers.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class BeamSpellRenderer extends SpellRenderer
{
    @Override
    public void render(SpellcastState state, Player player, EntityRenderDispatcher renderManager, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Vec3 offset)
    {
        float scale = 0.15f * state.scale();

        HitResult mop = state.getHitPosition(partialTicks);

        Vec3 start = state.getStart();
        Vec3 end = state.getEnd();

        Vec3 beam0 = end.subtract(start);

        start = start.add(offset);

        Vec3 beam = end.subtract(start);
        Vec3 dir = beam.normalize();

        double distance = beam.length();

        double beamPlane = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        double beamYaw = Math.atan2(dir.z, dir.x);
        double beamPitch = Math.atan2(dir.y, beamPlane);

        /*
        RenderSystem.disableLighting();
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
         */

        matrixStackIn.pushPose();

        float time = (player.tickCount + partialTicks);

        int beam_color = getColor(state);
        RenderType rt = getRenderType(state, state.spellcast());
        for (int i = 0; i <= 4; i++)
        {
            //float tt = (i + (player.ticksExisted % 10 + partialTicks) / 11.0f) / 5.0f;
            float scale_base = scale * (1 + 0.05f * i);
            float scale_start = scale_base * 0.3f;
            float scale_beam = scale_base * 0.3f;
            float scale_end = scale_base * 1.2f;

            int alpha = 255 - i * 32;
            int color = (alpha << 24) | beam_color;

            float angle = time * (6 + 3 * (4 + i)) * ((i & 1) == 0 ? 1 : -1) * 0.1f;

            Quaternionf rot = Axis.YP.rotation((float) (Math.PI * 0.5 - beamYaw));
            rot.mul(Axis.XP.rotation((float) -beamPitch));
            rot.mul(Axis.ZP.rotationDegrees(angle));

            {
                matrixStackIn.pushPose();
                matrixStackIn.translate((float) (offset.x), (float) (offset.y), (float) (offset.z));
                matrixStackIn.mulPose(rot);
                matrixStackIn.scale(scale_start, scale_start, scale_start);

                modelSphere.get().render(matrixStackIn, bufferIn, rt, 0x00F000F0, color);

                matrixStackIn.popPose();
            }

            {
                matrixStackIn.pushPose();
                matrixStackIn.translate((float) (offset.x), (float) (offset.y), (float) (offset.z));
                matrixStackIn.mulPose(rot);
                matrixStackIn.scale(scale_beam, scale_beam, (float) distance);

                modelCyl.get().render(matrixStackIn, bufferIn, rt, 0x00F000F0, color);

                matrixStackIn.popPose();
            }

            if (mop != null && mop.getType() != HitResult.Type.MISS)
            {
                matrixStackIn.pushPose();
                matrixStackIn.translate((float) (beam0.x), (float) (beam0.y), (float) (beam0.z));
                matrixStackIn.mulPose(rot);
                matrixStackIn.scale(scale_end, scale_end, scale_end);

                modelSphere.get().render(matrixStackIn, bufferIn, rt, 0x00F000F0, color);

                matrixStackIn.popPose();
            }
        }

        matrixStackIn.popPose();
    }
}
