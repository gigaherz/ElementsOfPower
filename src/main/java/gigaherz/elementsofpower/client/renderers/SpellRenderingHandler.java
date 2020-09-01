package gigaherz.elementsofpower.client.renderers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.client.renderers.spells.BeamSpellRenderer;
import gigaherz.elementsofpower.client.renderers.spells.ConeSpellRenderer;
import gigaherz.elementsofpower.client.renderers.spells.SpellRenderer;
import gigaherz.elementsofpower.client.renderers.spells.SphereSpellRenderer;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ElementsOfPowerMod.MODID)
public class SpellRenderingHandler
{
    public static final Map<SpellShape, SpellRenderer> rendererRegistry = Maps.newHashMap();

    static
    {
        rendererRegistry.put(SpellManager.BEAM, new BeamSpellRenderer());
        rendererRegistry.put(SpellManager.CONE, new ConeSpellRenderer());
        rendererRegistry.put(SpellManager.SPHERE, new SphereSpellRenderer());
    }

    @SubscribeEvent
    public static void renderFirstPerson(RenderWorldLastEvent event)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;
        SpellcastEntityData.get(player).ifPresent(data -> {
            Spellcast cast = data.getCurrentCasting();
            if (cast == null)
                return;

            SpellRenderer renderer = rendererRegistry.get(cast.getShape());
            if (renderer != null)
            {
                EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();

                float partialTicks = Minecraft.getInstance().getRenderPartialTicks();

                Vec3d off = player.getUpVector(partialTicks).scale(-0.15);

                IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
                MatrixStack stack = event.getMatrixStack();
                stack.push();
                renderer.render(cast, player, renderManager, partialTicks, stack, buffers, 0x00F000F0, off);
                stack.pop();

                RenderSystem.disableDepthTest();
                buffers.finish();
            }
        });
    }

    @SubscribeEvent
    public static void playerRenderPost(RenderPlayerEvent.Post event)
    {
        PlayerEntity player = event.getPlayer();

        SpellcastEntityData.get(player).ifPresent(data -> {
            Spellcast cast = data.getCurrentCasting();
            if (cast == null)
                return;

            SpellRenderer renderer = rendererRegistry.get(cast.getShape());
            if (renderer != null)
            {
                boolean isSelf = player.getEntityId() == Minecraft.getInstance().player.getEntityId();
                EntityRendererManager renderManager = event.getRenderer().getRenderManager();

                float partialTicks = event.getPartialRenderTick();

                Vec3d upVector = player.getUpVector(partialTicks);

                Vec3d off;
                if (isSelf)
                {
                    off = upVector.scale(-0.15);
                }
                else
                {
                    Vec3d lookVector = player.getLook(partialTicks);
                    Vec3d sideVector = lookVector.crossProduct(upVector);

                    off = sideVector.scale(0.4).add(lookVector.scale(-0.25));
                }
                off = off.add(0, player.getEyeHeight(), 0);

                MatrixStack stack = event.getMatrixStack();

                stack.push();
                renderer.render(cast, player, renderManager, partialTicks, stack, event.getBuffers(), event.getLight(), off);
                stack.pop();
            }
        });
    }
}
