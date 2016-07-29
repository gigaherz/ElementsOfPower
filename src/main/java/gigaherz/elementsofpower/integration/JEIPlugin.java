package gigaherz.elementsofpower.integration;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.integration.analyzer.AnalyzerCategory;
import gigaherz.elementsofpower.integration.analyzer.AnalyzerRecipeHandler;
import gigaherz.elementsofpower.integration.analyzer.AnalyzerRecipeWrapper;
import gigaherz.elementsofpower.integration.essentializer.EssentializerCategory;
import gigaherz.elementsofpower.integration.essentializer.EssentializerRecipeHandler;
import gigaherz.elementsofpower.integration.essentializer.EssentializerRecipeWrapper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin
{
    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        registry.addRecipeCategories(
                new EssentializerCategory(registry.getJeiHelpers().getGuiHelper()),
                new AnalyzerCategory(registry.getJeiHelpers().getGuiHelper()));

        registry.addRecipeHandlers(
                new ContainerChargeRecipeHandler(),
                new GemstoneChangeRecipeHandler(),
                new EssentializerRecipeHandler(),
                new AnalyzerRecipeHandler());

        registry.addRecipeCategoryCraftingItem(new ItemStack(ElementsOfPower.essentializer), EssentializerCategory.UID);
        registry.addRecipeCategoryCraftingItem(new ItemStack(ElementsOfPower.analyzer), AnalyzerCategory.UID);

        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new EssentializerCategory.TransferInfo());

        addContainerRecipes(registry);

        registry.addRecipes(EssentializerRecipeWrapper.getRecipes());
        registry.addRecipes(AnalyzerRecipeWrapper.getRecipes());
    }

    private void addContainerRecipes(@Nonnull IModRegistry registry)
    {
        ItemStack orb = new ItemStack(ElementsOfPower.magicOrb, 1, OreDictionary.WILDCARD_VALUE);
        ItemStack gemstone = new ItemStack(ElementsOfPower.gemstone, 1, OreDictionary.WILDCARD_VALUE);
        for (ItemStack stack : Arrays.asList(
                new ItemStack(ElementsOfPower.magicWand),
                new ItemStack(ElementsOfPower.magicStaff),
                new ItemStack(ElementsOfPower.magicRing)
        ))
        {
            registry.addRecipes(
                    Arrays.asList(
                            new GemstoneChangeRecipeWrapper(stack, stack, gemstone),
                            new ContainerChargeRecipeWrapper(stack, stack, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb, orb, orb),
                            new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb, orb, orb, orb)
                    ));
        }
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime)
    {
    }
}
