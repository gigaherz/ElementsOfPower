package gigaherz.elementsofpower.database;

public class Utils
{
    public static int gcd(int a, int b)
    {
        for (; ; )
        {
            if (a == 0) return b;
            b %= a;
            if (b == 0) return a;
            a %= b;
        }
    }

    public static int lcm(int a, int b)
    {
        int temp = gcd(a, b);
        return temp > 0 ? (a / temp * b) : 0;
    }
}
