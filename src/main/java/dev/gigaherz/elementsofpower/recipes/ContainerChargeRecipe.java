package dev.gigaherz.elementsofpower.recipes;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.items.MagicContainerItem;
import dev.gigaherz.elementsofpower.items.MagicOrbItem;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;


public class ContainerChargeRecipe extends CustomRecipe
{
    public ContainerChargeRecipe(CraftingBookCategory craftingBookCategory)
    {
        super(craftingBookCategory);
    }

    private interface ProcessAction
    {
        ItemStack processStack(ItemStack stack, MagicContainerItem item, MagicAmounts capacity, MagicAmounts contained, MagicAmounts charge, MagicAmounts result);
    }

    private ItemStack processRecipe(CraftingContainer inv, ProcessAction modify)
    {
        ItemStack gemContainer = ItemStack.EMPTY;
        MagicAmounts charge = MagicAmounts.EMPTY;

        MagicContainerItem gemContainerItem = null;

        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack current = inv.getItem(i);
            if (current.getCount() <= 0)
                continue;
            Item item = current.getItem();
            if (item instanceof MagicContainerItem item1)
            {
                if (gemContainer.getCount() > 0)
                    return ItemStack.EMPTY;
                gemContainer = current;
                gemContainerItem = item1;
            }
            else if (item instanceof MagicOrbItem item1)
            {
                charge = charge.add(item1.getMagicCharge());
            }
            else
            {
                return ItemStack.EMPTY;
            }
        }

        if (gemContainer.getCount() > 0 && charge.isPositive() && gemContainerItem != null)
        {
            var capacity = gemContainerItem.getCapacity(gemContainer);
            var contained = gemContainerItem.getContainedMagic(gemContainer);
            var newContained = contained.add(charge);

            if (newContained.allLessThan(capacity))
            {
                return modify.processStack(gemContainer, gemContainerItem, capacity, contained, charge, newContained);
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn)
    {
        return processRecipe(inv, (stack, item, capacity, contained, charge, newContained) -> stack).getCount() > 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess)
    {
        return processRecipe(inv, (stack, item, capacity, contained, charge, newContained) -> {
            var output = stack.copy();
            item.setContainedMagic(output, newContained);
            return output;
        });
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
        return ElementsOfPowerMod.CONTAINER_CHARGE.get();
    }

}
