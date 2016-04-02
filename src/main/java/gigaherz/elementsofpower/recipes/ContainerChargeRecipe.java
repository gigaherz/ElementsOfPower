package gigaherz.elementsofpower.recipes;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import gigaherz.elementsofpower.items.ItemMagicOrb;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import java.util.List;

public class ContainerChargeRecipe implements IRecipe
{
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        ItemStack gemContainer = null;
        ItemStack orb = null;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
            if (current == null)
                continue;
            Item item = current.getItem();
            if (item instanceof ItemMagicContainer)
            {
                if (gemContainer != null)
                    return false;
                gemContainer = current;
            }
            else if (item instanceof ItemMagicOrb)
            {
                orb = current;
            }
            else
            {
                return false;
            }
        }
        return gemContainer != null && orb != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemMagicContainer gemContainerItem = null;

        ItemStack gemContainer = null;
        List<ItemStack> orbs = Lists.newArrayList();

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
            if (current == null)
                continue;
            Item item = current.getItem();
            if (item instanceof ItemMagicContainer)
            {
                if (gemContainer != null)
                    return null;
                gemContainer = current.copy();
                gemContainerItem = (ItemMagicContainer) item;
            }
            else if (item instanceof ItemMagicOrb)
            {
                orbs.add(current);
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

        for (ItemStack orb : orbs)
        {
            gemContainer = gemContainerItem.addContainedMagic(gemContainer, orb);
            if (gemContainer == null)
                break;
        }

        return gemContainer;
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
        return new ItemStack[inv.getSizeInventory()];
    }
}
