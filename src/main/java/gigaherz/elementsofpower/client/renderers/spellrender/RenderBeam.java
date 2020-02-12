package gigaherz.elementsofpower.client.renderers.spellrender;

import com.mojang.blaze3d.matrix.MatrixStack;
import gigaherz.elementsofpower.client.renderers.ModelHandle;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class RenderBeam extends RenderSpell
{
    @Override
    public void render(Spellcast cast, PlayerEntity player, EntityRendererManager renderManager, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, Vec3d offset)
    {
        float scale = 0.15f * cast.getScale();


        RayTraceResult mop = cast.getHitPosition(partialTicks);

        Vec3d start = cast.start;
        Vec3d end = cast.end;

        Vec3d beam0 = end.subtract(start);

        start = start.add(offset);

        Vec3d beam = end.subtract(start);
        Vec3d dir = beam.normalize();

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

        matrixStackIn.push();

        float time = (player.ticksExisted + partialTicks);

        int beam_color = getColor(cast);
        RenderType rt = getRenderType(cast);
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

            {
                matrixStackIn.push();
                matrixStackIn.translate( (float) (offset.x), (float) (offset.y), (float) (offset.z));
                matrixStackIn.rotate(Vector3f.YP.rotation((float) (Math.PI*0.5-beamYaw)));
                matrixStackIn.rotate(Vector3f.YP.rotation((float) -beamPitch));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(angle));
                matrixStackIn.scale(scale_start, scale_start, scale_start);

                modelSphere.render(bufferIn, rt, matrixStackIn, 0x00F000F0, color);

                matrixStackIn.pop();
            }

            {
                matrixStackIn.push();
                matrixStackIn.translate( (float) (offset.x), (float) (offset.y), (float) (offset.z));
                matrixStackIn.rotate(Vector3f.YP.rotation((float) (Math.PI*0.5-beamYaw)));
                matrixStackIn.rotate(Vector3f.YP.rotation((float) -beamPitch));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(angle));
                matrixStackIn.scale(scale_beam, scale_beam, (float) distance);

                modelCyl.render(bufferIn, rt, matrixStackIn, 0x00F000F0, color);

                matrixStackIn.pop();
            }

            if (mop != null && mop.getType() != RayTraceResult.Type.MISS)
            {
                matrixStackIn.push();
                matrixStackIn.translate( (float) (offset.x), (float) (offset.y), (float) (offset.z));
                matrixStackIn.rotate(Vector3f.YP.rotation((float) (Math.PI*0.5-beamYaw)));
                matrixStackIn.rotate(Vector3f.YP.rotation((float) -beamPitch));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(angle));
                matrixStackIn.scale(scale_end, scale_end, scale_end);

                modelSphere.render(bufferIn, rt, matrixStackIn, 0x00F000F0, color);

                matrixStackIn.pop();
            }
        }

        matrixStackIn.pop();
    }
}
