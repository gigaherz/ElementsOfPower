package gigaherz.elementsofpower.integration.analyzer;
/*
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.analyzer.gui.GuiAnalyzer;
import gigaherz.elementsofpower.gemstones.Quality;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class AnalyzerCategory implements IRecipeCategory<AnalyzerRecipeWrapper>
{
    public static final String UID = ElementsOfPowerMod.MODID + "_analyzer";

    public static AnalyzerCategory INSTANCE;

    String[] qualityChances = {
            "70%", "20%", "9%", "0.9%", "0.1%"
    };

    private final IDrawable background;

    public AnalyzerCategory(IGuiHelper guiHelper)
    {
        INSTANCE = this;
        background = guiHelper.createDrawable(GuiAnalyzer.GUI_TEXTURE_LOCATION, 7, 15, 162, 66, 0, 0, 0, 0);
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
        return I18n.format("text." + ElementsOfPowerMod.MODID + ".jei.category.analyzer");
    }

    @Override
    public String getModName()
    {
        return "Elements of Power";
    }

    @Nonnull
    @Override
    public IDrawable getBackground()
    {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon()
    {
        return null;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft mc)
    {
        for (int ord = 0; ord < Quality.values.length; ord++)
        {
            mc.fontRenderer.drawString(Quality.values[ord].name(),
                    32 + 64 * (ord % 2) + 10,
                    17 + 21 * (ord / 2) - 14, 0xFFFFFFFF, false);
            mc.fontRenderer.drawString(qualityChances[ord],
                    32 + 64 * (ord % 2) + 10,
                    17 + 21 * (ord / 2) - 4, 0xFFFFFFFF, false);
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, AnalyzerRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(0, true, 0, 0);

        for (int ord = 0; ord < Quality.values.length; ord++)
        {
            itemStacks.init(ord + 1, false,
                    32 + 64 * (ord % 2) - 8,
                    17 + 21 * (ord / 2) - 16);
        }

        /-*
        List inputs = recipeWrapper.getInputs();
        List outputs = recipeWrapper.getOutputs();
        *-/

        recipeWrapper.getIngredients(ingredients);

        /-*
        itemStacks.setFromRecipe(0, inputs.get(0));
        itemStacks.setFromRecipe(1, outputs.get(0));
        itemStacks.setFromRecipe(2, outputs.get(1));
        itemStacks.setFromRecipe(3, outputs.get(2));
        itemStacks.setFromRecipe(4, outputs.get(3));
        i-temStacks.setFromRecipe(5, outputs.get(4));
        *-/
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        return null;
    }
}
*/