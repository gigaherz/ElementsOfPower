package gigaherz.elementsofpower.client.renderers;

import com.google.common.collect.Maps;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.client.renderers.spellrender.RenderBeam;
import gigaherz.elementsofpower.client.renderers.spellrender.RenderCone;
import gigaherz.elementsofpower.client.renderers.spellrender.RenderSpell;
import gigaherz.elementsofpower.client.renderers.spellrender.RenderSphere;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ElementsOfPowerMod.MODID)
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
    public static void renderFirstPerson(RenderWorldLastEvent event)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();

        float partialTicks = event.getPartialTicks();

        float ppitch = player.prevRotationPitch + partialTicks * (player.rotationPitch - player.prevRotationPitch);
        float pyaw = player.prevRotationYawHead + partialTicks * (player.rotationYawHead - player.prevRotationYawHead);

        Vec3d off = new Vec3d(0, -0.15, 0);
        off = off.rotatePitch(-(float) Math.toRadians(ppitch));
        off = off.rotateYaw(-(float) Math.toRadians(pyaw));

        drawSpellsOnPlayer(player, renderManager, 0, player.getEyeHeight(), 0, partialTicks, off);
    }

    @SubscribeEvent
    public static void playerRenderPost(RenderPlayerEvent.Post event)
    {
        if (event.getEntityPlayer() == Minecraft.getInstance().player)
            return;

        boolean isSelf = event.getEntityPlayer().getEntityId() == Minecraft.getInstance().player.getEntityId();
        PlayerEntity player = event.getEntityPlayer();
        EntityRendererManager renderManager = event.getRenderer().getRenderManager();

        float partialTicks = event.getPartialRenderTick();

        float ppitch = player.prevRotationPitch + partialTicks * (player.rotationPitch - player.prevRotationPitch);
        float pyaw = player.prevRotationYawHead + partialTicks * (player.rotationYawHead - player.prevRotationYawHead);

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

        drawSpellsOnPlayer(player, renderManager, event.getX(), event.getY() + player.getEyeHeight(), event.getZ(), partialTicks, off);
    }

    @SuppressWarnings("unchecked")
    public static void drawSpellsOnPlayer(PlayerEntity player, EntityRendererManager renderManager, double x, double y, double z, float partialTicks, Vec3d offset)
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
