package gigaherz.elementsofpower.database.recipes.furnace;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.database.recipes.IRecipeInfoProvider;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FurnaceRecipeInfo implements IRecipeInfoProvider
{
    ArrayList<ItemStack> recipeItems;
    ItemStack recipeOutput;

    public FurnaceRecipeInfo(ItemStack input, ItemStack output)
    {
        ItemStack recipeInput = input.copy();

        ArrayList<ItemStack> inputs = Lists.newArrayList();
        inputs.add(recipeInput);
        inputs.add(new ItemStack(Items.COAL));

        recipeItems = inputs;
        recipeOutput = output.copy();

        recipeInput.stackSize *= 8;
        recipeOutput.stackSize *= 8;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput()
    {
        return recipeOutput;
    }

    @Nonnull
    @Override
    public List<ItemStack> getRecipeInputs()
    {
        return recipeItems;
    }
}
