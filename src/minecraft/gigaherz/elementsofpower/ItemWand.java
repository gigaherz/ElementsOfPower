package gigaherz.elementsofpower;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemWand extends ItemMagicContainer
{
    private static final String[] subNames = { "lapis", "emerald", "diamond" };

    public ItemWand(int id)
    {
        super(id);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    public int getIconFromDamage(int par1)
    {
        return this.iconIndex + par1 * 16;
    }

    @Override
    public int getMetadata(int damageValue)
    {
        return damageValue;
    }

    public String getTextureFile()
    {
        return CommonProxy.ITEMS_PNG;
    }

    @Override
    public String getItemNameIS(ItemStack stack)
    {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length)
        {
            sub = 0;
        }

        return getItemName() + "." + subNames[sub];
    }

    public ItemStack getStack(int count, int damageValue)
    {
        ItemStack stack = new ItemStack(this, count);
        stack.setItemDamage(damageValue);
        return stack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int unknown, CreativeTabs tab, List subItems)
    {
        for (int meta = 1; meta < subNames.length; meta++)
        {
            subItems.add(new ItemStack(this, 1, meta));
        }
    }
    
    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }
    
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining)
    {
    	//ElementsOfPower.instance.proxy.sendMagicItemPacket(stack, world, player, Math.max(0, remaining));
    	if(!world.isRemote)
    	{
    		onMagicItemReleased(stack, world, player, remaining);
    	}
    }

    public ItemStack onFoodEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        return par1ItemStack;
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }

    @Override
	public void onMagicItemReleased(ItemStack stack, World world,
			EntityPlayer player, int remaining) 
    {
    	int charge = this.getMaxItemUseDuration(stack) - remaining;
        int power = Math.min(3, charge / 5);

        Vec3 var20 = player.getLook(1.0F);        
        
        System.out.println("BOOM! " + charge + " / " + power);
        
		if (power > 0)
	    {	        
	        EntityLargeFireball var17 = new EntityLargeFireball(world, player, var20.xCoord * 10, var20.yCoord * 10, var20.zCoord * 10);
	
	        // explosion power
	        var17.field_92012_e = power;
	        
	        var17.posX = player.posX + var20.xCoord * player.width * 0.75f;
	        var17.posY = player.posY + 1.0f;
	        var17.posZ = player.posZ + var20.zCoord * player.width * 0.75f;
	        
	        world.spawnEntityInWorld(var17);
	    }
		else 
	    {	        
	        EntitySmallFireball var17 = new EntitySmallFireball(world, player, var20.xCoord * 10, var20.yCoord * 10, var20.zCoord * 10);
	        
	        var17.posX = player.posX + var20.xCoord * 2;
	        var17.posY = player.posY + 1.0f;
	        var17.posZ = player.posZ + var20.zCoord * 2;
	        
	        world.spawnEntityInWorld(var17);
	    }
	}
}
