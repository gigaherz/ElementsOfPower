package gigaherz.elementsofpower.renders;

import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public class PlayerBeamRenderOverlay
{
    IFlexibleBakedModel model;

    /*@Override
    public void doRenderLayer(EntityPlayer player, float swing, float prevSwing, float partialTicks, float animationProgress, float relativeHeadYaw, float prevPitch, float scale)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        drawSpellsOnPlayer(player, renderManager, 0, 0, 0, partialTicks);
    }
    */

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

        drawSpellsOnPlayer(player, renderManager, event.x, event.y, event.z, event.partialRenderTick);
    }

    public void drawSpellsOnPlayer(EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks)
    {
        SpellcastEntityData spell = SpellcastEntityData.get(player);

        // TODO: add special effects for other spell tipes that I may add in the future
        if(!spell.isCastingBeam())
            return;

        if(model == null)
        {
            try
            {
                IModel mod = ModelLoaderRegistry.getModel(new ResourceLocation("elementsofpower:entity/sphere.obj"));
                model = mod.bake(mod.getDefaultState(), Attributes.DEFAULT_BAKED_FORMAT,
                        (location) -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
            }
            catch(IOException e)
            {
                throw new ReportedException(new CrashReport("Error loading model for entity", e));
            }
        }

        float scale = 0.15f;
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

        final float step = 0.01f;

        Vec3 vstep = new Vec3(
                dir.xCoord * step,
                dir.yCoord * step,
                dir.zCoord * step);

        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();

        renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        GlStateManager.pushMatrix();

        Vec3 cp = off;

        GlStateManager.pushMatrix();
        GlStateManager.translate(
                (float) (x + cp.xCoord),
                (float) (y + cp.yCoord),
                (float) (z + cp.zCoord));
        GlStateManager.rotate(-(float)Math.toDegrees(beamYaw) + 90, 0, 1, 0);
        GlStateManager.rotate(-(float)Math.toDegrees(beamPitch), 1, 0, 0);
        GlStateManager.rotate((player.ticksExisted % 60 + partialTicks) * 360 / 61.0f, 0, 0, 1);
        GlStateManager.scale(scale*0.5, scale*0.5, distance);

        int beam_color = 0xFFffffff;
        renderBeam(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/sea_lantern"), beam_color);

        GlStateManager.popMatrix();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for(int i=0;i<=4;i++)
        {
            float tt = (i+(player.ticksExisted % 10 + partialTicks) / 11.0f)/5.0f;
            float tscale = 0.25f *(1 + 0.5f * tt);

            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    (float) (x + beam0.xCoord),
                    (float) (y + beam0.yCoord),
                    (float) (z + beam0.zCoord));
            GlStateManager.rotate(-(float) Math.toDegrees(beamYaw), 0, 1, 0);
            GlStateManager.rotate((float) Math.toDegrees(beamPitch), 0, 0, 1);
            GlStateManager.scale(tscale, tscale, tscale);

            int alpha = 255 - (i==0 ? 0 : (int)(tt*255));
            int color = (alpha << 24) | 0xffffff;
            renderModel(model, color);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
    }

    private void renderBeam(TextureAtlasSprite tas, int beamColor)
    {
        float u0 = tas.getInterpolatedU(0);
        float u1 = tas.getInterpolatedU(1);
        float v0 = tas.getInterpolatedV(0);
        float v1 = tas.getInterpolatedV(1);
        
        Vec3 a0 = new Vec3(0,1,0);
        Vec3 b0 = new Vec3(1,0,0);
        Vec3 c0 = new Vec3(0,-1,0);
        Vec3 d0 = new Vec3(-1,0,0);

        Vec3 a1 = new Vec3(0,1,1);
        Vec3 b1 = new Vec3(1,0,1);
        Vec3 c1 = new Vec3(0,-1,1);
        Vec3 d1 = new Vec3(-1,0,1);

        int r = beamColor & 0xFF;
        int g = (beamColor>>8) & 0xFF;
        int b = (beamColor>>16) & 0xFF;
        int a = (beamColor>>24) & 0xFF;

        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        wr.pos(a0.xCoord, a0.yCoord, a0.zCoord).tex(u0,v0).color(r, g, b, a).normal(1,1,0).endVertex();
        wr.pos(a1.xCoord, a1.yCoord, a1.zCoord).tex(u0,v1).color(r, g, b, a).normal(1,1,0).endVertex();
        wr.pos(b1.xCoord, b1.yCoord, b1.zCoord).tex(u1,v1).color(r, g, b, a).normal(1,1,0).endVertex();
        wr.pos(b0.xCoord, b0.yCoord, b0.zCoord).tex(u1,v0).color(r, g, b, a).normal(1,1,0).endVertex();
        wr.pos(b0.xCoord, b0.yCoord, b0.zCoord).tex(u0,v0).color(r, g, b, a).normal(1,-1,0).endVertex();
        wr.pos(b1.xCoord, b1.yCoord, b1.zCoord).tex(u0,v1).color(r, g, b, a).normal(1,-1,0).endVertex();
        wr.pos(c1.xCoord, c1.yCoord, c1.zCoord).tex(u1,v1).color(r, g, b, a).normal(1,-1,0).endVertex();
        wr.pos(c0.xCoord, c0.yCoord, c0.zCoord).tex(u1,v0).color(r, g, b, a).normal(1,-1,0).endVertex();
        wr.pos(c0.xCoord, c0.yCoord, c0.zCoord).tex(u0,v0).color(r, g, b, a).normal(-1,-1,0).endVertex();
        wr.pos(c1.xCoord, c1.yCoord, c1.zCoord).tex(u0,v1).color(r, g, b, a).normal(-1,-1,0).endVertex();
        wr.pos(d1.xCoord, d1.yCoord, d1.zCoord).tex(u1,v1).color(r, g, b, a).normal(-1,-1,0).endVertex();
        wr.pos(d0.xCoord, d0.yCoord, d0.zCoord).tex(u1,v0).color(r, g, b, a).normal(-1,-1,0).endVertex();
        wr.pos(d0.xCoord, d0.yCoord, d0.zCoord).tex(u0,v0).color(r, g, b, a).normal(-1,1,0).endVertex();
        wr.pos(d1.xCoord, d1.yCoord, d1.zCoord).tex(u0,v1).color(r, g, b, a).normal(-1,1,0).endVertex();
        wr.pos(a1.xCoord, a1.yCoord, a1.zCoord).tex(u1,v1).color(r, g, b, a).normal(-1,1,0).endVertex();
        wr.pos(a0.xCoord, a0.yCoord, a0.zCoord).tex(u1,v0).color(r, g, b, a).normal(-1,1,0).endVertex();
        Tessellator.getInstance().draw();
    }

    private void renderModel(IFlexibleBakedModel model)
    {
        renderModel(model, -1);
    }

    private void renderModel(IFlexibleBakedModel model, int color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, model.getFormat());

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            this.renderQuads(worldrenderer, model.getFaceQuads(enumfacing), color);
        }

        this.renderQuads(worldrenderer, model.getGeneralQuads(), color);
        tessellator.draw();
    }

    private void renderQuads(WorldRenderer renderer, List<BakedQuad> quads, int color)
    {
        for (BakedQuad bakedquad : quads)
            LightUtil.renderQuadColor(renderer, bakedquad, color);
    }
}
