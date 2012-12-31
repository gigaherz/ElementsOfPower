package gigaherz.elementsofpower;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.Configuration;

public class MagicDatabase
{
    public static Map<ItemStack, ItemStack> containerConversion = new HashMap<ItemStack, ItemStack>();
    public static Map<ItemStack, MagicAmounts> containerCapacity = new HashMap<ItemStack, MagicAmounts>();
    public static Map<ItemStack, MagicAmounts> itemEssences = new HashMap<ItemStack, MagicAmounts>();
    
    public static final String[] magicNames = {
	    "Fire", "Water", "Air", "Earth",
	    "Light", "Darkness", "Life", "Death"
    };

    public static void preInitialize(Configuration config)
    {
    }

    public static void initialize()
    {
    	containerConversion.put(new ItemStack(Item.dyePowder, 1, 4), new ItemStack(ElementsOfPower.lapisContainer, 1));
    	containerConversion.put(new ItemStack(Item.emerald, 1), new ItemStack(ElementsOfPower.emeraldContainer, 1));
    	containerConversion.put(new ItemStack(Item.diamond, 1), new ItemStack(ElementsOfPower.diamondContainer, 1));
        
        containerCapacity.put(new ItemStack(Item.dyePowder, 1, 4), new MagicAmounts().all(100));
        containerCapacity.put(new ItemStack(Item.emerald, 1), new MagicAmounts().all(500));
        containerCapacity.put(new ItemStack(Item.diamond, 1), new MagicAmounts().all(1000));
        itemEssences.put(new ItemStack(Block.dirt, 1), new MagicAmounts().earth(1));
        itemEssences.put(new ItemStack(Block.grass, 1), new MagicAmounts().earth(1).life(1));
        itemEssences.put(new ItemStack(Block.cobblestone, 1), new MagicAmounts().earth(5));
        itemEssences.put(new ItemStack(Block.stone, 1), new MagicAmounts().earth(10));
    }

    public static void postInitialize()
    {
    }

    private static ItemStack findKeyForValue(Map<ItemStack, ItemStack> map, ItemStack stack)
    {
        for (ItemStack k : map.keySet())
        {
        	ItemStack v = map.get(k); 
            
        	int dmg = v.getItemDamage();
            if (v.itemID == stack.itemID && (dmg < 0 || dmg == stack.getItemDamage()))
            {
                return k;
            }
        }

        return null;
    }

    private static <OType> OType getFromMap(Map<ItemStack, OType> map, ItemStack stack)
    {
        for (ItemStack k : map.keySet())
        {
            int dmg = k.getItemDamage();

            if (k.itemID == stack.itemID && (dmg < 0 || dmg == stack.getItemDamage()))
            {
                return map.get(k);
            }
        }

        return null;
    }

    private static <OType> boolean stackIsInMap(Map<ItemStack, OType> map, ItemStack stack)
    {
        return getFromMap(map, stack) != null;
    }

    public static boolean itemContainsMagic(ItemStack stack)
    {
        if (stack.stackSize > 1)
        {
            return false;
        }

        if (stackIsInMap(containerCapacity, stack))
        {
            return false;
        }

        NBTTagList nbt = stack.getEnchantmentTagList();

        if (nbt == null)
        {
            return false;
        }

        for (int i = 0; i < nbt.tagCount(); i++)
        {
            NBTBase tag = nbt.tagAt(i);

            if (tag.getName().equals("magic.contained"))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean canItemContainMagic(ItemStack stack)
    {
        if (stack.stackSize > 1)
        {
            return false;
        }

        return stackIsInMap(containerCapacity, stack);
    }

    public static boolean itemHasEssence(ItemStack stack)
    {
        return stackIsInMap(itemEssences, stack);
    }

    public static MagicAmounts getEssences(ItemStack stack)
    {
        return getFromMap(itemEssences, stack);
    }

    public static MagicAmounts getContainedMagic(ItemStack output)
    {
        if (output == null)
        	return null;
        
        if (output.stackSize != 1)
        	return null;
        	        
        NBTTagCompound nbt = output.stackTagCompound;

        if (nbt == null)
        {
        	return null;
        }
        
        NBTTagCompound tag = nbt.getCompoundTag("magicContained");

        MagicAmounts amounts = new MagicAmounts();
        int max = 0;
        for (int i = 0; i < 8; i++)
        {
        	try
        	{
        		int amount = nbt.getInteger("" + i);
        		if(amount > max)
        			max = amount;
        		amounts.amounts[i] = amount;
        	}
        	catch(NumberFormatException ex)
        	{
        		continue;
        	}
        }
        
        if(max > 0)
        	return amounts;
        
        return null;
    }

    public static ItemStack setContainedMagic(ItemStack output, MagicAmounts amounts)
    {
        if (output == null)
        	return null;
        
        if (output.stackSize != 1)
        	return null;
        
        if(amounts != null)
        {
        	if(amounts.isEmpty())
        		amounts = null;
        }	        
        
        if(amounts != null)
        {
	        NBTTagCompound nbt = output.stackTagCompound;
	
	        if (nbt == null)
	        {
	        	if(stackIsInMap(containerConversion, output))
	        	{
	        		output = getFromMap(containerConversion, output).copy();
	        	}
	        	
	        	// output.setItemName(par1Str)
	        	nbt = new NBTTagCompound();
	        	output.stackTagCompound = nbt;
	        }
	        
	        NBTTagCompound tag = nbt.getCompoundTag("magicContained");
	
	        int max = 0;
	        for (int i = 0; i < 8; i++)
	        {
	        	nbt.setInteger("" + i, amounts.amounts[i]);
	        }
	
	        return output;
        }
        else
        {
        	output = findKeyForValue(containerConversion, output).copy();
        	
        	return null;
        }
    }

	public static String getMagicName(int i) {
		return magicNames[i];
	}
}
