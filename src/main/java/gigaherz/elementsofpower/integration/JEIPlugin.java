package gigaherz.elementsofpower.integration;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.integration.analyzer.AnalyzerCategory;
import gigaherz.elementsofpower.integration.analyzer.AnalyzerRecipeWrapper;
import gigaherz.elementsofpower.integration.essentializer.EssentializerCategory;
import gigaherz.elementsofpower.integration.essentializer.EssentializerRecipeWrapper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin
{
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
    {

    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry)
    {

    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        registry.addRecipeCategories(
                new EssentializerCategory(registry.getJeiHelpers().getGuiHelper()),
                new AnalyzerCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        registry.handleRecipes(ContainerChargeRecipeWrapper.class, (w) -> w, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(GemstoneChangeRecipeWrapper.class, (w) -> w, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(EssentializerRecipeWrapper.class, (w) -> w, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(AnalyzerRecipeWrapper.class, (w) -> w, VanillaRecipeCategoryUid.CRAFTING);

        registry.addRecipeCatalyst(new ItemStack(ElementsOfPower.essentializer), EssentializerCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ElementsOfPower.analyzer), AnalyzerCategory.UID);

        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new EssentializerCategory.TransferInfo());

        registry.addRecipes(EssentializerRecipeWrapper.getRecipes(), EssentializerCategory.UID);
        registry.addRecipes(AnalyzerRecipeWrapper.getRecipes(), AnalyzerCategory.UID);

        addContainerRecipes(registry);
    }

    private void addContainerRecipes(@Nonnull IModRegistry registry)
    {
        ItemStack orb = new ItemStack(ElementsOfPower.orb, 1, OreDictionary.WILDCARD_VALUE);
        ItemStack gemstone = new ItemStack(ElementsOfPower.gemstone, 1, OreDictionary.WILDCARD_VALUE);
        for (ItemStack stack : Arrays.asList(
                new ItemStack(ElementsOfPower.wand),
                new ItemStack(ElementsOfPower.staff),
                new ItemStack(ElementsOfPower.ring)
        ))
        {
            registry.addRecipes(Arrays.asList(
                    new GemstoneChangeRecipeWrapper(stack, stack, gemstone),
                    new ContainerChargeRecipeWrapper(stack, stack, orb),
                    new ContainerChargeRecipeWrapper(stack, stack, orb, orb),
                    new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb),
                    new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb),
                    new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb),
                    new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb, orb),
                    new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb, orb, orb),
                    new ContainerChargeRecipeWrapper(stack, stack, orb, orb, orb, orb, orb, orb, orb, orb)
            ), VanillaRecipeCategoryUid.CRAFTING);
        }
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime)
    {
    }
}
