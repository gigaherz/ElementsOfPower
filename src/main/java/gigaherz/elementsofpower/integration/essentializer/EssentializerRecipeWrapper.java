package gigaherz.elementsofpower.integration.essentializer;
/*
import com.google.common.collect.Lists;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.MagicAmounts;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EssentializerRecipeWrapper implements IRecipeWrapper
{
    public static List<EssentializerRecipeWrapper> getRecipes()
    {
        List<EssentializerRecipeWrapper> list = Lists.newArrayList();
        for (Map.Entry<ItemStack, MagicAmounts> entry : EssenceConversions.itemEssences.entrySet())
        {
            list.add(new EssentializerRecipeWrapper(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    List<ItemStack> inputs;
    MagicAmounts outputs;

    private EssentializerRecipeWrapper(ItemStack input, MagicAmounts am)
    {
        inputs = Collections.singletonList(input);
        outputs = am;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
        ingredients.setInputs(ItemStack.class, inputs);
    }

    @Nonnull
    public MagicAmounts getEssences()
    {
        return outputs;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {
        EssentializerCategory.INSTANCE.drawEssenceSlots(minecraft);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        return EssentializerCategory.INSTANCE.getTooltipStrings(mouseX, mouseY);
    }
}
*/