package dev.gigaherz.elementsofpower.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

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
        public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z)
        {
            return buffer.vertex(matrix, x, y, z);
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z)
        {
            return buffer.vertex(x, y, z);
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a)
        {
            return buffer.color((int) (r * _r), (int) (g * _g), (int) (b * _b), (int) (a * _a));
        }

        @Override
        public VertexConsumer color(float r, float g, float b, float a)
        {
            return buffer.color(r * _r, g * _g, b * _b, a * _a);
        }

        @Override
        public VertexConsumer uv(float u, float v)
        {
            return buffer.uv(u, v);
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v)
        {
            return buffer.overlayCoords(u, v);
        }

        @Override
        public VertexConsumer uv2(int u, int v)
        {
            return buffer.uv2(u, v);
        }

        @Override
        public VertexConsumer normal(float x, float y, float z)
        {
            return buffer.normal(x, y, z);
        }

        @Override
        public VertexConsumer normal(Matrix3f matrix, float x, float y, float z)
        {
            return buffer.normal(matrix, x, y, z);
        }

        @Override
        public void endVertex()
        {
            buffer.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a)
        {
            buffer.defaultColor(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor()
        {
            buffer.unsetDefaultColor();
        }
    }
}
