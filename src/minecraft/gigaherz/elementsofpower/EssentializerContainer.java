package gigaherz.elementsofpower;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EssentializerContainer extends Container
{
    protected EssentializerTile worker;
    protected int lastPowerAcc = 0;
    protected int lastX = 0;
    protected int lastY = 0;

    public EssentializerContainer(EssentializerTile tileEntity, InventoryPlayer playerInventory)
    {
        this.worker = tileEntity;

        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                addSlotToContainer(new Slot(tileEntity,
                        j + i * 3,
                        8 + j * 18,
                        17 + i * 18));
            }
        }

        for (int i = 0; i < 3; i++)
        {
            addSlotToContainer(new Slot(tileEntity,
                    i + 18,
                    68,
                    17 + i * 18));
        }

        for (int i = 0; i < 3; i++)
        {
            addSlotToContainer(new Slot(tileEntity,
                    i + 21,
                    92,
                    17 + i * 18));
        }

        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                addSlotToContainer(new SlotFurnace(playerInventory.player, tileEntity,
                        j + i * 3 + 9,
                        116 + j * 18,
                        17 + i * 18));
            }
        }

        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(InventoryPlayer playerInventory)
    {
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new Slot(playerInventory,
                        j + i * 9 + 9,
                        8 + j * 18,
                        84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return worker.isUseableByPlayer(player);
    }

    public void addCraftingToCrafters(ICrafting crafter)
    {
        super.addCraftingToCrafters(crafter);
        crafter.sendProgressBarUpdate(this, 0, this.worker.powerAccum);
        crafter.sendProgressBarUpdate(this, 1, this.worker.currentX);
        crafter.sendProgressBarUpdate(this, 2, this.worker.currentZ);
    }

    public void updateCraftingResults()
    {
        super.updateCraftingResults();

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting crafter = (ICrafting)this.crafters.get(i);

            if (this.lastPowerAcc != this.worker.powerAccum)
            {
                crafter.sendProgressBarUpdate(this, 0, this.worker.powerAccum);
            }

            if (this.lastX != this.worker.currentX)
            {
                crafter.sendProgressBarUpdate(this, 1, this.worker.currentX);
            }

            if (this.lastY != this.worker.currentZ)
            {
                crafter.sendProgressBarUpdate(this, 2, this.worker.currentZ);
            }
        }

        this.lastPowerAcc = this.worker.powerAccum;
        this.lastX = this.worker.currentX;
        this.lastY = this.worker.currentZ;
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int bar, int value)
    {
        this.worker.updateProgressBar(bar, value);
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
    {
        ItemStack var3 = null;
        Slot var4 = (Slot)this.inventorySlots.get(par2);

        if (var4 != null && var4.getHasStack())
        {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if (par2 == 2)
            {
                if (!this.mergeItemStack(var5, 3, 39, true))
                {
                    return null;
                }

                var4.onSlotChange(var5, var3);
            }
            else if (par2 != 1 && par2 != 0)
            {
                if (FurnaceRecipes.smelting().getSmeltingResult(var5) != null)
                {
                    if (!this.mergeItemStack(var5, 0, 1, false))
                    {
                        return null;
                    }
                }
                else if (TileEntityFurnace.isItemFuel(var5))
                {
                    if (!this.mergeItemStack(var5, 1, 2, false))
                    {
                        return null;
                    }
                }
                else if (par2 >= 3 && par2 < 30)
                {
                    if (!this.mergeItemStack(var5, 30, 39, false))
                    {
                        return null;
                    }
                }
                else if (par2 >= 30 && par2 < 39 && !this.mergeItemStack(var5, 3, 30, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(var5, 3, 39, false))
            {
                return null;
            }

            if (var5.stackSize == 0)
            {
                var4.putStack((ItemStack)null);
            }
            else
            {
                var4.onSlotChanged();
            }

            if (var5.stackSize == var3.stackSize)
            {
                return null;
            }

            var4.onPickupFromSlot(par1EntityPlayer, var5);
        }

        return var3;
    }
}
