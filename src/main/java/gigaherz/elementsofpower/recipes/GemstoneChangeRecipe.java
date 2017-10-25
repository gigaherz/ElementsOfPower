package gigaherz.elementsofpower.recipes;

import gigaherz.elementsofpower.gemstones.ItemGemstone;
import gigaherz.elementsofpower.items.ItemGemContainer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class GemstoneChangeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
            if (current.getCount() <= 0)
                continue;
            Item item = current.getItem();
            if (item instanceof ItemGemContainer)
            {
                if (gemContainer.getCount() > 0)
                    return false;
                gemContainer = current;
            }
            else if (item instanceof ItemGemstone)
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
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
            if (current.getCount() <= 0)
                continue;
            Item item = current.getItem();
            if (item instanceof ItemGemContainer)
            {
                if (gemContainer.getCount() > 0)
                    return ItemStack.EMPTY;
                gemContainer = current;
            }
            else if (item instanceof ItemGemstone)
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

        return ((ItemGemContainer) gemContainer.getItem()).setContainedGemstone(gemContainer.copy(), gem);
    }

    @Override
    public boolean canFit(int width, int height)
    {
        return (width * height) >= 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
    {
        NonNullList<ItemStack> arr = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        ItemGemContainer gemContainerItem = null;

        ItemStack gemContainer = null;

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
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
            arr.set(0, gemContainerItem.getContainedGemstone(gemContainer));
            return arr;
        }

        return arr;
    }
}
