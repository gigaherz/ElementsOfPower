package dev.gigaherz.purrticles.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.gigaherz.purrticles.ParticleModule;
import dev.gigaherz.purrticles.ParticleSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class CrappyParticleRenderer
{
    private final List<ParticleSystem> systemsToRender = Lists.newArrayList();

    public CrappyParticleRenderer()
    {
        NeoForge.EVENT_BUS.addListener(this::renderEvent);
    }

    private void renderEvent(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;

        float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack stack = event.getPoseStack();

        stack.pushPose();
        render(stack, buffers, partialTicks);
        stack.popPose();

        RenderSystem.disableDepthTest();
        buffers.endBatch();
    }

    private void render(PoseStack stack, MultiBufferSource buffers, float partialTicks)
    {
        for (ParticleSystem system : systemsToRender)
        {
            RenderType rt = RenderType.entityTranslucent(TextureAtlas.LOCATION_PARTICLES);
            VertexConsumer buffer = buffers.getBuffer(rt);
            fill(stack, buffer, system, partialTicks);
        }
    }

    private void fill(PoseStack stack, VertexConsumer buffer, ParticleSystem system, float partialTicks)
    {
        int usedSize = system.size();

        RenderProperties renderProperties = system.getModule(RenderProperties.class);

        float[] ss = renderProperties.scaleChannel.get();

        float[] xp = renderProperties.xPositionChannel.get();
        float[] yp = renderProperties.yPositionChannel.get();
        float[] zp = renderProperties.zPositionChannel.get();

        float[] xn = renderProperties.xNormalChannel.get();
        float[] yn = renderProperties.yNormalChannel.get();
        float[] zn = renderProperties.zNormalChannel.get();

        float[] u0t = renderProperties.u0TextureChannel.get();
        float[] v0t = renderProperties.v0TextureChannel.get();
        float[] u1t = renderProperties.u1TextureChannel.get();
        float[] v1t = renderProperties.v1TextureChannel.get();

        float[] rc = renderProperties.rColorChannel.get();
        float[] gc = renderProperties.gColorChannel.get();
        float[] bc = renderProperties.bColorChannel.get();
        float[] ac = renderProperties.aColorChannel.get();

        for (int i = 0; i < usedSize; i++)
        {
            float scale = ss[i];

            stack.pushPose();
            stack.translate(xp[i], yp[i], zp[i]);
            stack.scale(scale, scale, scale);

            PoseStack.Pose pose = stack.last();
            Matrix4f posMatrix = pose.pose();

            buffer.addVertex(posMatrix, 0, 0, 0)
                    .setUv(u0t[i], v0t[i])
                    .setColor(rc[i], gc[i], bc[i], ac[i])
                    .setNormal(pose, xn[i], yn[i], zn[i]);

            buffer.addVertex(posMatrix, 1, 0, 0)
                    .setUv(u1t[i], v0t[i])
                    .setColor(rc[i], gc[i], bc[i], ac[i])
                    .setNormal(pose, xn[i], yn[i], zn[i]);

            buffer.addVertex(posMatrix, 1, 1, 0)
                    .setUv(u1t[i], v1t[i])
                    .setColor(rc[i], gc[i], bc[i], ac[i])
                    .setNormal(pose, xn[i], yn[i], zn[i]);

            buffer.addVertex(posMatrix, 0, 1, 0)
                    .setUv(u0t[i], v1t[i])
                    .setColor(rc[i], gc[i], bc[i], ac[i])
                    .setNormal(pose, xn[i], yn[i], zn[i]);

            stack.popPose();
        }
    }

    public static class RenderProperties extends ParticleModule
    {
        private Supplier<float[]> xPositionChannel;
        private Supplier<float[]> yPositionChannel;
        private Supplier<float[]> zPositionChannel;
        private Supplier<float[]> u0TextureChannel;
        private Supplier<float[]> v0TextureChannel;
        private Supplier<float[]> u1TextureChannel;
        private Supplier<float[]> v1TextureChannel;
        private Supplier<float[]> rColorChannel;
        private Supplier<float[]> gColorChannel;
        private Supplier<float[]> bColorChannel;
        private Supplier<float[]> aColorChannel;
        private Supplier<float[]> xNormalChannel;
        private Supplier<float[]> yNormalChannel;
        private Supplier<float[]> zNormalChannel;
        private Supplier<float[]> scaleChannel;
        private Supplier<float[]> timeChannel;

        public RenderProperties()
        {
            super.addConsumes(
                    "position.x",
                    "position.y",
                    "position.z",
                    "texture.u0",
                    "texture.v0",
                    "texture.u1",
                    "texture.v1",
                    "color.r",
                    "color.g",
                    "color.b",
                    "color.a",
                    "normal.x",
                    "normal.y",
                    "normal.z",
                    "scale",
                    "time"
            );
        }

        @Override
        public void setIndices(Function<String, Supplier<float[]>> indices)
        {
            xPositionChannel = indices.apply("position.x");
            yPositionChannel = indices.apply("position.y");
            zPositionChannel = indices.apply("position.z");
            u0TextureChannel = indices.apply("texture.u0");
            v0TextureChannel = indices.apply("texture.v0");
            u1TextureChannel = indices.apply("texture.u1");
            v1TextureChannel = indices.apply("texture.v1");
            rColorChannel = indices.apply("color.r");
            gColorChannel = indices.apply("color.g");
            bColorChannel = indices.apply("color.b");
            aColorChannel = indices.apply("color.a");
            xNormalChannel = indices.apply("normal.x");
            yNormalChannel = indices.apply("normal.y");
            zNormalChannel = indices.apply("normal.z");
            scaleChannel = indices.apply("scale");
            timeChannel = indices.apply("time");
        }

        @Override
        public void update(ParticleSystem system, int usedSize)
        {

        }
    }
}
