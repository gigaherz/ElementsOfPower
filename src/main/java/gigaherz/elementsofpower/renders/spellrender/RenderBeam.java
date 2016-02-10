package gigaherz.elementsofpower.renders.spellrender;

import gigaherz.elementsofpower.renders.RenderingStuffs;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.FlameEffect;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import org.lwjgl.opengl.GL11;

public class RenderBeam extends RenderSpell
{

    @Override
    public void doRender(Spellcast cast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset, String tex)
    {
        int beam_color = cast.getColor();
        float scale = 0.15f * cast.getScale();

        if (tex != null)
        {
            beam_color = 0xFFFFFF;
        }

        IFlexibleBakedModel modelSphere = RenderingStuffs.loadModelRetextured("elementsofpower:entity/sphere.obj",
                "#Default", tex);
        IFlexibleBakedModel modelCyl = RenderingStuffs.loadModelRetextured("elementsofpower:entity/cylinder.obj",
                "#Default", tex);

        MovingObjectPosition mop = cast.getHitPosition();

        Vec3 start = cast.start;
        Vec3 end = cast.end;

        Vec3 beam0 = end.subtract(start);

        start = start.add(offset);

        Vec3 beam = end.subtract(start);
        Vec3 dir = beam.normalize();

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

            float angle = time * (6 + 3 * (4+i)) * ((i&1)==0?1:-1) * 0.1f;

            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(
                        (float) (x + offset.xCoord),
                        (float) (y + offset.yCoord),
                        (float) (z + offset.zCoord));
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw) + 90, 0, 1, 0);
                GlStateManager.rotate(-(float) Math.toDegrees(beamPitch), 1, 0, 0);
                GlStateManager.rotate(angle, 0, 0, 1);
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
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw) + 90, 0, 1, 0);
                GlStateManager.rotate(-(float) Math.toDegrees(beamPitch), 1, 0, 0);
                GlStateManager.rotate(angle, 0, 0, 1);
                GlStateManager.scale(scale_beam, scale_beam, distance);

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
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw) + 90, 0, 1, 0);
                GlStateManager.rotate(-(float) Math.toDegrees(beamPitch), 1, 0, 0);
                GlStateManager.rotate(angle, 0, 0, 1);
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
