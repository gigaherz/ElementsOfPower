package dev.gigaherz.elementsofpower.essentializer.menu;

import dev.gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class MagicSourceSlot extends SlotItemHandler
{
    private final Level level;

    public MagicSourceSlot(Level level, IItemHandler inventory, int par2, int par3, int par4)
    {
        super(inventory, par2, par3, par4);
        this.level = level;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return stack == null || (ModList.get().isLoaded("aequivaleo") && AequivaleoPlugin.getEssences(level, stack, false).isPresent());
    }

    @Override
    public int getMaxStackSize()
    {
        return 64;
    }
}
