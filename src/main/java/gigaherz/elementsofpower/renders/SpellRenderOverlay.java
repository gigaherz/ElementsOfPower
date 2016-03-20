package gigaherz.elementsofpower.renders;

import com.google.common.collect.Maps;
import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import gigaherz.elementsofpower.renders.spellrender.RenderBeam;
import gigaherz.elementsofpower.renders.spellrender.RenderCone;
import gigaherz.elementsofpower.renders.spellrender.RenderSpell;
import gigaherz.elementsofpower.renders.spellrender.RenderSphere;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class SpellRenderOverlay
{
    public static final Map<SpellShape, RenderSpell> rendererRegistry = Maps.newHashMap();

    static
    {
        rendererRegistry.put(SpellManager.beam, new RenderBeam());
        rendererRegistry.put(SpellManager.cone, new RenderCone());
        rendererRegistry.put(SpellManager.sphere, new RenderSphere());
    }

    @SubscribeEvent
    public void renderFirstPerson(RenderWorldLastEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        float ppitch = player.prevRotationPitch + event.partialTicks * (player.rotationPitch - player.prevRotationPitch);
        float pyaw = player.prevRotationYawHead + event.partialTicks * (player.rotationYawHead - player.prevRotationYawHead);

        Vec3d off = new Vec3d(0, -0.15, 0);
        off = off.rotatePitch(-(float) Math.toRadians(ppitch));
        off = off.rotateYaw(-(float) Math.toRadians(pyaw));

        drawSpellsOnPlayer(player, renderManager, 0, player.getEyeHeight(), 0, event.partialTicks, off);
    }

    @SubscribeEvent
    public void playerRenderPost(RenderPlayerEvent.Post event)
    {
        if (event.entityPlayer == Minecraft.getMinecraft().thePlayer)
            return;

        boolean isSelf = event.entityPlayer.getEntityId() == Minecraft.getMinecraft().thePlayer.getEntityId();
        EntityPlayer player = event.entityPlayer;
        RenderManager renderManager = event.renderer.getRenderManager();

        float ppitch = player.prevRotationPitch + event.partialRenderTick * (player.rotationPitch - player.prevRotationPitch);
        float pyaw = player.prevRotationYawHead + event.partialRenderTick * (player.rotationYawHead - player.prevRotationYawHead);

        Vec3d off;
        if (isSelf)
        {
            off = new Vec3d(0, -0.15, 0);
            off = off.rotatePitch(-(float) Math.toRadians(ppitch));
            off = off.rotateYaw(-(float) Math.toRadians(pyaw));
        }
        else
        {
            off = new Vec3d(0, 0, 0.4);
            off = off.rotatePitch(-(float) Math.toRadians(ppitch));
            off = off.rotateYaw(-(float) Math.toRadians(pyaw));
            off = off.add(new Vec3d(0, -0.25, 0));
        }

        drawSpellsOnPlayer(player, renderManager, event.x, event.y + player.getEyeHeight(), event.z, event.partialRenderTick, off);
    }

    @SuppressWarnings("unchecked")
    public void drawSpellsOnPlayer(EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3d offset)
    {
        SpellcastEntityData data = SpellcastEntityData.get(player);

        Spellcast cast = data.getCurrentCasting();

        if (cast == null)
            return;

        RenderSpell renderer = rendererRegistry.get(cast.getShape());
        if (renderer != null)
        {
            renderer.doRender(cast, player, renderManager, x, y, z, partialTicks, offset);
        }
    }
}
