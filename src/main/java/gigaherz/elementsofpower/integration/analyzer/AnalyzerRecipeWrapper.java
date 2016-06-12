package gigaherz.elementsofpower.integration.analyzer;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class AnalyzerRecipeWrapper extends BlankRecipeWrapper
{
    public static List<AnalyzerRecipeWrapper> getRecipes()
    {
        @SuppressWarnings("unchecked")
        List<Pair<Gemstone, String>> gems = Lists.newArrayList(
                Pair.of(Gemstone.Ruby, "gemRuby"),
                Pair.of(Gemstone.Sapphire, "gemSapphire"),
                Pair.of(Gemstone.Citrine, "gemCitrine"),
                Pair.of(Gemstone.Agate, "gemAgate"),
                Pair.of(Gemstone.Quartz, "gemQuartz"),
                Pair.of(Gemstone.Serendibite, "gemSerendibite"),
                Pair.of(Gemstone.Emerald, "gemEmerald"),
                Pair.of(Gemstone.Amethyst, "gemAmethyst"),
                Pair.of(Gemstone.Diamond, "gemDiamond")
        );

        List<AnalyzerRecipeWrapper> list = Lists.newArrayList();

        for (Pair<Gemstone, String> pair : gems)
        {
            list.add(new AnalyzerRecipeWrapper(pair.getRight(), pair.getLeft()));
        }

        return list;
    }

    List<String> inputs;
    List<ItemStack> outputs;

    private AnalyzerRecipeWrapper(String oreDict, Gemstone gem)
    {
        inputs = Collections.singletonList(oreDict);
        outputs = Lists.newArrayList();
        for (Quality q : Quality.values)
        {
            outputs.add(ElementsOfPower.gemstone.setQuality(ElementsOfPower.gemstone.getStack(gem), q));
        }
    }

    @Nonnull
    @Override
    public List getInputs()
    {
        return inputs;
    }

    @Nonnull
    @Override
    public List getOutputs()
    {
        return outputs;
    }
}
