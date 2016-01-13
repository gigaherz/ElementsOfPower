package gigaherz.elementsofpower.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.MagicDatabase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemRing extends ItemMagicContainer implements IBauble
{
    public static final float MAX_TRANSFER_TICK = 1 / 20.0f;

    private static final String[] subNames = {
            ".lapisRing", ".emeraldRing", ".diamondRing", ".creativeRing"
    };

    private static final EnumRarity rarities[] = {
            EnumRarity.UNCOMMON, EnumRarity.RARE, EnumRarity.EPIC, EnumRarity.COMMON
    };

    private static final boolean areCreative[] = {
            false, false, false, true
    };

    @Override
    public boolean isInfinite(ItemStack stack)
    {
        int dmg = stack.getItemDamage();
        return dmg <= areCreative.length && areCreative[dmg];
    }

    public ItemRing()
    {
        super();
        setHasSubtypes(true);
        setUnlocalizedName(ElementsOfPower.MODID + ".magicWand");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack)
    {
        return rarities[stack.getItemDamage()];
    }

    @Override
    public int getMetadata(int damageValue)
    {
        return damageValue;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length)
        {
            return getUnlocalizedName();
        }

        return "item." + ElementsOfPower.MODID + subNames[sub];
    }

    @Override
    public ItemStack getStack(int count, int damageValue)
    {
        return new ItemStack(this, count, damageValue);
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int meta = 0; meta < subNames.length; meta++)
        {
            subItems.add(new ItemStack(itemIn, 1, meta));
        }
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
        MagicAmounts self = MagicDatabase.getContainedMagic(thisStack);

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
            if (MagicDatabase.canItemContainMagic(s))
            {
                if (MagicDatabase.canTransferAnything(s, self))
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
                    if (MagicDatabase.canItemContainMagic(s))
                    {
                        if (MagicDatabase.canTransferAnything(s, self))
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

        MagicAmounts limits = MagicDatabase.getMagicLimits(stack);
        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);

        if (limits == null)
            return;

        if (amounts == null)
            amounts = new MagicAmounts();

        float maxTransfer = MAX_TRANSFER_TICK;
        float totalTransfer = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float transfer = Math.min(maxTransfer, limits.amounts[i] - amounts.amounts[i]);
            if(!isInfinite(stack))
                transfer = Math.min(self.amounts[i], transfer);
            if (transfer > 0)
            {
                totalTransfer += transfer;
                maxTransfer -= transfer;
                amounts.amounts[i] += transfer;
                if(!isInfinite(stack))
                    self.amounts[i] -= transfer;
            }
        }

        if (totalTransfer > 0)
        {
            ItemStack stack2 = MagicDatabase.setContainedMagic(stack, amounts);
            if (stack2 != stack)
                inv.setInventorySlotContents(slot, stack2);

            if (!isInfinite(thisStack))
                MagicDatabase.setContainedMagic(thisStack, self);
        }
    }
}
