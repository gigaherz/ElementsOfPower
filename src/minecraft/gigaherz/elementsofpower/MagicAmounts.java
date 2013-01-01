package gigaherz.elementsofpower;

public class MagicAmounts
{
    public enum MagicTypes { fire, water, air, earth, light, darkness, life, death }

    public int[] amounts;

    public MagicAmounts()
    {
        amounts = new int[8];
    }

    public MagicAmounts(int fire, int water, int air, int earth, int light, int darkness, int life, int death)
    {
        amounts = new int[] { fire, water, air, earth, light, darkness, life, death };
    }

    public int getTotalMagic()
    {
        int acc = 0;

        for (int i = 0; i < amounts.length; i++)
        {
            acc += amounts[i];
        }

        return acc;
    }

    public int getAmountOfType(int type)
    {
        return amounts[type];
    }

    public boolean isEmpty()
    {
        for (int i = 0; i < amounts.length; i++)
        {
            if (amounts[i] > 0)
            {
                return false;
            }
        }

        return true;
    }

    public void setAmountOfType(int type, int amount)
    {
        amounts[type] = amount;
    }

    public MagicAmounts fire(int amount)
    {
        amounts[0] += amount;
        return this;
    }
    public MagicAmounts water(int amount)
    {
        amounts[1] += amount;
        return this;
    }
    public MagicAmounts air(int amount)
    {
        amounts[2] += amount;
        return this;
    }
    public MagicAmounts earth(int amount)
    {
        amounts[3] += amount;
        return this;
    }
    public MagicAmounts light(int amount)
    {
        amounts[4] += amount;
        return this;
    }
    public MagicAmounts darkness(int amount)
    {
        amounts[5] += amount;
        return this;
    }
    public MagicAmounts life(int amount)
    {
        amounts[6] += amount;
        return this;
    }
    public MagicAmounts death(int amount)
    {
        amounts[7] += amount;
        return this;
    }
    public MagicAmounts all(int amount)
    {
        for (int i = 0; i < amounts.length; i++)
        {
            amounts[i] += amount;
        }

        return this;
    }
}
