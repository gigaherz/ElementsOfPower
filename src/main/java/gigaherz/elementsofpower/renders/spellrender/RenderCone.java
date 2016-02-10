package gigaherz.elementsofpower.renders.spellrender;

import gigaherz.elementsofpower.renders.RenderingStuffs;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import org.lwjgl.opengl.GL11;

public class RenderCone extends RenderSpell
{
    @Override
    public void doRender(Spellcast cast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset, String tex)
    {
        int color = cast.getColor();
        float scale = 2 * cast.getScale();

        if (tex != null)
        {
            color = 0xFFFFFF;
        }

        IFlexibleBakedModel modelCone = RenderingStuffs.loadModelRetextured("elementsofpower:entity/cone.obj",
                "#Default", tex);

        cast.getHitPosition();

        Vec3 start = cast.start;
        Vec3 end = cast.end;

        start = start.add(offset);

        Vec3 beam = end.subtract(start);
        Vec3 dir = beam.normalize();

        double beamPlane = Math.sqrt(dir.xCoord * dir.xCoord + dir.zCoord * dir.zCoord);
        double beamYaw = Math.atan2(dir.zCoord, dir.xCoord);
        double beamPitch = Math.atan2(dir.yCoord, beamPlane);

        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        GlStateManager.pushMatrix();

        int alpha = 80; // 64;
        color = (alpha << 24) | color;

        float time = (player.ticksExisted + partialTicks);

        for (int i = 0; i <= 4; i++)
        {
            float scale_xy = scale * (float) Math.pow(0.8, i);
            float scale_z = scale * (float) Math.pow(1.05, i);
            float offset_z = 0.5f + 0.005f * i;

            float angle = time * (6 + 3 * (4-i)) * ((i&1)==0?1:-1);

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

            RenderingStuffs.renderModel(modelCone, color);

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
