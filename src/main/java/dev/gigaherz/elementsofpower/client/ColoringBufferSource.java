package dev.gigaherz.elementsofpower.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ColoringBufferSource implements MultiBufferSource
{
    public final float _r;
    public final float _g;
    public final float _b;
    public final float _a;
    private final MultiBufferSource inner;

    public ColoringBufferSource(float r, float g, float b, float a, MultiBufferSource inner)
    {
        this._r = r;
        this._g = g;
        this._b = b;
        this._a = a;
        this.inner = inner;
    }

    @Override
    public VertexConsumer getBuffer(RenderType rt)
    {
        return new ConsumerWrapper(inner.getBuffer(rt));
    }

    private class ConsumerWrapper implements VertexConsumer
    {
        private final VertexConsumer buffer;

        public ConsumerWrapper(VertexConsumer buffer)
        {
            this.buffer = buffer;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z)
        {
            return buffer.addVertex(x,y,z);
        }

        @Override
        public VertexConsumer addVertex(Matrix4f pose, float x, float y, float z)
        {
            return buffer.addVertex(pose, x, y, z);
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a)
        {
            return buffer.setColor((int) (r * _r), (int) (g * _g), (int) (b * _b), (int) (a * _a));
        }

        @Override
        public VertexConsumer setUv(float u, float v)
        {
            return buffer.setUv(u,v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v)
        {
            return buffer.setUv1(u,v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v)
        {
            return buffer.setUv2(u,v);
        }

        @Override
        public VertexConsumer setNormal(PoseStack.Pose pose, float x, float y, float z)
        {
            return buffer.setNormal(pose, x, y, z);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z)
        {
            return buffer.setNormal(x, y, z);
        }
    }
}
