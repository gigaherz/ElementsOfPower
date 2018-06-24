package gigaherz.elementsofpower.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Optional.Interface(modid = "baubles", iface = "baubles.api.IBauble")
public abstract class ItemBauble extends ItemGemContainer
{
    private static final float MAX_TRANSFER_TICK = 1 / 20.0f;

    protected ItemBauble(String name)
    {
        super(name);
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
                && ContainerInformation.canItemContainMagic(s)
                && ContainerInformation.canTransferAnything(s, available);
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
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);

        if (worldIn.isRemote)
            return;

        if (entityIn instanceof EntityPlayer)
            tryTransferToWands(stack, (EntityPlayer) entityIn);
    }

    protected void tryTransferToWands(ItemStack thisStack, EntityPlayer p)
    {
        IMagicContainer magic = ContainerInformation.getMagic(thisStack);
        if (magic == null)
            return;

        MagicAmounts available = magic.getContainedMagic();

        if (available.isEmpty())
            return;

        ItemSlotReference slotReference = findInInventory(thisStack, p.inventory, available);

        if (slotReference == null)
        {
            slotReference = findInInventory(thisStack, BaublesApi.getBaublesHandler(p), available);
        }

        if (slotReference == null)
            return;

        doTransfer(thisStack, magic, available, slotReference);
    }

    private void doTransfer(ItemStack thisStack, IMagicContainer thisMagic,
                            MagicAmounts available,
                            ItemSlotReference slotReference)
    {
        ItemStack stack = slotReference.get();
        IMagicContainer magic = ContainerInformation.getMagic(stack);
        if (magic == null)
            return;

        MagicAmounts limits = magic.getCapacity();
        MagicAmounts amounts = magic.getContainedMagic();

        if (limits.isEmpty())
            return;

        float totalTransfer = getTotalTransfer(thisStack, thisMagic, available, stack, limits, amounts);

        if (totalTransfer > 0)
        {
            magic.setContainedMagic(amounts);

            if (!thisMagic.isInfinite())
                thisMagic.setContainedMagic(available);
        }
    }

    private float getTotalTransfer(ItemStack thisStack, IMagicContainer thisMagic,
                                   MagicAmounts available, ItemStack stack,
                                   MagicAmounts limits, MagicAmounts amounts)
    {
        Gemstone g = getGemstone(thisStack);
        Quality q = getQuality(thisStack);

        if (g == null || q == null)
            return 0;

        float boost = q.getTransferSpeed();

        float totalTransfer = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float maxTransfer = ItemBauble.MAX_TRANSFER_TICK;

            if (g == Gemstone.Diamond || g.ordinal() == i)
                maxTransfer *= boost;

            float transfer = Math.min(maxTransfer, limits.get(i) - amounts.get(i));
            if (!thisMagic.isInfinite())
                transfer = Math.min(available.get(i), transfer);
            if (transfer > 0)
            {
                totalTransfer += transfer;
                amounts = amounts.add(i, transfer);
                if (!thisMagic.isInfinite())
                    available = available.add(i, -transfer);
            }
        }

        return totalTransfer;
    }

    private static Capability<IBauble> BAUBLE_ITEM_CAP = null;

    @CapabilityInject(IBauble.class)
    public static void injectBaubleCap(Capability<IBauble> cap)
    {
        BAUBLE_ITEM_CAP = cap;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack _stack, @Nullable NBTTagCompound nbt)
    {
        return BAUBLE_ITEM_CAP == null ? null : new ICapabilityProvider()
        {
            ItemStack stack = _stack;

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
            {
                return capability == BAUBLE_ITEM_CAP;
            }

            @SuppressWarnings("unchecked")
            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
            {
                if (capability == BAUBLE_ITEM_CAP)
                    return (T) getBaubleInstance();
                return null;
            }
        };
    }

    protected abstract Object getBaubleInstance();

    protected abstract class BaubleData implements IBauble
    {
        @Optional.Method(modid = "baubles")
        @Override
        public abstract BaubleType getBaubleType(ItemStack itemstack);

        @Override
        public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player)
        {
            return true;
        }

        @Override
        public void onWornTick(ItemStack itemstack, EntityLivingBase player)
        {
            if (player.world.isRemote)
                return;

            if (player instanceof EntityPlayer)
                tryTransferToWands(itemstack, (EntityPlayer) player);
        }

        @Override
        public void onEquipped(ItemStack itemstack, EntityLivingBase player)
        {

        }

        @Override
        public void onUnequipped(ItemStack itemstack, EntityLivingBase player)
        {

        }

        @Override
        public boolean canEquip(ItemStack itemstack, EntityLivingBase player)
        {
            return true;
        }

        @Override
        public boolean canUnequip(ItemStack itemstack, EntityLivingBase player)
        {
            return true;
        }
    }
}
