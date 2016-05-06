package gigaherz.elementsofpower.integration;

import gigaherz.elementsofpower.ElementsOfPower;
import mezz.jei.JeiHelpers;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nonnull;

public class TestCategory implements IRecipeCategory
{
    public static final String UID = ElementsOfPower.MODID + "_ContainerCharge";

    private final IDrawable background;

    public TestCategory(IJeiHelpers helpers)
    {
        background = helpers.getGuiHelper().createDrawable(new ResourceLocation(ElementsOfPower.MODID, "gui/jei/container.png"), 3, 4, 155, 65);
    }

    @Nonnull
    @Override
    public String getUid()
    {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return I18n.translateToLocal("text." + ElementsOfPower.MODID + ".jei.container");
    }

    @Nonnull
    @Override
    public IDrawable getBackground()
    {
        return background;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft)
    {

    }

    @Override
    public void drawAnimations(@Nonnull Minecraft minecraft)
    {

    }

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    @SuppressWarnings("unchecked")
    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper)
    {
        recipeLayout.getItemStacks().init(INPUT_SLOT, true, 31, 0);
        recipeLayout.getItemStacks().init(OUTPUT_SLOT, false, 125, 30);

        if (recipeWrapper instanceof ContainerChargeRecipeWrapper)
        {
            ContainerChargeRecipeWrapper altarRecipeWrapper = (ContainerChargeRecipeWrapper) recipeWrapper;
            recipeLayout.getItemStacks().set(INPUT_SLOT, altarRecipeWrapper.getInputs());
            recipeLayout.getItemStacks().set(OUTPUT_SLOT, altarRecipeWrapper.getOutputs());
        }
    }
}
