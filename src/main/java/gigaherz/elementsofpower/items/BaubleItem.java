package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class BaubleItem extends GemContainerItem
{
    private static final float MAX_TRANSFER_TICK = 1 / 20.0f;
    private static final float[] TRANSFER_RATES = {
            MAX_TRANSFER_TICK,
            MAX_TRANSFER_TICK * 2,
            MAX_TRANSFER_TICK * 5,
            MAX_TRANSFER_TICK * 10,
            MAX_TRANSFER_TICK * 25
    };

    static {
        assert TRANSFER_RATES.length == Quality.values().length;
    }

    public BaubleItem(Properties properties)
    {
        super(properties);
    }

    interface ItemSlotReference
    {
        ItemStack get();

        void set(ItemStack stack);
    }

    @Nullable
    private static ItemSlotReference
    findInInventory(ItemStack thisStack, @Nullable final IInventory b, MagicAmounts available)
    {
        if (b == null)
            return null;

        for (int i = 0; i < b.getSizeInventory(); i++)
        {
            ItemStack s = b.getStackInSlot(i);
            if (canReceiveMagic(thisStack, s, available))
            {
                final int slot = i;
                return new ItemSlotReference()
                {
                    @Override
                    public ItemStack get()
                    {
                        return b.getStackInSlot(slot);
                    }

                    @Override
                    public void set(ItemStack stack)
                    {
                        b.setInventorySlotContents(slot, stack);
                    }
                };
            }
        }

        return null;
    }

    @Nullable
    private static ItemSlotReference
    findInInventory(ItemStack thisStack, @Nullable IItemHandlerModifiable b, MagicAmounts available)
    {
        if (b == null)
            return null;

        for (int i = 0; i < b.getSlots(); i++)
        {
            ItemStack s = b.getStackInSlot(i);
            if (canReceiveMagic(thisStack, s, available))
            {
                final int slot = i;
                return new ItemSlotReference()
                {
                    @Override
                    public ItemStack get()
                    {
                        return b.getStackInSlot(slot);
                    }

                    @Override
                    public void set(ItemStack stack)
                    {
                        b.setStackInSlot(slot, stack);
                    }
                };
            }
        }

        return null;
    }

    private static boolean canReceiveMagic(ItemStack thisStack, ItemStack s, MagicAmounts available)
    {
        return s != thisStack
                && MagicContainerCapability.isNotFull(s, available);
    }


    protected MagicAmounts adjustInsertedMagic(MagicAmounts am)
    {
        return am.multiply(1.5f);
    }

    protected MagicAmounts adjustRemovedMagic(MagicAmounts am)
    {
        return am.multiply(1 / 1.5f);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

        if (worldIn.isRemote)
            return;

        if (entityIn instanceof PlayerEntity)
            tryTransferToWands(stack, (PlayerEntity) entityIn);
    }

    protected void tryTransferToWands(ItemStack thisStack, PlayerEntity p)
    {
        MagicContainerCapability.getContainer(thisStack).ifPresent(magic -> {
            MagicAmounts available = magic.getContainedMagic();

            if (available.isEmpty())
                return;

            ItemSlotReference slotReference = findInInventory(thisStack, p.inventory, available);

        /*if (slotReference == null)
        {
            slotReference = findInInventory(thisStack, BaublesApi.getBaublesHandler(p), available);
        }*/

            if (slotReference == null)
                return;

            doTransfer(thisStack, magic, available, slotReference);
        });

    }

    private void doTransfer(ItemStack thisStack, IMagicContainer thisMagic,
                            MagicAmounts available,
                            ItemSlotReference slotReference)
    {
        ItemStack stack = slotReference.get();
        MagicContainerCapability.getContainer(stack).ifPresent(magic -> {

            MagicAmounts limits = magic.getCapacity();
            MagicAmounts amounts = magic.getContainedMagic();

            if (limits.isEmpty())
                return;

            MagicAmounts remaining = available;

            Gemstone g = getGemstone(thisStack);
            Quality q = getQuality(thisStack);
            if (g != null && q != null)
            {
                float maxTransferFrom = TRANSFER_RATES[q.ordinal()];
                float boost = q.getTransferSpeed();

                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    float maxTransfer = maxTransferFrom;

                    if (g == Gemstone.Diamond || g.ordinal() == i)
                        maxTransfer *= boost;

                    float transfer = Math.min(maxTransfer, limits.get(i) - amounts.get(i));
                    if (!thisMagic.isInfinite())
                        transfer = Math.min(remaining.get(i), transfer);
                    if (transfer > 0)
                    {
                        amounts = amounts.add(i, transfer);
                        if (!thisMagic.isInfinite())
                            remaining = remaining.add(i, -transfer);
                    }
                }
            }

            if (remaining.lessThan(available))
            {
                magic.setContainedMagic(amounts);

                if (!thisMagic.isInfinite())
                    thisMagic.setContainedMagic(remaining);
            }
        });
    }
}
