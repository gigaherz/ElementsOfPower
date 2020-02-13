package gigaherz.elementsofpower.recipes;

import com.google.gson.JsonObject;
import gigaherz.elementsofpower.gemstones.GemstoneItem;
import gigaherz.elementsofpower.items.GemContainerItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class GemstoneChangeRecipe implements ICraftingRecipe
{
    private final ResourceLocation id;

    public GemstoneChangeRecipe(ResourceLocation id)
    {
        this.id = id;
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
    public ItemStack getRecipeOutput()
    {
        return ItemStack.EMPTY;
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
    public boolean isDynamic()
    {
        return true;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return Serializer.INSTANCE;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<GemstoneChangeRecipe>
    {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public GemstoneChangeRecipe read(ResourceLocation recipeId, JsonObject json)
        {
            return new GemstoneChangeRecipe(recipeId);
        }

        @Nullable
        @Override
        public GemstoneChangeRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
        {
            return new GemstoneChangeRecipe(recipeId);
        }

        @Override
        public void write(PacketBuffer buffer, GemstoneChangeRecipe recipe)
        {

        }
    }
}
