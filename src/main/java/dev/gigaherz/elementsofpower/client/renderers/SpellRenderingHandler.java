package dev.gigaherz.elementsofpower.client.renderers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.renderers.spells.BeamSpellRenderer;
import dev.gigaherz.elementsofpower.client.renderers.spells.ConeSpellRenderer;
import dev.gigaherz.elementsofpower.client.renderers.spells.SpellRenderer;
import dev.gigaherz.elementsofpower.client.renderers.spells.SphereSpellRenderer;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.SpellManager;
import dev.gigaherz.elementsofpower.spells.SpellShapes;
import dev.gigaherz.elementsofpower.spells.SpellcastEntityData;
import dev.gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ElementsOfPowerMod.MODID)
public class SpellRenderingHandler
{
    public static final Map<SpellShape, SpellRenderer> rendererRegistry = Maps.newHashMap();

    static
    {
        rendererRegistry.put(SpellShapes.BEAM, new BeamSpellRenderer());
        rendererRegistry.put(SpellShapes.CONE, new ConeSpellRenderer());
        rendererRegistry.put(SpellShapes.SPHERE, new SphereSpellRenderer());
    }

    @SubscribeEvent
    public static void renderFirstPerson(RenderLevelLastEvent event)
    {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;
        SpellcastEntityData.get(player).ifPresent(data -> {
            InitializedSpellcast cast = data.getCurrentCasting();
            if (cast == null)
                return;

            SpellRenderer renderer = rendererRegistry.get(cast.getShape());
            if (renderer != null)
            {
                EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();

                float partialTicks = Minecraft.getInstance().getFrameTime();

                Vec3 off = player.getUpVector(partialTicks).scale(-0.15);

                MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
                PoseStack stack = event.getPoseStack();

                stack.pushPose();
                renderer.render(cast, player, renderManager, partialTicks, stack, buffers, 0x00F000F0, off);
                stack.popPose();

                RenderSystem.disableDepthTest();
                buffers.endBatch();
            }
        });
    }

    @SubscribeEvent
    public static void playerRenderPost(RenderPlayerEvent.Post event)
    {
        Player player = event.getPlayer();

        SpellcastEntityData.get(player).ifPresent(data -> {
            InitializedSpellcast cast = data.getCurrentCasting();
            if (cast == null)
                return;

            SpellRenderer renderer = rendererRegistry.get(cast.getShape());
            if (renderer != null)
            {
                Minecraft mc = Minecraft.getInstance();
                boolean isSelf = player.getId() == mc.player.getId();
                EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();

                float partialTicks = event.getPartialTick();

                Vec3 upVector = player.getUpVector(partialTicks);

                Vec3 off;
                if (isSelf)
                {
                    off = upVector.scale(-0.15);
                }
                else
                {
                    Vec3 lookVector = player.getViewVector(partialTicks);
                    Vec3 sideVector = lookVector.cross(upVector);

                    off = sideVector.scale(0.4).add(lookVector.scale(-0.25));
                }
                off = off.add(0, player.getEyeHeight(), 0);

                PoseStack stack = event.getPoseStack();

                stack.pushPose();
                renderer.render(cast, player, renderManager, partialTicks, stack, event.getMultiBufferSource(), event.getPackedLight(), off);
                stack.popPose();
            }
        });
    }
}
