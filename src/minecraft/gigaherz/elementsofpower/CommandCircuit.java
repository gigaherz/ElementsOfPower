package gigaherz.elementsofpower;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CommandCircuit extends Item
{
    private final static String[] subNames =
    {
        "unprogrammed",

        // Tier 1
        "planter", "harvester", "woodcutter",

        // Tier 2
        "fertilizer", "tiller",

        // Tier 3
        "miner", "filler",
    };

    public CommandCircuit(int id)
    {
        super(id);
        // Constructor Configuration
        setMaxStackSize(64);
        setCreativeTab(CreativeTabs.tabMisc);
        setIconIndex(0);
        setItemName("commandCircuit");
        setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    public int getIconFromDamage(int par1)
    {
    	switch(par1)
    	{
    	case 1:
    		return 0;
    	case 2:
    		return 1;
    	case 3:
    		return 2;

    	case 4:
    		return 17;
    	case 5:
    		return 16;

    	case 6:
    		return 32;
    	case 7:
    		return 33;
    	}
        return this.iconIndex;
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
}
