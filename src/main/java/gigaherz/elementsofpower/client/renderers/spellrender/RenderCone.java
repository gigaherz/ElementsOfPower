package gigaherz.elementsofpower.client.renderers.spellrender;

import com.mojang.blaze3d.matrix.MatrixStack;
import gigaherz.elementsofpower.client.renderers.ModelHandle;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class RenderCone extends RenderSpell
{
    @Override
    public void render(Spellcast cast, PlayerEntity player, EntityRendererManager renderManager, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, Vec3d offset)
    {
        float scale = 2 * cast.getScale();


        cast.getHitPosition(partialTicks);

        Vec3d start = cast.start;
        Vec3d end = cast.end;

        start = start.add(offset);

        Vec3d beam = end.subtract(start);
        Vec3d dir = beam.normalize();

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

        matrixStackIn.push();

        int alpha = 80; // 64;
        int color = (alpha << 24) | getColor(cast);

        float time = (player.ticksExisted + partialTicks);

        RenderType rt = getRenderType(cast);
        for (int i = 0; i <= 4; i++)
        {
            float scale_xy = scale * (float) Math.pow(0.8, i);
            float scale_z = scale * (float) Math.pow(1.05, i);
            float offset_z = 0.5f + 0.005f * i;

            float angle = time * (6 + 3 * (4 - i)) * ((i & 1) == 0 ? 1 : -1);

            matrixStackIn.push();
            matrixStackIn.translate( (float) (offset.x), (float) (offset.y), (float) (offset.z));
            matrixStackIn.rotate(Vector3f.YP.rotation((float) (Math.PI*0.5-beamYaw)));
            matrixStackIn.rotate(Vector3f.YP.rotation((float) -beamPitch));
            matrixStackIn.translate(0, -0.15f, offset_z);
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(angle));
            matrixStackIn.scale(scale_xy, scale_xy, scale_z);

            modelCone.get().render(bufferIn, rt, matrixStackIn, 0x00F000F0, color);

            matrixStackIn.pop();
        }

        matrixStackIn.pop();
    }
}
