package dev.gigaherz.elementsofpower.recipes;

import com.google.common.collect.Lists;
import dev.gigaherz.elementsofpower.items.MagicContainerItem;
import dev.gigaherz.elementsofpower.items.MagicOrbItem;
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

import java.util.List;

public class ContainerChargeRecipe extends CustomRecipe
{
    public ContainerChargeRecipe(ResourceLocation idIn)
    {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        ItemStack orb = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack current = inv.getItem(i);
            if (current.getCount() <= 0)
                continue;
            Item item = current.getItem();
            if (item instanceof MagicContainerItem)
            {
                if (gemContainer.getCount() > 0)
                    return false;
                gemContainer = current;
            }
            else if (item instanceof MagicOrbItem)
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
    public ItemStack assemble(CraftingContainer inv)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        List<ItemStack> orbs = Lists.newArrayList();

        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack current = inv.getItem(i);
            if (current == ItemStack.EMPTY)
                continue;
            Item item = current.getItem();
            if (item instanceof MagicContainerItem)
            {
                if (gemContainer.getCount() > 0)
                    return ItemStack.EMPTY;
                gemContainer = current.copy();
            }
            else if (item instanceof MagicOrbItem)
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

        gemContainer = ((MagicContainerItem) gemContainer.getItem()).addContainedMagic(gemContainer, orbs);
        return gemContainer;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return (width * height) >= 2;
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv)
    {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    @ObjectHolder("elementsofpower:container_charge")
    public static SimpleRecipeSerializer<ContainerChargeRecipe> SERIALIZER;
}
