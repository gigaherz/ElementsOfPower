package gigaherz.elementsofpower.integration.essentializer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.essentializer.GuiEssentializer;
import gigaherz.elementsofpower.gemstones.Element;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.List;

public class EssentializerCategory implements IRecipeCategory
{
    static final String UID = ElementsOfPower.MODID + "_essentializer";

    @Nonnull
    private final IDrawable background;
    @Nonnull
    private final IDrawable slotDrawable;

    public EssentializerCategory(IGuiHelper guiHelper)
    {
        background = guiHelper.createDrawable(GuiEssentializer.GUI_TEXTURE_LOCATION, 7, 15, 162, 74, 0, 0, 0, 0);

        slotDrawable = guiHelper.getSlotDrawable();
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
        return "Essentializer Breakdown";
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
        //slotDrawable.draw(minecraft, 80, 29);
    }

    @Override
    public void drawAnimations(@Nonnull Minecraft minecraft)
    {
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper)
    {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(0, true, 72, 28);
        for (int ord = 0; ord < Element.values.length; ord++)
        {
            itemStacks.init(ord + 1, false,
                    GuiEssentializer.MAGIC_ORBS[ord * 2] - 8,
                    GuiEssentializer.MAGIC_ORBS[ord * 2 + 1] - 16);
        }

        if (recipeWrapper instanceof EssentializerRecipeWrapper)
        {
            List inputs = recipeWrapper.getInputs();
            List outputs = recipeWrapper.getOutputs();

            itemStacks.setFromRecipe(0, inputs.get(0));
            for (int ord = 0; ord < Element.values.length; ord++)
            {
                itemStacks.setFromRecipe(ord + 1, outputs.get(ord));
            }
        }
    }
}
