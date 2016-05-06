package gigaherz.elementsofpower.integration;

import gigaherz.elementsofpower.ElementsOfPower;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ContainerChargeRecipeWrapper extends BlankRecipeWrapper implements ICraftingRecipeWrapper
{

    private final List<ItemStack> inputs;
    private final List<ItemStack> outputs;

    public ContainerChargeRecipeWrapper(ItemStack output, ItemStack... inputs)
    {
        this.inputs = Arrays.asList(inputs);
        outputs = Collections.singletonList(output);
    }

    @Nonnull
    @Override
    public List<ItemStack> getInputs()
    {
        return inputs;
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputs()
    {
        return outputs;
    }
}
