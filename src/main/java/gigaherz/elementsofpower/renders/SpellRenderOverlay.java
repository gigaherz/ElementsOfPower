package gigaherz.elementsofpower.renders;

import com.google.common.collect.Maps;
import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import gigaherz.elementsofpower.renders.spellrender.RenderBeam;
import gigaherz.elementsofpower.renders.spellrender.RenderSpell;
import gigaherz.elementsofpower.spells.cast.Spellcast;
import gigaherz.elementsofpower.spells.cast.shapes.SpellcastBeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class SpellRenderOverlay
{
    public static final Map<Class<? extends Spellcast>, RenderSpell> rendererRegistry = Maps.newHashMap();

    static {
        rendererRegistry.put(SpellcastBeam.class, new RenderBeam());
        //rendererRegistry.put(ConeBase.class, new RenderBeam());
    }

    @SuppressWarnings("unchecked")
    public static RenderSpell getRenderer(Spellcast cast)
    {
        Class<? extends Spellcast> clazz = cast.getClass();

        while(clazz != null)
        {
            RenderSpell renderer = rendererRegistry.get(clazz);
            if(renderer != null)
                return renderer;

            Class<?> clazz2 = clazz.getSuperclass();
            if(!Spellcast.class.isAssignableFrom(clazz2))
                break;

            clazz = (Class<? extends Spellcast>) clazz2;
        }

        return null;
    }


    @SubscribeEvent
    public void renderFirstPerson(RenderWorldLastEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        float ppitch = player.prevRotationPitch + event.partialTicks * (player.rotationPitch - player.prevRotationPitch);
        float pyaw = player.prevRotationYawHead + event.partialTicks * (player.rotationYawHead - player.prevRotationYawHead);

        Vec3 off = new Vec3(0, -0.15, 0);
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

        Vec3 off;
        if (isSelf)
        {
            off = new Vec3(0, -0.15, 0);
            off = off.rotatePitch(-(float) Math.toRadians(ppitch));
            off = off.rotateYaw(-(float) Math.toRadians(pyaw));
        }
        else
        {
            off = new Vec3(0, 0, 0.4);
            off = off.rotatePitch(-(float) Math.toRadians(ppitch));
            off = off.rotateYaw(-(float) Math.toRadians(pyaw));
            off = off.add(new Vec3(0, -0.25, 0));
        }

        drawSpellsOnPlayer(player, renderManager, event.x, event.y + player.getEyeHeight(), event.z, event.partialRenderTick, off);
    }

    @SuppressWarnings("unchecked")
    public void drawSpellsOnPlayer(EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset)
    {
        SpellcastEntityData data = SpellcastEntityData.get(player);

        Spellcast cast = data.getCurrentCasting();

        if(cast == null)
            return;

        RenderSpell renderer = getRenderer(cast);
        if(renderer != null)
        {
            renderer.doRender(cast, player, renderManager, x, y, z, partialTicks, offset);
        }
    }
}
