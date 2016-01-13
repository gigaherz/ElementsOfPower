package gigaherz.elementsofpower.slots;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotMagic extends Slot
{
    TileEssentializer essentializer;

    public SlotMagic(TileEssentializer essentializer, int par2, int par3, int par4)
    {
        super(null, par2, par3, par4);
        this.essentializer = essentializer;
    }

    @Override
    public ItemStack getStack()
    {
        return new ItemStack(ElementsOfPower.magicOrb,
                (int) Math.floor(essentializer.containedMagic.amounts[getSlotIndex()]),
                getSlotIndex());
    }

    public boolean getHasStack()
    {
        return true;
    }

    @Override
    public void putStack(ItemStack stack)
    {
    }

    @Override
    public void onSlotChanged()
    {
    }

    @Override
    public int getSlotStackLimit()
    {
        return TileEssentializer.MaxEssentializerMagic;
    }

    @Override
    public ItemStack decrStackSize(int amount)
    {
        return null;
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack)
    {
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer par1EntityPlayer)
    {
        return false;
    }
}
