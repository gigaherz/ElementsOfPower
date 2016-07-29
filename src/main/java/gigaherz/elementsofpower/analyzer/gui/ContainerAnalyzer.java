package gigaherz.elementsofpower.analyzer.gui;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.ContainerInformation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAnalyzer extends Container
{
    public InventoryInternal internalInventory;
    public EntityPlayer player;
    public InventoryPlayer playerInventory;
    public int slotNumber;

    public ContainerAnalyzer(EntityPlayer player)
    {
        this.internalInventory = new InventoryInternal();
        this.player = player;
        this.playerInventory = player.inventory;
        this.slotNumber = playerInventory.currentItem;

        addSlotToContainer(new SlotAnalyzerIn(internalInventory, 0, 8, 16));

        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(InventoryPlayer playerInventory)
    {
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                int slot = j + i * 9 + 9;
                int x = 8 + j * 18;
                int y = 94 + i * 18;
                if (slot == slotNumber)
                    addSlotToContainer(new SlotReadonly(playerInventory, slot, x, y));
                else
                    addSlotToContainer(new Slot(playerInventory, slot, x, y));
            }
        }

        for (int i = 0; i < 9; i++)
        {
            int x = 8 + i * 18;
            if (i == slotNumber)
                addSlotToContainer(new SlotReadonly(playerInventory, i, x, 152));
            else
                addSlotToContainer(new Slot(playerInventory, i, x, 152));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        Slot slot = this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack())
        {
            return null;
        }

        ItemStack stack = slot.getStack();
        assert stack != null;
        ItemStack stackCopy = stack.copy();

        int startIndex;
        int endIndex;

        if (slotIndex > 0)
        {
            startIndex = 0;
            endIndex = startIndex + 1;
        }
        else
        {
            startIndex = 1;
            endIndex = startIndex + 9 * 4;
        }

        if (!this.mergeItemStack(stack, startIndex, endIndex, false))
        {
            return null;
        }

        if (stack.stackSize == 0)
        {
            slot.putStack(null);
        }
        else
        {
            slot.onSlotChanged();
        }

        if (stack.stackSize == stackCopy.stackSize)
        {
            return null;
        }

        slot.onPickupFromSlot(player, stack);
        return stackCopy;
    }

    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection)
    {
        boolean flag = false;
        int i = startIndex;

        if (reverseDirection)
        {
            i = endIndex - 1;
        }

        if (stack.isStackable())
        {
            while (stack.stackSize > 0 && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex))
            {
                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();
                int limit = Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize());

                if (itemstack != null && itemstack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack))
                {
                    int j = itemstack.stackSize + stack.stackSize;

                    if (j <= limit)
                    {
                        stack.stackSize = 0;
                        itemstack.stackSize = j;
                        slot.onSlotChanged();
                        flag = true;
                    }
                    else if (itemstack.stackSize < limit)
                    {
                        stack.stackSize -= limit - itemstack.stackSize;
                        itemstack.stackSize = limit;
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        if (stack.stackSize > 0)
        {
            if (reverseDirection)
            {
                i = endIndex - 1;
            }
            else
            {
                i = startIndex;
            }

            while (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex)
            {
                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();
                int limit = Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize());

                if (itemstack == null && slot.isItemValid(stack)) // Forge: Make sure to respect isItemValid in the slot.
                {
                    ItemStack put = stack.copy();
                    put.stackSize = Math.min(put.stackSize, limit);
                    slot.putStack(put);
                    slot.onSlotChanged();
                    stack.stackSize -= put.stackSize;
                    flag = true;
                    break;
                }

                if (reverseDirection)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Override
    public void detectAndSendChanges()
    {
        Slot s = inventorySlots.get(0);
        ItemStack stack = s.getStack();
        if (stack != null && !player.worldObj.isRemote)
        {
            ItemStack stack2 = ContainerInformation.identifyQuality(stack);

            if (!ItemStack.areItemStacksEqual(stack, stack2))
            {
                internalInventory.setInventorySlotContents(0, stack2);
                this.inventoryItemStacks.set(0, stack2);

                for (IContainerListener listener : this.listeners)
                {
                    boolean prev = false;
                    EntityPlayerMP p = null;
                    if (listener instanceof EntityPlayerMP)
                    {
                        p = (EntityPlayerMP) listener;
                        prev = p.isChangingQuantityOnly;
                        p.isChangingQuantityOnly = false;
                    }

                    //noinspection ConstantConditions
                    listener.sendSlotContents(this, 0, stack2);

                    if (prev)
                    {
                        p.isChangingQuantityOnly = true;
                    }
                }
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack)
    {
        ElementsOfPower.logger.warn("putStackInSlot " + stack + " client=" + player.worldObj.isRemote);
        super.putStackInSlot(slotID, stack);
    }

    @Override
    public void putStacksInSlots(ItemStack[] stacks)
    {
        ElementsOfPower.logger.warn("putStacksInSlots " + stacks[0] + " client=" + player.worldObj.isRemote);
        super.putStacksInSlots(stacks);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!player.worldObj.isRemote)
        {
            ItemStack itemstack = internalInventory.removeStackFromSlot(0);
            if (itemstack != null)
            {
                playerIn.dropItem(itemstack, false);
            }
        }
    }

    class InventoryInternal extends InventoryBasic
    {
        public InventoryInternal()
        {
            super("analyzer", false, 1);
        }

        @Override
        public int getInventoryStackLimit()
        {
            return 1;
        }
    }
}
