package gigaherz.elementsofpower;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.*;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemWand extends ItemMagicContainer {
    private static final String[] subNames = {
            "lapisWand", "emeraldWand", "diamondWand", "creativeWand",
            "lapisStaff", "emeraldStaff", "diamondStaff", "creativeStaff"
    };

    private static final EnumRarity rarities[] = {
            EnumRarity.UNCOMMON,EnumRarity.RARE,EnumRarity.EPIC,EnumRarity.COMMON,
            EnumRarity.UNCOMMON,EnumRarity.RARE,EnumRarity.EPIC,EnumRarity.COMMON
    };


    public ItemWand() {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setUnlocalizedName("magicWand");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return rarities[stack.getItemDamage()];
    }

    @Override
    public int getMetadata(int damageValue) {
        return damageValue;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length) {
            sub = 0;
        }

        return getUnlocalizedName() + "." + subNames[sub];
    }

    public ItemStack getStack(int count, int damageValue) {
        return new ItemStack(this, count, damageValue);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
        for (int meta = 0; meta < subNames.length; meta++) {
            subItems.add(new ItemStack(itemIn, 1, meta));
        }
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining) {
        //ElementsOfPower.instance.proxy.sendMagicItemPacket(stack, world, player, Math.max(0, remaining));
        if (!world.isRemote) {
            onMagicItemReleased(stack, world, player, remaining);
        }
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.BOW;
    }

    @Override
    public void onMagicItemReleased(ItemStack stack, World world,
                                    EntityPlayer player, int remaining) {
        int charge = this.getMaxItemUseDuration(stack) - remaining;
        int power = Math.min(3, charge / 5);

        Vec3 var20 = player.getLook(1.0F);

        System.out.println("BOOM! " + charge + " / " + power);

        if (power > 0) {
            EntityLargeFireball var17 = new EntityLargeFireball(world, player, var20.xCoord * 10, var20.yCoord * 10, var20.zCoord * 10);

            var17.explosionPower = power;

            var17.posX = player.posX + var20.xCoord * player.width * 0.75f;
            var17.posY = player.posY + 1.0f;
            var17.posZ = player.posZ + var20.zCoord * player.width * 0.75f;

            world.spawnEntityInWorld(var17);
        } else {
            EntitySmallFireball var17 = new EntitySmallFireball(world, player, var20.xCoord * 10, var20.yCoord * 10, var20.zCoord * 10);

            var17.posX = player.posX + var20.xCoord * 2;
            var17.posY = player.posY + 1.0f;
            var17.posZ = player.posZ + var20.zCoord * 2;

            world.spawnEntityInWorld(var17);
        }
    }

    // TODO: OLD STUFF THAT NEEDS REPLACING
    @SideOnly(Side.CLIENT)
    public int getIconFromDamage(int par1) {
        return 0;
        //return this.iconIndex + par1 * 16;
    }

}
