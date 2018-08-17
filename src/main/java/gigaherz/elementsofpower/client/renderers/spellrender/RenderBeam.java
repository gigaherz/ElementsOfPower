package gigaherz.elementsofpower.client.renderers.spellrender;

import gigaherz.common.client.ModelHandle;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RenderBeam extends RenderSpell
{
    @Override
    public void doRender(Spellcast cast, EntityPlayer player, RenderManager renderManager,
                         double x, double y, double z, float partialTicks, Vec3d offset, String tex, int beam_color)
    {
        float scale = 0.15f * cast.getScale();

        ModelHandle modelSphere = getSphere(tex);
        ModelHandle modelCyl = getCylinder(tex);

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

        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();

        float time = (player.ticksExisted + partialTicks);

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
                GlStateManager.pushMatrix();
                GlStateManager.translate(
                        (float) (x + offset.x),
                        (float) (y + offset.y),
                        (float) (z + offset.z));
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw) + 90, 0, 1, 0);
                GlStateManager.rotate(-(float) Math.toDegrees(beamPitch), 1, 0, 0);
                GlStateManager.rotate(angle, 0, 0, 1);
                GlStateManager.scale(scale_start, scale_start, scale_start);

                modelSphere.render(color);

                GlStateManager.popMatrix();
            }

            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(
                        (float) (x + offset.x),
                        (float) (y + offset.y),
                        (float) (z + offset.z));
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw) + 90, 0, 1, 0);
                GlStateManager.rotate(-(float) Math.toDegrees(beamPitch), 1, 0, 0);
                GlStateManager.rotate(angle, 0, 0, 1);
                GlStateManager.scale(scale_beam, scale_beam, distance);

                modelCyl.render(color);

                GlStateManager.popMatrix();
            }

            if (mop != null && mop.hitVec != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(
                        (float) (x + beam0.x),
                        (float) (y + beam0.y),
                        (float) (z + beam0.z));
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw) + 90, 0, 1, 0);
                GlStateManager.rotate(-(float) Math.toDegrees(beamPitch), 1, 0, 0);
                GlStateManager.rotate(angle, 0, 0, 1);
                GlStateManager.scale(scale_end, scale_end, scale_end);

                modelSphere.render(color);

                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
    }
}
