package gigaherz.elementsofpower.recipes;

import gigaherz.elementsofpower.gemstones.GemstoneItem;
import gigaherz.elementsofpower.items.GemContainerItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

public class GemstoneChangeRecipe extends SpecialRecipe
{
    public GemstoneChangeRecipe(ResourceLocation idIn)
    {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
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
    public ItemStack getCraftingResult(CraftingInventory inv)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        ItemStack gem = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
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
    public boolean canFit(int width, int height)
    {
        return (width * height) >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
    {
        NonNullList<ItemStack> arr = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        GemContainerItem gemContainerItem = null;

        ItemStack gemContainer = null;

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack current = inv.getStackInSlot(i);
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
    public IRecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    @ObjectHolder("elementsofpower:gemstone_change")
    public static SpecialRecipeSerializer<GemstoneChangeRecipe> SERIALIZER;
}
