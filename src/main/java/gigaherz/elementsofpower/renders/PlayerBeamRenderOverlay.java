package gigaherz.elementsofpower.renders;

import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class PlayerBeamRenderOverlay
{

    @SubscribeEvent
    public void renderFirstPerson(RenderWorldLastEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        drawSpellsOnPlayer(player, renderManager, 0, player.getEyeHeight(), 0, event.partialTicks);
    }

    @SubscribeEvent
    public void playerRenderPost(RenderPlayerEvent.Post event)
    {
        if (event.entityPlayer == Minecraft.getMinecraft().thePlayer)
            return;

        EntityPlayer player = event.entityPlayer;
        RenderManager renderManager = event.renderer.getRenderManager();

        drawSpellsOnPlayer(player, renderManager, event.x, event.y + player.getEyeHeight(), event.z, event.partialRenderTick);
    }

    public void drawSpellsOnPlayer(EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks)
    {
        SpellcastEntityData data = SpellcastEntityData.get(player);

        // TODO: add special effects for other spell tipes that I may add in the future
        if(!data.isCastingBeam())
            return;

        IFlexibleBakedModel modelSphere = RenderingStuffs.loadModel("elementsofpower:entity/sphere.obj");
        IFlexibleBakedModel modelCyl = RenderingStuffs.loadModel("elementsofpower:entity/cylinder.obj");

        int beam_color = data.getCurrentCasting().getEffect().getColor();
        float scale = 0.15f * data.getCurrentCasting().getEffect().getScale();
        float maxDistance = 10.0f;

        float ppitch = player.prevRotationPitch + partialTicks * (player.rotationPitch - player.prevRotationPitch);
        float pyaw = player.prevRotationYawHead + partialTicks * (player.rotationYawHead - player.prevRotationYawHead);

        Vec3 off = new Vec3(0, -0.15, 0);
        off = off.rotatePitch(-(float)Math.toRadians(ppitch));
        off = off.rotateYaw(-(float)Math.toRadians(pyaw));

        Vec3 start = player.getPositionEyes(partialTicks);
        Vec3 dir = player.getLook(partialTicks);
        Vec3 end = start.addVector(dir.xCoord * maxDistance, dir.yCoord * maxDistance, dir.zCoord * maxDistance);
        MovingObjectPosition mop = player.worldObj.rayTraceBlocks(start, end, false, true, false);

        if(mop != null && mop.hitVec != null)
            end = mop.hitVec;

        Vec3 beam0 = end.subtract(start);

        start = start.add(off);

        Vec3 beam = end.subtract(start);
        dir = beam.normalize();

        double distance = beam.lengthVector();

        double beamPlane = Math.sqrt(dir.xCoord*dir.xCoord + dir.zCoord*dir.zCoord);
        double beamYaw = Math.atan2(dir.zCoord, dir.xCoord);
        double beamPitch = Math.atan2(dir.yCoord, beamPlane);

        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        GlStateManager.pushMatrix();

        for(int i=0;i<=4;i++)
        {
            float tt = (i+(player.ticksExisted % 10 + partialTicks) / 11.0f)/5.0f;
            float tscale = scale * 1.2f *(1 + 0.5f * tt);

            int alpha = 255 - (i==0 ? 0 : (int)(tt*255));
            int color = (alpha << 24) | beam_color;

            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    (float) (x + off.xCoord),
                    (float) (y + off.yCoord),
                    (float) (z + off.zCoord));
            GlStateManager.rotate(-(float) Math.toDegrees(beamYaw), 0, 1, 0);
            GlStateManager.rotate((float) Math.toDegrees(beamPitch) - 90, 0, 0, 1);
            GlStateManager.scale(tscale * 0.25, distance, tscale * 0.25);

            RenderingStuffs.renderModel(modelCyl, color);

            GlStateManager.popMatrix();

            if(mop != null && mop.hitVec != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(
                        (float) (x + beam0.xCoord),
                        (float) (y + beam0.yCoord),
                        (float) (z + beam0.zCoord));
                GlStateManager.rotate(-(float) Math.toDegrees(beamYaw), 0, 1, 0);
                GlStateManager.rotate((float) Math.toDegrees(beamPitch) - 90, 0, 0, 1);
                GlStateManager.scale(tscale, tscale, tscale);

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
