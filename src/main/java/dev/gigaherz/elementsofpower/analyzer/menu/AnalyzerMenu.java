package dev.gigaherz.elementsofpower.analyzer.menu;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.database.GemstoneExaminer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkHooks;

public class AnalyzerMenu extends AbstractContainerMenu
{
    public InventoryInternal internalInventory;
    public Player player;
    public Inventory playerInventory;
    public int slotNumber;

    public AnalyzerMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        this(id, playerInventory, extraData.readInt());
    }

    public AnalyzerMenu(int id, Inventory playerInventory, int slotNumber)
    {
        super(ElementsOfPowerMod.ANALYZER_MENU.get(), id);
        this.internalInventory = new InventoryInternal();
        this.playerInventory = playerInventory;
        this.player = playerInventory.player;
        this.slotNumber = slotNumber;

        addSlot(new AnalyzerInputSlot(internalInventory, 0, 8, 16)
        {
            @Override
            public void onQuickCraft(ItemStack p_75220_1_, ItemStack p_75220_2_)
            {
                super.onQuickCraft(p_75220_1_, p_75220_2_);
            }
        });

        bindPlayerInventory(playerInventory);
    }

    public static void openAnalyzer(ServerPlayer serverPlayer, int slot)
    {
        NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((id, playerInventory, player) -> new AnalyzerMenu(id, playerInventory, slot),
                        Component.translatable("container.elementsofpower.analyzer")),
                (packetBuffer) -> packetBuffer.writeInt(slot));
    }

    protected void bindPlayerInventory(Inventory playerInventory)
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
    public boolean stillValid(Player playerIn)
    {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
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

        if (!this.moveItemStackTo(stack, startIndex, endIndex, false))
        {
            return ItemStack.EMPTY;
        }

        if (stack.getCount() == 0)
        {
            slot.set(ItemStack.EMPTY);
        }
        else
        {
            slot.setChanged();
        }

        if (stack.getCount() == stackCopy.getCount())
        {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return stackCopy;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection)
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
                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                int limit = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());

                if (itemstack.getCount() > 0 && ItemStack.isSameItemSameTags(stack, itemstack))
                {
                    int j = itemstack.getCount() + stack.getCount();

                    if (j <= limit)
                    {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    }
                    else if (itemstack.getCount() < limit)
                    {
                        stack.shrink(limit - itemstack.getCount());
                        itemstack.setCount(limit);
                        slot.setChanged();
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
                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                int limit = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());

                if (itemstack.getCount() <= 0 && slot.mayPlace(stack)) // Forge: Make sure to respect isItemValid in the slot.
                {
                    ItemStack put = stack.copy();
                    put.setCount(Math.min(put.getCount(), limit));
                    slot.set(put);
                    slot.setChanged();
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
    public void broadcastChanges()
    {
        Slot s = slots.get(0);
        ItemStack stack = s.getItem();
        if (stack.getCount() > 0 && !player.level().isClientSide)
        {
            ItemStack stack2 = GemstoneExaminer.identifyQuality(stack);

            if (!ItemStack.matches(stack, stack2))
            {
                internalInventory.setItem(0, stack2);
                this.lastSlots.set(0, stack2);

                for (ContainerListener listener : this.containerListeners)
                {
                    listener.slotChanged(this, 0, stack2);
                }
            }
        }

        super.broadcastChanges();
    }

    @Override
    public void removed(Player playerIn)
    {
        super.removed(playerIn);

        if (!player.level().isClientSide)
        {
            ItemStack itemstack = internalInventory.removeItemNoUpdate(0);
            if (itemstack.getCount() > 0)
            {
                if (!playerIn.isAlive() || playerIn instanceof ServerPlayer && ((ServerPlayer) playerIn).hasDisconnected())
                {
                    playerIn.drop(itemstack, false);
                }
                else
                {
                    playerIn.getInventory().placeItemBackInInventory(itemstack);
                }
            }
        }
    }


    static class InventoryInternal extends SimpleContainer
    {
        public InventoryInternal()
        {
            super(1);
        }

        @Override
        public int getMaxStackSize()
        {
            return 1;
        }
    }
}
