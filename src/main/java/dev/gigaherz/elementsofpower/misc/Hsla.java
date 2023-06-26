package dev.gigaherz.elementsofpower.misc;

record Hsla(int h, int s, int l, int a)
{
    public static Hsla fromRgb(int color)
    {
        return Hsva.fromRgb(color).toHsla();
    }

    public static Hsla fromHsv(Hsva hsv)
    {
        var h = hsv.h();
        var s = hsv.s();
        var v = hsv.v();
        var a = hsv.a();
        var l = v * (255-s/2) / 255;
        var sl = (l==0 || l==255) ? 0 : ((v-l) * 255 / Math.min(l,255-l));
        return new Hsla(h,sl,l,a);
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
