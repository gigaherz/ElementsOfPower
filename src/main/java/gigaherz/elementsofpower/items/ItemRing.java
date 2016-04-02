package gigaherz.elementsofpower.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemRing extends ItemGemContainer implements IBauble
{
    public static final float MAX_TRANSFER_TICK = 1 / 20.0f;

    public ItemRing(String name)
    {
        super(name);
        setUnlocalizedName(ElementsOfPower.MODID + ".ring");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    protected MagicAmounts adjustInsertedMagic(MagicAmounts am)
    {
        if (am == null)
            return null;

        return am.copy().multiply(1.5f);
    }

    @Override
    protected MagicAmounts adjustRemovedMagic(MagicAmounts am)
    {
        if (am == null)
            return null;

        return am.copy().multiply(1 / 1.5f);
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemstack)
    {
        return BaubleType.RING;
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player)
    {
        if (player.worldObj.isRemote)
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

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);

        if (worldIn.isRemote)
            return;

        if (entityIn instanceof EntityPlayer)
            tryTransferToWands(stack, (EntityPlayer) entityIn);
    }

    private void tryTransferToWands(ItemStack thisStack, EntityPlayer p)
    {
        Gemstone g = getGemstone(thisStack);
        Quality q = getQuality(thisStack);

        if (g == null || q == null)
            return;

        MagicAmounts self = ContainerInformation.getContainedMagic(thisStack);

        if (self == null || self.isEmpty())
            return;

        IInventory inv = null;
        int slot = 0;
        ItemStack stack = null;

        IInventory b = p.inventory;
        for (int i = 0; i < b.getSizeInventory(); i++)
        {
            ItemStack s = b.getStackInSlot(i);
            if (s == null || s == thisStack)
                continue;
            if (ContainerInformation.canItemContainMagic(s))
            {
                if (ContainerInformation.canTransferAnything(s, self))
                {
                    stack = s;
                    inv = b;
                    slot = i;
                    break;
                }
            }
        }

        if (stack == null)
        {
            b = BaublesApi.getBaubles(p);
            if (b != null)
            {
                for (int i = 0; i < b.getSizeInventory(); i++)
                {
                    ItemStack s = b.getStackInSlot(i);
                    if (s == null || s == thisStack)
                        continue;
                    if (ContainerInformation.canItemContainMagic(s))
                    {
                        if (ContainerInformation.canTransferAnything(s, self))
                        {
                            stack = s;
                            inv = b;
                            slot = i;
                            break;
                        }
                    }
                }
            }
        }

        if (stack == null)
            return;

        MagicAmounts limits = ContainerInformation.getMagicLimits(stack);
        MagicAmounts amounts = ContainerInformation.getContainedMagic(stack);

        if (limits == null)
            return;

        if (amounts == null)
            amounts = new MagicAmounts();

        float boost = 1.0f;
        switch (q)
        {
            case Rough:
                boost = 0.9f;
                break;
            case Common:
                boost = 1.0f;
                break;
            case Smooth:
                boost = 1.25f;
                break;
            case Flawless:
                boost = 1.5f;
                break;
            case Pure:
                boost = 2.0f;
                break;
        }

        float totalTransfer = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float maxTransfer = MAX_TRANSFER_TICK;

            if (g == Gemstone.Diamond || g.ordinal() == i)
                maxTransfer *= boost;

            float transfer = Math.min(maxTransfer, limits.amounts[i] - amounts.amounts[i]);
            if (!isInfinite(stack))
                transfer = Math.min(self.amounts[i], transfer);
            if (transfer > 0)
            {
                totalTransfer += transfer;
                amounts.amounts[i] += transfer;
                if (!isInfinite(stack))
                    self.amounts[i] -= transfer;
            }
        }

        if (totalTransfer > 0)
        {
            ItemStack stack2 = ContainerInformation.setContainedMagic(stack, amounts);
            if (stack2 != stack)
                inv.setInventorySlotContents(slot, stack2);

            if (!isInfinite(thisStack))
                ContainerInformation.setContainedMagic(thisStack, self);
        }
    }
}
