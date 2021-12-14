package dev.gigaherz.purrticles.features;

import dev.gigaherz.purrticles.ParticleModule;
import dev.gigaherz.purrticles.ParticleSystem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.function.Function;
import java.util.function.Supplier;

public class SpriteAnimationModule extends ParticleModule
{
    private final TextureAtlasSprite[] sprites;
    private final float[] times;
    private final boolean loop;
    private final float totalTime;

    private Supplier<float[]> timeChannel;
    private Supplier<float[]> u0TextureChannel;
    private Supplier<float[]> v0TextureChannel;
    private Supplier<float[]> u1TextureChannel;
    private Supplier<float[]> v1TextureChannel;

    public SpriteAnimationModule(TextureAtlasSprite[] sprites, float[] times, boolean loop)
    {
        if (sprites.length != times.length)
            throw new IllegalStateException("Sprites array must be the same length as times array");
        this.sprites = sprites;
        this.times = times;
        this.loop = loop;
        float totalTime = 0;
        for (float time : times) totalTime += time;
        this.totalTime = totalTime;
        addConsumes("time");
        addModifies("texture.u0", "texture.v0", "texture.u1", "texture.v1");
    }

    @Override
    public void setIndices(Function<String, Supplier<float[]>> indices)
    {
        timeChannel = indices.apply("time");
        u0TextureChannel = indices.apply("texture.u0");
        v0TextureChannel = indices.apply("texture.v0");
        u1TextureChannel = indices.apply("texture.u1");
        v1TextureChannel = indices.apply("texture.v1");
    }

    @Override
    public void update(ParticleSystem system, int usedSize)
    {
        float[] times = timeChannel.get();

        // texture coords (out)
        float[] u0t = u0TextureChannel.get();
        float[] v0t = v0TextureChannel.get();
        float[] u1t = u1TextureChannel.get();
        float[] v1t = v1TextureChannel.get();

        for (int i = 0; i < usedSize; i++)
        {
            float time = times[i];

            if (loop) time = time % totalTime;

            TextureAtlasSprite s = loop ? sprites[i] : sprites[sprites.length - 1];
            for (int j = 0; j < times.length; j++)
            {
                if (times[j] < time)
                {
                    s = sprites[j];
                    break;
                }
            }

            u0t[i] = s.getU(0);
            v0t[i] = s.getV(0);
            u1t[i] = s.getU(1);
            v1t[i] = s.getV(1);
        }
    }
}
