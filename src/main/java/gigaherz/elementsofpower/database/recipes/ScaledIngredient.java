package gigaherz.elementsofpower.database.recipes;

import net.minecraft.item.crafting.Ingredient;

public class ScaledIngredient
{
    public final Ingredient ingredient;
    public final double scale;

    public ScaledIngredient(Ingredient ingredient, double scale)
    {
        this.ingredient = ingredient;
        this.scale = scale;
    }

    public ScaledIngredient scale(double scale)
    {
        return new ScaledIngredient(ingredient, this.scale * scale);
    }
}
