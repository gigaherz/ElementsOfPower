package gigaherz.elementsofpower.client.renderers.spellrender;

import gigaherz.elementsofpower.client.renderers.ModelHandle;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RenderCone extends RenderSpell
{
    @Override
    public void doRender(Spellcast cast, EntityPlayer player, RenderManager renderManager,
                         double x, double y, double z, float partialTicks, Vec3d offset, String tex, int color)
    {
        float scale = 2 * cast.getScale();

        IBakedModel modelCone = getCone(tex);

        cast.getHitPosition(partialTicks);

        Vec3d start = cast.start;
        Vec3d end = cast.end;

        start = start.add(offset);

        Vec3d beam = end.subtract(start);
        Vec3d dir = beam.normalize();

        double beamPlane = Math.sqrt(dir.xCoord * dir.xCoord + dir.zCoord * dir.zCoord);
        double beamYaw = Math.atan2(dir.zCoord, dir.xCoord);
        double beamPitch = Math.atan2(dir.yCoord, beamPlane);

        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

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
                    (float) (x + offset.xCoord),
                    (float) (y + offset.yCoord),
                    (float) (z + offset.zCoord));
            GlStateManager.rotate(-(float) Math.toDegrees(beamYaw) + 90, 0, 1, 0);
            GlStateManager.rotate(-(float) Math.toDegrees(beamPitch), 1, 0, 0);
            GlStateManager.translate(0, -0.15f, offset_z);
            GlStateManager.rotate(angle, 0, 0, 1);
            GlStateManager.scale(scale_xy, scale_xy, scale_z);

            ModelHandle.renderModel(modelCone, color);

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
