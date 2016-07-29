package gigaherz.elementsofpower.essentializer.gui;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEssentializer
        extends Container
{
    protected TileEssentializer tile;
    private MagicAmounts prevContained;
    private MagicAmounts prevRemaining;

    public ContainerEssentializer(TileEssentializer tileEntity, InventoryPlayer playerInventory)
    {
        this.tile = tileEntity;

        addSlotToContainer(new SlotSource(tileEntity, 0, 80, 44));
        addSlotToContainer(new SlotContainerIn(tileEntity, 1, 8, 56));
        addSlotToContainer(new SlotContainerOut(tileEntity, 2, 152, 56));

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
                        8 + j * 18, 94 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 152));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (!MagicAmounts.areAmountsEqual(prevContained, tile.containedMagic)
                || !MagicAmounts.areAmountsEqual(prevRemaining, tile.remainingToConvert))
        {
            for (IContainerListener watcher : this.listeners)
            {
                if (watcher instanceof EntityPlayerMP)
                {
                    ElementsOfPower.channel.sendTo(new EssentializerAmountsUpdate(windowId, tile), (EntityPlayerMP) watcher);
                }
            }

            prevContained = MagicAmounts.copyOf(tile.containedMagic);
            prevRemaining = MagicAmounts.copyOf(tile.remainingToConvert);
        }
    }

    public void updateAmounts(MagicAmounts contained, MagicAmounts remaining)
    {
        tile.containedMagic = contained;
        tile.remainingToConvert = remaining;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        if (slotIndex < 8)
        {
            return null;
        }

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

        if (slotIndex >= 3)
        {
            boolean itemIsContainer = ContainerInformation.canItemContainMagic(stack);
            boolean itemContainsMagic = itemIsContainer && ContainerInformation.itemContainsMagic(stack);
            boolean itemHasEssence = EssenceConversions.itemHasEssence(stack);

            if (itemContainsMagic)
            {
                startIndex = 1;
                endIndex = startIndex + 1;
            }
            else if (itemIsContainer)
            {
                startIndex = 2;
                endIndex = startIndex + 1;
            }
            else if (itemHasEssence)
            {
                startIndex = 0;
                endIndex = startIndex + 1;
            }
            else if (slotIndex < (27 + 3))
            {
                startIndex = 27 + 3;
                endIndex = startIndex + 9;
            }
            else if (slotIndex >= (27 + 3))
            {
                startIndex = 3;
                endIndex = startIndex + 27;
            }
            else
            {
                return null;
            }
        }
        else
        {
            startIndex = 3;
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
}
