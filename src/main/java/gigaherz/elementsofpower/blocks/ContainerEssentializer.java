package gigaherz.elementsofpower.blocks;

import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.slots.SlotContainer;
import gigaherz.elementsofpower.slots.SlotMagic;
import gigaherz.elementsofpower.slots.SlotSource;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEssentializer
        extends Container {
    protected TileEssentializer tile;

    public ContainerEssentializer(TileEssentializer tileEntity, InventoryPlayer playerInventory) {
        this.tile = tileEntity;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                addSlotToContainer(new SlotMagic(tileEntity,
                        j + i * 2,
                        71 + j * 18, 8 + i * 18));
            }
        }

        BlockWorkbench wb;

        addSlotToContainer(new SlotSource(tileEntity, 8, 26, 35));
        addSlotToContainer(new SlotContainer(tileEntity, 9, 134, 35));
        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(InventoryPlayer playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(playerInventory,
                        j + i * 9 + 9,
                        8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    /*@Override
    public void onCraftGuiClosed(EntityPlayer player)
    {
        super.onCraftGuiClosed(player);

        if (!this.tile.worldObj.isRemote)
        {
            for (int i = 8; i < 10; i++)
            {
                ItemStack stack = this.tile.getStackInSlotOnClosing(i);

                if (stack != null)
                {
                    player.dropPlayerItem(stack);
                }
            }
        }
    }*/

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        if (slotIndex < 8) {
            return null;
        }

        ItemStack stackCopy = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);

        if (slot == null || !slot.getHasStack()) {
            return null;
        }

        ItemStack stack = slot.getStack();
        stackCopy = stack.copy();

        if (slotIndex >= 10) {
            boolean itemIsContainer = MagicDatabase.canItemContainMagic(stack);
            boolean itemContainsMagic = itemIsContainer && MagicDatabase.itemContainsMagic(stack);
            boolean itemHasEssence = MagicDatabase.itemHasEssence(stack);

            if (itemIsContainer) {
                if (itemContainsMagic) {
                    if (!this.mergeItemStack(stack, 8, 9, false)) {
                        ItemStack dest = ((Slot) this.inventorySlots.get(9)).getStack();

                        if (dest != null && dest.stackSize > 0) {
                            return null;
                        }

                        if (!this.mergeItemStack(stack, 9, 10, false)) {
                            return null;
                        }
                    }
                } else {
                    ItemStack dest = ((Slot) this.inventorySlots.get(9)).getStack();

                    if (dest != null && dest.stackSize > 0) {
                        if (!itemHasEssence) {
                            return null;
                        }

                        if (!this.mergeItemStack(stack, 8, 9, false)) {
                            return null;
                        }
                    } else if (!this.mergeItemStack(stack, 9, 10, false)) {
                        if (!itemHasEssence) {
                            return null;
                        }

                        if (!this.mergeItemStack(stack, 8, 9, false)) {
                            return null;
                        }
                    }
                }
            } else if (itemHasEssence) {
                if (!this.mergeItemStack(stack, 8, 9, false)) {
                    return null;
                }
            } else if (slotIndex >= 10 && slotIndex < 37) {
                if (!this.mergeItemStack(stack, 37, 46, false)) {
                    return null;
                }
            } else if (slotIndex >= 37 && slotIndex < 39) {
                if (!this.mergeItemStack(stack, 10, 37, false)) {
                    return null;
                }
            }
        } else {
            if (!this.mergeItemStack(stack, 10, 46, false)) {
                return null;
            }
        }

        if (stack.stackSize == 0) {
            slot.putStack((ItemStack) null);
        } else {
            slot.onSlotChanged();
        }

        if (stack.stackSize == stackCopy.stackSize) {
            return null;
        }

        slot.onPickupFromSlot(player, stack);
        return stackCopy;
    }

    public void clickedMagic(Slot slot, int button) {
        //ElementsOfPower.proxy.sendProgressBarUpdate(tile, button, slot.slotNumber);
    }
}
