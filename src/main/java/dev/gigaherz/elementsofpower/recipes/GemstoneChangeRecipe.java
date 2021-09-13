package dev.gigaherz.elementsofpower.recipes;

import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.items.GemContainerItem;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;

public class GemstoneChangeRecipe extends CustomRecipe
{
    public GemstoneChangeRecipe(ResourceLocation idIn)
    {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack current = inv.getItem(i);
            if (current.getCount() <= 0)
                continue;
            Item item = current.getItem();
            if (item instanceof GemContainerItem)
            {
                if (gemContainer.getCount() > 0)
                    return false;
                gemContainer = current;
            }
            else if (item instanceof GemstoneItem)
            {
                if (gem.getCount() > 0)
                    return false;
                gem = current;
            }
            else
            {
                return false;
            }
        }
        return gemContainer.getCount() > 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack current = inv.getItem(i);
            if (current.getCount() <= 0)
                continue;
            Item item = current.getItem();
            if (item instanceof GemContainerItem)
            {
                if (gemContainer.getCount() > 0)
                    return ItemStack.EMPTY;
                gemContainer = current;
            }
            else if (item instanceof GemstoneItem)
            {
                if (gem.getCount() > 0)
                    return ItemStack.EMPTY;
                gem = current;
            }
            else
            {
                return ItemStack.EMPTY;
            }
        }

        if (gemContainer.getCount() <= 0)
        {
            return ItemStack.EMPTY;
        }

        return ((GemContainerItem) gemContainer.getItem()).setContainedGemstone(gemContainer.copy(), gem);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return (width * height) >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv)
    {
        NonNullList<ItemStack> arr = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        GemContainerItem gemContainerItem = null;

        ItemStack gemContainer = null;

        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack current = inv.getItem(i);
            Item item = current.getItem();
            if (item instanceof GemContainerItem)
            {
                if (gemContainer != null)
                    return arr;
                gemContainer = current;
                gemContainerItem = (GemContainerItem) item;
            }
        }

        if (gemContainer != null)
        {
            arr.set(0, gemContainerItem.getContainedGemstone(gemContainer));
            return arr;
        }

        return arr;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    @ObjectHolder("elementsofpower:gemstone_change")
    public static SimpleRecipeSerializer<GemstoneChangeRecipe> SERIALIZER;
}
