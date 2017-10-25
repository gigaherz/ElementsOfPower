package gigaherz.elementsofpower.test;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class DemoTileEntity extends TileEntity
{
    /* OPTIONAL !!!!!!!!!!!
     * Using "ITEMS_CAP" will be EXACTLY THE SAME as using CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
     * except that local statics are just very slightly faster to access, and shorter to type.
     */
    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEMS_CAP;
    /* END OF OPTIONAL */

    public static final int SLOT_COUNT = 9;

    final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            markDirty();
        }
    };

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == ITEMS_CAP) return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == ITEMS_CAP) return (T) inventory;
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey("Inventory"))
        {
            ITEMS_CAP.readNBT(inventory, null, compound.getTag("Inventory"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        compound.setTag("Inventory", ITEMS_CAP.writeNBT(inventory, null));

        return compound;
    }
}
