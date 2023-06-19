package dev.gigaherz.elementsofpower.essentializer.menu;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.network.UpdateEssentializerAmounts;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;

public class EssentializerMenu
        extends AbstractContainerMenu
{
    private final Player player;
    private final IMagicAmountHolder magicHolder;
    private MagicAmounts prevContained = MagicAmounts.EMPTY;
    private MagicAmounts prevRemaining = MagicAmounts.EMPTY;

    public EssentializerMenu(int id, Inventory playerInventory)
    {
        this(id, playerInventory, new ItemStackHandler(3), new IMagicAmountHolder()
        {
            private MagicAmounts remaining = MagicAmounts.EMPTY;
            private MagicAmounts contained = MagicAmounts.EMPTY;

            @Override
            public MagicAmounts getContainedMagic()
            {
                return this.contained;
            }

            @Override
            public MagicAmounts getRemainingToConvert()
            {
                return this.remaining;
            }

            @Override
            public void setContainedMagic(MagicAmounts contained)
            {
                this.contained = contained;
            }

            @Override
            public void setRemainingToConvert(MagicAmounts remaining)
            {
                this.remaining = remaining;
            }
        });
    }

    public EssentializerMenu(int id, EssentializerBlockEntity tileEntity, Inventory playerInventory)
    {
        this(id, playerInventory, tileEntity.getInventory(), tileEntity);
    }

    private EssentializerMenu(int id, Inventory playerInventory, IItemHandler inv, IMagicAmountHolder magicHolder)
    {
        super(ElementsOfPowerMod.ESSENTIALIZER_MENU.get(), id);

        this.magicHolder = magicHolder;
        this.player = playerInventory.player;

        addSlot(new MagicSourceSlot(AequivaleoPlugin.get(playerInventory.player.level), inv, 0, 80, 44));
        addSlot(new MagicContainerInputSlot(inv, 1, 8, 56));
        addSlot(new MagicContainerOutputSlot(inv, 2, 152, 56));

        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(Inventory playerInventory)
    {
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlot(new Slot(playerInventory,
                        j + i * 9 + 9,
                        8 + j * 18, 94 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++)
        {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 152));
        }
    }

    public IMagicAmountHolder getMagicHolder()
    {
        return magicHolder;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        if (!prevContained.equals(magicHolder.getContainedMagic())
                || !prevRemaining.equals(magicHolder.getRemainingToConvert()))
        {
            if (player instanceof ServerPlayer serverPlayer)
            {
                ElementsOfPowerMod.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new UpdateEssentializerAmounts(containerId, magicHolder));
            }

            prevContained = magicHolder.getContainedMagic();
            prevRemaining = magicHolder.getRemainingToConvert();
        }
    }

    public void updateAmounts(MagicAmounts contained, MagicAmounts remaining)
    {
        magicHolder.setContainedMagic(contained);
        magicHolder.setRemainingToConvert(remaining);
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

        if (slotIndex >= 3)
        {
            boolean itemIsContainer = MagicContainerCapability.hasContainer(stack);
            boolean itemHasEssence = AequivaleoPlugin.getEssences(player.level, stack, false).isPresent();

            if (itemIsContainer)
            {
                startIndex = 1;
                endIndex = startIndex + 2;
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
                return ItemStack.EMPTY;
            }
        }
        else
        {
            startIndex = 3;
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
}
