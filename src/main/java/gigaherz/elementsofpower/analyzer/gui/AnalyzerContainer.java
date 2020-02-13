package gigaherz.elementsofpower.analyzer.gui;

import gigaherz.elementsofpower.database.GemstoneExaminer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;

public class AnalyzerContainer extends Container
{
    @ObjectHolder("elementsofpower:analyzer")
    public static ContainerType<AnalyzerContainer> TYPE;

    public InventoryInternal internalInventory;
    public PlayerEntity player;
    public PlayerInventory playerInventory;
    public int slotNumber;

    public AnalyzerContainer(int id, PlayerInventory playerInventory, PacketBuffer extraData)
    {
        this(id, playerInventory, extraData.readInt());
    }

    public AnalyzerContainer(int id, PlayerInventory playerInventory, int slotNumber)
    {
        super(TYPE, id);
        this.internalInventory = new InventoryInternal();
        this.playerInventory = playerInventory;
        this.player = playerInventory.player;
        this.slotNumber = slotNumber;

        addSlot(new AnalyzerInputSlot(internalInventory, 0, 8, 16));

        bindPlayerInventory(playerInventory);
    }


    protected void bindPlayerInventory(PlayerInventory playerInventory)
    {
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                int slot = j + i * 9 + 9;
                int x = 8 + j * 18;
                int y = 94 + i * 18;
                if (slot == slotNumber)
                    addSlot(new ReadonlySlot(playerInventory, slot, x, y));
                else
                    addSlot(new Slot(playerInventory, slot, x, y));
            }
        }

        for (int i = 0; i < 9; i++)
        {
            int x = 8 + i * 18;
            if (i == slotNumber)
                addSlot(new ReadonlySlot(playerInventory, i, x, 152));
            else
                addSlot(new Slot(playerInventory, i, x, 152));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotIndex)
    {
        Slot slot = this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        assert stack.getCount() > 0;
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
            return ItemStack.EMPTY;
        }

        if (stack.getCount() == 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }
        else
        {
            slot.onSlotChanged();
        }

        if (stack.getCount() == stackCopy.getCount())
        {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
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
            while (stack.getCount() > 0 && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex))
            {
                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();
                int limit = Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize());

                if (itemstack.getCount() > 0 && itemstack.getItem() == stack.getItem() && ItemStack.areItemStackTagsEqual(stack, itemstack))
                {
                    int j = itemstack.getCount() + stack.getCount();

                    if (j <= limit)
                    {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.onSlotChanged();
                        flag = true;
                    }
                    else if (itemstack.getCount() < limit)
                    {
                        stack.shrink(limit - itemstack.getCount());
                        itemstack.setCount(limit);
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

        if (stack.getCount() > 0)
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

                if (itemstack.getCount() <= 0 && slot.isItemValid(stack)) // Forge: Make sure to respect isItemValid in the slot.
                {
                    ItemStack put = stack.copy();
                    put.setCount(Math.min(put.getCount(), limit));
                    slot.putStack(put);
                    slot.onSlotChanged();
                    stack.shrink(put.getCount());
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
        if (stack.getCount() > 0 && !player.world.isRemote)
        {
            ItemStack stack2 = GemstoneExaminer.identifyQuality(stack);

            if (!ItemStack.areItemStacksEqual(stack, stack2))
            {
                internalInventory.setInventorySlotContents(0, stack2);
                this.inventoryItemStacks.set(0, stack2);

                for (IContainerListener listener : this.listeners)
                {
                    boolean prev = false;
                    ServerPlayerEntity p = null;
                    if (listener instanceof ServerPlayerEntity)
                    {
                        p = (ServerPlayerEntity) listener;
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
        super.putStackInSlot(slotID, stack);
    }


    @Override
    public void setAll(List<ItemStack> stacks)
    {
        super.setAll(stacks);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!player.world.isRemote)
        {
            ItemStack itemstack = internalInventory.removeStackFromSlot(0);
            if (itemstack.getCount() > 0)
            {
                playerIn.dropItem(itemstack, false);
            }
        }
    }

    class InventoryInternal extends Inventory
    {
        public InventoryInternal()
        {
            super(1);
        }

        @Override
        public int getInventoryStackLimit()
        {
            return 1;
        }
    }
}
