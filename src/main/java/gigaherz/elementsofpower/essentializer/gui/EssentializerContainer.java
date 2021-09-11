package gigaherz.elementsofpower.essentializer.gui;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.essentializer.EssentializerTileEntity;
import gigaherz.elementsofpower.network.UpdateEssentializerAmounts;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ObjectHolder;

import java.util.regex.Pattern;

public class EssentializerContainer
        extends Container
{
    @ObjectHolder("elementsofpower:essentializer")
    public static ContainerType<EssentializerContainer> TYPE;

    protected final World world;
    protected IMagicAmountHolder magicHolder;
    private MagicAmounts prevContained = MagicAmounts.EMPTY;
    private MagicAmounts prevRemaining = MagicAmounts.EMPTY;

    public EssentializerContainer(int id, PlayerInventory playerInventory)
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

    public EssentializerContainer(int id, EssentializerTileEntity tileEntity, PlayerInventory playerInventory)
    {
        this(id, playerInventory, tileEntity.getInventory(), tileEntity);
    }

    private EssentializerContainer(int id, PlayerInventory playerInventory, IItemHandler inv, IMagicAmountHolder magicHolder)
    {
        super(TYPE, id);

        this.world = playerInventory.player.world;
        this.magicHolder = magicHolder;

        addSlot(new MagicSourceSlot(AequivaleoPlugin.get(playerInventory.player.world), inv, 0, 80, 44));
        addSlot(new MagicContainerInputSlot(inv, 1, 8, 56));
        addSlot(new MagicContainerOutputSlot(inv, 2, 152, 56));

        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(PlayerInventory playerInventory)
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
    public boolean canInteractWith(PlayerEntity player)
    {
        return true;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (!prevContained.equals(magicHolder.getContainedMagic())
                || !prevRemaining.equals(magicHolder.getRemainingToConvert()))
        {
            for (IContainerListener watcher : this.listeners)
            {
                if (watcher instanceof ServerPlayerEntity)
                {
                    ElementsOfPowerMod.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) watcher),
                            new UpdateEssentializerAmounts(windowId, magicHolder));
                }
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

        if (slotIndex >= 3)
        {
            boolean itemIsContainer = MagicContainerCapability.hasContainer(stack);
            boolean itemHasEssence = AequivaleoPlugin.getEssences(player.world, stack, false).isPresent();

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
}
