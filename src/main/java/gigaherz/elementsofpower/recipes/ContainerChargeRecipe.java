package gigaherz.elementsofpower.recipes;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import gigaherz.elementsofpower.items.ItemMagicOrb;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;

public class ContainerChargeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        ItemStack orb = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
            if (current.getCount() <= 0)
                continue;
            Item item = current.getItem();
            if (item instanceof ItemMagicContainer)
            {
                if (gemContainer.getCount() > 0)
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
        return gemContainer.getCount() > 0 && orb.getCount() > 0;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        List<ItemStack> orbs = Lists.newArrayList();

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
            if (current == ItemStack.EMPTY)
                continue;
            Item item = current.getItem();
            if (item instanceof ItemMagicContainer)
            {
                if (gemContainer != null)
                    return ItemStack.EMPTY;
                gemContainer = current.copy();
            }
            else if (item instanceof ItemMagicOrb)
            {
                orbs.add(current);
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

        for (ItemStack orb : orbs)
        {
            gemContainer = ((ItemMagicContainer) gemContainer.getItem()).addContainedMagic(gemContainer, orb);
            if (gemContainer.getCount() <= 0)
                break;
        }

        return gemContainer;
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

    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
    {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean isDynamic()
    {
        return true;
    }
}
