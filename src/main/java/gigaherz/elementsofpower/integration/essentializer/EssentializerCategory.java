package gigaherz.elementsofpower.integration.essentializer;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.renderers.StackRenderingHelper;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.essentializer.gui.ContainerEssentializer;
import gigaherz.elementsofpower.essentializer.gui.GuiEssentializer;
import gigaherz.elementsofpower.gemstones.Element;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class EssentializerCategory implements IRecipeCategory<EssentializerRecipeWrapper>
{
    public static final String UID = ElementsOfPower.MODID + "_essentializer";

    public static EssentializerCategory INSTANCE;

    @Nonnull
    private final IDrawable background;

    MagicAmounts essenceAmounts;

    public EssentializerCategory(IGuiHelper guiHelper)
    {
        INSTANCE = this;
        background = guiHelper.createDrawable(GuiEssentializer.GUI_TEXTURE_LOCATION, 7, 15, 162, 74, 0, 0, 0, 0);
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
        return I18n.format("text." + ElementsOfPower.MODID + ".jei.category.essentializer");
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

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull EssentializerRecipeWrapper recipeWrapper)
    {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(0, true, 72, 28);
        for (int ord = 0; ord < Element.values.length; ord++)
        {
            itemStacks.init(ord + 1, false,
                    GuiEssentializer.MAGIC_ORBS[ord * 2] - 8,
                    GuiEssentializer.MAGIC_ORBS[ord * 2 + 1] - 16);
        }

        recipeLayout.setRecipeTransferButton(background.getWidth() - 12, background.getHeight() - 12);

        List inputs = recipeWrapper.getInputs();
        essenceAmounts = recipeWrapper.getEssences();

        itemStacks.setFromRecipe(0, inputs.get(0));
    }

    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        for (int ord = 0; ord < Element.values.length; ord++)
        {
            int x = GuiEssentializer.MAGIC_ORBS[ord * 2] - 8;
            int y = GuiEssentializer.MAGIC_ORBS[ord * 2 + 1] - 16;
            if (mouseX >= x && mouseX < (x + 16) &&
                    mouseY >= y && mouseY < (y + 16))
                return Collections.singletonList(MagicAmounts.getMagicName(ord));
        }
        return null;
    }

    public void drawEssenceSlots(Minecraft mc)
    {
        if (essenceAmounts == null)
            return;

        ItemModelMesher mesher = mc.getRenderItem().getItemModelMesher();

        GlStateManager.disableDepth();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float am = essenceAmounts.amounts[i];

            int alpha = am > 0 ? 0xFFFFFFFF : 0x3FFFFFFF;

            int x0 = GuiEssentializer.MAGIC_ORBS[i * 2] - 7;
            int y0 = GuiEssentializer.MAGIC_ORBS[i * 2 + 1] - 15;

            ItemStack stack = ElementsOfPower.magicOrb.getStack(1, Element.values[i]);

            StackRenderingHelper.renderItemStack(mesher, mc.renderEngine, x0, y0, stack, alpha);
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(1 / 1.5, 1 / 1.5, 1);
        GlStateManager.translate(0, 0, 150);
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float am = essenceAmounts.amounts[i];

            int x0 = GuiEssentializer.MAGIC_ORBS[i * 2] - 7;
            int y0 = GuiEssentializer.MAGIC_ORBS[i * 2 + 1] - 15;

            float count = am;
            String suffix = "";
            if (count >= 900)
            {
                suffix = "k";
                count /= 1000;
            }

            String formatted = ElementsOfPower.prettyNumberFormatter.format(count) + suffix;

            float x1 = (x0 + 16) * 1.5f - mc.fontRendererObj.getStringWidth(formatted);
            float y1 = (y0 + 10.5f) * 1.5f;

            mc.fontRendererObj.drawString(formatted, x1, y1, 0xFFFFFFFF, true);
        }
        GlStateManager.popMatrix();

        GlStateManager.enableDepth();
    }

    public static class TransferInfo implements IRecipeTransferInfo
    {
        @Override
        public Class<? extends Container> getContainerClass()
        {
            return ContainerEssentializer.class;
        }

        @Override
        public String getRecipeCategoryUid()
        {
            return UID;
        }

        @Override
        public List<Slot> getRecipeSlots(Container container)
        {
            return Collections.singletonList(container.getSlot(0));
        }

        @Override
        public List<Slot> getInventorySlots(Container container)
        {
            List<Slot> l = Lists.newArrayList();
            for (int i = 3; i < (3 + 4 * 9); i++)
            { l.add(container.getSlot(i)); }
            return l;
        }
    }
}
