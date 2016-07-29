package gigaherz.elementsofpower.recipes;

import gigaherz.elementsofpower.gemstones.ItemGemstone;
import gigaherz.elementsofpower.items.ItemGemContainer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class GemstoneChangeRecipe implements IRecipe
{
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        ItemStack gemContainer = null;
        ItemStack gem = null;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
            if (current == null)
                continue;
            Item item = current.getItem();
            if (item instanceof ItemGemContainer)
            {
                if (gemContainer != null)
                    return false;
                gemContainer = current;
            }
            else if (item instanceof ItemGemstone)
            {
                if (gem != null)
                    return false;
                gem = current;
            }
            else
            {
                return false;
            }
        }
        return gemContainer != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemGemContainer gemContainerItem = null;

        ItemStack gemContainer = null;
        ItemStack gem = null;

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
            if (current == null)
                continue;
            Item item = current.getItem();
            if (item instanceof ItemGemContainer)
            {
                if (gemContainer != null)
                    return null;
                gemContainer = current;
                gemContainerItem = (ItemGemContainer) item;
            }
            else if (item instanceof ItemGemstone)
            {
                if (gem != null)
                    return null;
                gem = current;
            }
            else
            {
                return null;
            }
        }

        if (gemContainer == null)
        {
            return null;
        }

        return gemContainerItem.setContainedGemstone(gemContainer.copy(), gem);
    }

    @Override
    public int getRecipeSize()
    {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return null;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        ItemStack[] arr = new ItemStack[inv.getSizeInventory()];

        ItemGemContainer gemContainerItem = null;

        ItemStack gemContainer = null;

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
            if (current == null)
                continue;
            Item item = current.getItem();
            if (item instanceof ItemGemContainer)
            {
                if (gemContainer != null)
                    return arr;
                gemContainer = current;
                gemContainerItem = (ItemGemContainer) item;
            }
        }

        if (gemContainer != null)
        {
            arr[0] = gemContainerItem.getContainedGemstone(gemContainer);
            return arr;
        }

        return arr;
    }
}
