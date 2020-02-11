package gigaherz.elementsofpower.client.renderers.spellrender;

import gigaherz.common.client.ModelHandle;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RenderCone extends RenderSpell
{
    @Override
    public void doRender(Spellcast cast, PlayerEntity player, EntityRendererManager renderManager,
                         double x, double y, double z, float partialTicks, Vec3d offset, String tex, int color)
    {
        float scale = 2 * cast.getScale();

        ModelHandle modelCone = getCone(tex);

        cast.getHitPosition(partialTicks);

        Vec3d start = cast.start;
        Vec3d end = cast.end;

        start = start.add(offset);

        Vec3d beam = end.subtract(start);
        Vec3d dir = beam.normalize();

        double beamPlane = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        double beamYaw = Math.atan2(dir.z, dir.x);
        double beamPitch = Math.atan2(dir.y, beamPlane);

        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        renderManager.renderEngine.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();

        int alpha = 80; // 64;
        color = (alpha << 24) | color;

        float time = (player.ticksExisted + partialTicks);

        for (int i = 0; i <= 4; i++)
        {
            float scale_xy = scale * (float) Math.pow(0.8, i);
            float scale_z = scale * (float) Math.pow(1.05, i);
            float offset_z = 0.5f + 0.005f * i;

            float angle = time * (6 + 3 * (4 - i)) * ((i & 1) == 0 ? 1 : -1);

            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    (float) (x + offset.x),
                    (float) (y + offset.y),
                    (float) (z + offset.z));
            GlStateManager.rotate(-(float) Math.toDegrees(beamYaw) + 90, 0, 1, 0);
            GlStateManager.rotate(-(float) Math.toDegrees(beamPitch), 1, 0, 0);
            GlStateManager.translate(0, -0.15f, offset_z);
            GlStateManager.rotate(angle, 0, 0, 1);
            GlStateManager.scale(scale_xy, scale_xy, scale_z);

            modelCone.render(color);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();
    }
}
