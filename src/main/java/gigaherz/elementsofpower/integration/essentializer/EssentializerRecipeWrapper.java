package gigaherz.elementsofpower.integration.essentializer;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Element;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EssentializerRecipeWrapper extends BlankRecipeWrapper
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
    List<ItemStack> outputs;

    private EssentializerRecipeWrapper(ItemStack output, MagicAmounts am)
    {
        inputs = Collections.singletonList(output);
        outputs = Lists.newArrayList();

        for (int ord = 0; ord < Element.values.length; ord++)
        {
            if (am.amounts[ord] > 0)
                outputs.add(new ItemStack(ElementsOfPower.magicOrb, (int) am.amounts[ord], ord));
            else
                outputs.add(null);
        }
    }

    @Override
    public List getInputs()
    {
        return inputs;
    }

    @Override
    public List getOutputs()
    {
        return outputs;
    }
}
