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
    public void doRender(Spellcast cast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset)
    {
        IFlexibleBakedModel modelCone = RenderingStuffs.loadModel("elementsofpower:entity/cone.obj");

        int beam_color = cast.getColor();
        float scale = 0.15f * cast.getScale();
        float maxDistance = 10.0f;

        Vec3 start = player.getPositionEyes(partialTicks);
        Vec3 dir = player.getLook(partialTicks);
        Vec3 end = start.addVector(dir.xCoord * maxDistance, dir.yCoord * maxDistance, dir.zCoord * maxDistance);
        MovingObjectPosition mop = player.worldObj.rayTraceBlocks(start, end, false, true, false);

        if (mop != null && mop.hitVec != null)
            end = mop.hitVec;

        start = start.add(offset);

        Vec3 beam = end.subtract(start);
        dir = beam.normalize();

        double beamPlane = Math.sqrt(dir.xCoord * dir.xCoord + dir.zCoord * dir.zCoord);
        double beamYaw = Math.atan2(dir.zCoord, dir.xCoord);
        double beamPitch = Math.atan2(dir.yCoord, beamPlane);

        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        GlStateManager.pushMatrix();

        int alpha = 64;
        int color = (alpha << 24) | beam_color;

        for (int i = 0; i <= 4; i++)
        {
            float scale_xy = scale * (float) Math.pow(0.8, i);
            float scale_z = scale * (float) Math.pow(0.8, i);
            float offset_z = 0.05f * i;

            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    (float) (x + offset.xCoord),
                    (float) (y + offset.yCoord),
                    (float) (z + offset.zCoord + offset_z));
            GlStateManager.rotate(-(float) Math.toDegrees(beamYaw), 0, 1, 0);
            GlStateManager.rotate((float) Math.toDegrees(beamPitch) - 90, 0, 0, 1);
            GlStateManager.scale(scale_xy, scale_xy, scale_z);

            RenderingStuffs.renderModel(modelCone, color);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
    }
}
