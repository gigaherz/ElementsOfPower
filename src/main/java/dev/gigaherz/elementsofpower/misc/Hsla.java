package dev.gigaherz.elementsofpower.misc;

import net.minecraft.util.FastColor;

record Hsla(int h, int s, int l, int a)
{
    public static Hsla fromRgb(int color)
    {
        return Hsva.fromRgb(color).toHsla();
    }

    Hsla alpha(int newA)
    {
        return new Hsla(h,s,l,newA);
    }

    int toRgb()
    {
        return toHsva().toRgb();
    }

    public Hsva toHsva()
    {
        var v = l + s * Math.min(l,255-l) / 255;
        var sv = (v == 0) ? 0 : (2 * (255 - l*255/v));
        return new Hsva(h,sv,v,a);
    }
}
