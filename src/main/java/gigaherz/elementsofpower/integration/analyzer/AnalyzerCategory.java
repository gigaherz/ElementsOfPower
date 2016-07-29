package gigaherz.elementsofpower.integration.analyzer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.analyzer.gui.GuiAnalyzer;
import gigaherz.elementsofpower.gemstones.Quality;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.List;

public class AnalyzerCategory implements IRecipeCategory<AnalyzerRecipeWrapper>
{
    public static final String UID = ElementsOfPower.MODID + "_analyzer";

    public static AnalyzerCategory INSTANCE;

    String[] qualityChances = {
            "70%", "20%", "9%", "0.9%", "0.1%"
    };

    @Nonnull
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
        return I18n.format("text." + ElementsOfPower.MODID + ".jei.category.analyzer");
    }

    @Nonnull
    @Override
    public IDrawable getBackground()
    {
        return background;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft mc)
    {
        for (int ord = 0; ord < Quality.values.length; ord++)
        {
            mc.fontRendererObj.drawString(Quality.values[ord].name(),
                    32 + 64 * (ord % 2) + 10,
                    17 + 21 * (ord / 2) - 14, 0xFFFFFFFF, false);
            mc.fontRendererObj.drawString(qualityChances[ord],
                    32 + 64 * (ord % 2) + 10,
                    17 + 21 * (ord / 2) - 4, 0xFFFFFFFF, false);
        }
    }

    @Override
    public void drawAnimations(@Nonnull Minecraft minecraft)
    {
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull AnalyzerRecipeWrapper recipeWrapper)
    {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(0, true, 0, 0);

        for (int ord = 0; ord < Quality.values.length; ord++)
        {
            itemStacks.init(ord + 1, false,
                    32 + 64 * (ord % 2) - 8,
                    17 + 21 * (ord / 2) - 16);
        }

        List inputs = recipeWrapper.getInputs();
        List outputs = recipeWrapper.getOutputs();

        itemStacks.setFromRecipe(0, inputs.get(0));
        itemStacks.setFromRecipe(1, outputs.get(0));
        itemStacks.setFromRecipe(2, outputs.get(1));
        itemStacks.setFromRecipe(3, outputs.get(2));
        itemStacks.setFromRecipe(4, outputs.get(3));
        itemStacks.setFromRecipe(5, outputs.get(4));
    }
}
