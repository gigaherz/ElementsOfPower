package gigaherz.elementsofpower.renders.spellrender;

import gigaherz.elementsofpower.renders.RenderingStuffs;
import gigaherz.elementsofpower.spells.cast.shapes.SpellcastBeam;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import org.lwjgl.opengl.GL11;

public class RenderBeam extends RenderSpell<SpellcastBeam>
{

    @Override
    public void doRender(SpellcastBeam spellcast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset)
    {
        IFlexibleBakedModel modelSphere = RenderingStuffs.loadModel("elementsofpower:entity/sphere.obj");
        IFlexibleBakedModel modelCyl = RenderingStuffs.loadModel("elementsofpower:entity/cylinder.obj");

        int beam_color = spellcast.getColor();
        float scale = 0.15f * spellcast.getEffect().getScale();
        float maxDistance = 10.0f;

        Vec3 start = player.getPositionEyes(partialTicks);
        Vec3 dir = player.getLook(partialTicks);
        Vec3 end = start.addVector(dir.xCoord * maxDistance, dir.yCoord * maxDistance, dir.zCoord * maxDistance);
        MovingObjectPosition mop = player.worldObj.rayTraceBlocks(start, end, false, true, false);

        if (mop != null && mop.hitVec != null)
            end = mop.hitVec;

        Vec3 beam0 = end.subtract(start);

        start = start.add(offset);

        Vec3 beam = end.subtract(start);
        dir = beam.normalize();

        double distance = beam.lengthVector();

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

        for (int i = 0; i <= 4; i++)
        {
            float tt = (i + (player.ticksExisted % 10 + partialTicks) / 11.0f) / 5.0f;
            float scale_base = scale * (1 + 0.5f * tt);
            float scale_start = scale_base * 0.3f;
            float scale_beam = scale_base * 0.3f;
            float scale_end = scale_base * 1.2f;

            int alpha = 255 - (i == 0 ? 0 : (int) (tt * 255));
            int color = (alpha << 24) | beam_color;

            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(
                        (float) (x + offset.xCoord),
                        (float) (y + offset.yCoord),
                        (float) (z + offset.zCoord));
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw), 0, 1, 0);
                GlStateManager.rotate((float) Math.toDegrees(beamPitch) - 90, 0, 0, 1);
                GlStateManager.scale(scale_start, scale_start, scale_start);

                RenderingStuffs.renderModel(modelSphere, color);

                GlStateManager.popMatrix();
            }

            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(
                        (float) (x + offset.xCoord),
                        (float) (y + offset.yCoord),
                        (float) (z + offset.zCoord));
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw), 0, 1, 0);
                GlStateManager.rotate((float) Math.toDegrees(beamPitch) - 90, 0, 0, 1);
                GlStateManager.scale(scale_beam, distance, scale_beam);

                RenderingStuffs.renderModel(modelCyl, color);

                GlStateManager.popMatrix();
            }

            if (mop != null && mop.hitVec != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(
                        (float) (x + beam0.xCoord),
                        (float) (y + beam0.yCoord),
                        (float) (z + beam0.zCoord));
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw), 0, 1, 0);
                GlStateManager.rotate((float) Math.toDegrees(beamPitch) - 90, 0, 0, 1);
                GlStateManager.scale(scale_end, scale_end, scale_end);

                RenderingStuffs.renderModel(modelSphere, color);

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
