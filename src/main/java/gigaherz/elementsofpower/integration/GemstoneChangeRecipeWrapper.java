package gigaherz.elementsofpower.integration;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class GemstoneChangeRecipeWrapper implements IRecipeWrapper
{

    private final List<ItemStack> inputs;
    private final ItemStack output;

    public GemstoneChangeRecipeWrapper(ItemStack output, ItemStack... inputs)
    {
        this.inputs = Arrays.asList(inputs);
        this.output = output;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, output);
    }
}
