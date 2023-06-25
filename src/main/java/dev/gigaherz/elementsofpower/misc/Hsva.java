package dev.gigaherz.elementsofpower.misc;

import net.minecraft.util.FastColor;

record Hsva(int h, int s, int v, int a)
{
    static {
        Hsva t;

        t = fromRgb(0x00000000);
        if (t.toRgb() != 0)
            throw new RuntimeException("Test fail!");

        t = fromRgb(0xFF000000);
        if (t.toRgb() != 0xFF000000)
            throw new RuntimeException("Test fail!");

        t = fromRgb(0xFFFF0000);
        if (t.toRgb() != 0xFFFF0000)
            throw new RuntimeException("Test fail!");

        t = fromRgb(0xFF00FF00);
        if (t.toRgb() != 0xFF00FF00)
            throw new RuntimeException("Test fail!");

        t = fromRgb(0xFF0000FF);
        if (t.toRgb() != 0xFF0000FF)
            throw new RuntimeException("Test fail!");

        t = fromRgb(0xFF010101);
        if (t.toRgb() != 0xFF010101)
            throw new RuntimeException("Test fail!");

        t = fromRgb(0xFF010203);
        if (t.toRgb() != 0xFF010103)
            throw new RuntimeException("Test fail!");

        t = fromRgb(0xFF7F7F7F);
        if (t.toRgb() != 0xFF7F7F7F)
            throw new RuntimeException("Test fail!");

        t = fromRgb(0xFF7FFF7F);
        if (t.toRgb() != 0xFF7FFF7F)
            throw new RuntimeException("Test fail!");
    }


    public static Hsva fromRgb(int color)
    {
        int a = FastColor.ARGB32.alpha(color);
        int r = FastColor.ARGB32.red(color);
        int g = FastColor.ARGB32.green(color);
        int b = FastColor.ARGB32.blue(color);

        int h, s, v;

        int min, max, delta;

        min = Math.min(Math.min(r, g), b);
        max = Math.max(Math.max(r, g), b);

        // V/* www  .  j  a v a 2 s . co  m*/
        v = max;

        delta = max - min;

        // S
        if (max != 0)
            s = delta * 255 / max;
        else
        {
            s = 0;
            h = -1;
            return new Hsva(h, s, v, a);
        }

        if (delta == 0)
            return new Hsva(0, s, v, a);

        // H
        if (r == max)
            h = (g - b) * 59 / delta; // between yellow & magenta
        else if (g == max)
            h = 120 + (b - r) * 59 / delta; // between cyan & yellow
        else
            h = 240 + (r - g) * 59 / delta; // between magenta & cyan

        if (h < 0)
            h += 360;

        return new Hsva(h, s, v, a);
    }

    Hsva alpha(int newA)
    {
        return new Hsva(h,s,v,newA);
    }

    int toRgb()
    {
        int r = v;
        int g = v;
        int b = v;

        if (h == -1)
        {
            return FastColor.ARGB32.color(a, r, g, b);
        }

        int h = this.h % 360;
        int i = h / 60;

        int f = h % 60;
        if (i % 2 == 0)
        {
            f = 59 - f;
        }

        int m = v * (255 - s) / 255;
        int n = v * (255 * 59 - s * f) / (255 * 59);

        switch (i)
        {
            case 0 -> {
                g = n;
                b = m;
            }
            case 1 -> {
                r = n;
                b = m;
            }
            case 2 -> {
                r = m;
                b = n;
            }
            case 3 -> {
                r = m;
                g = n;
            }
            case 4 -> {
                r = n;
                g = m;
            }
            case 5 -> {
                g = m;
                b = n;
            }
        }

        return FastColor.ARGB32.color(a, r, g, b);
    }

    public Hsla toHsla()
    {
        var l = v * (255-s/2) / 255;
        var sl = (l==0 || l==255) ? 0 : ((v-l) * 255 / Math.min(l,255-l));
        return new Hsla(h,sl,l,a);
    }
}
