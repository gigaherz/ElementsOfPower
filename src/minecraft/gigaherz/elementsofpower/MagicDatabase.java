package gigaherz.elementsofpower;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.Configuration;

public class MagicDatabase 
{
	public static Map<ItemStack, MagicAmounts> containerCapacity = new HashMap<ItemStack, MagicAmounts>();
	public static Map<ItemStack, MagicAmounts> itemEssences = new HashMap<ItemStack, MagicAmounts>();

	public static void preInitialize(Configuration config) 
	{
		
	}

	public static void initialize()
	{
		containerCapacity.put(new ItemStack(Item.dyePowder, 1, 4), new MagicAmounts().all(100) );
		containerCapacity.put(new ItemStack(Item.emerald, 1), new MagicAmounts().all(500) );
		containerCapacity.put(new ItemStack(Item.diamond, 1), new MagicAmounts().all(1000) );

		itemEssences.put(new ItemStack(Block.dirt, 1), new MagicAmounts().earth(1) );
		itemEssences.put(new ItemStack(Block.cobblestone, 1), new MagicAmounts().earth(5) );
		itemEssences.put(new ItemStack(Block.stone, 1), new MagicAmounts().earth(10) );
	}

	public static void postInitialize() 
	{
		
	}

	private static MagicAmounts getAmountsForStack(Map<ItemStack, MagicAmounts> map, ItemStack stack) 
	{
		for(ItemStack k : map.keySet())
		{
			int dmg = k.getItemDamage();
			if(k.itemID == stack.itemID && (dmg < 0 || dmg == stack.getItemDamage()))
				return map.get(k);
		}
		return null;
	}

	private static boolean stackIsInMap(Map<ItemStack, MagicAmounts> map, ItemStack stack) 
	{		
		return getAmountsForStack(map, stack) != null;
	}

	public static boolean itemContainsMagic(ItemStack stack)
	{		
		if(stack.stackSize > 1)
			return false;
		
		if(stackIsInMap(containerCapacity, stack))
			return false;
		
		NBTTagList nbt = stack.getEnchantmentTagList();
		
		if(nbt == null)
			return false;
		
		for(int i = 0; i < nbt.tagCount(); i++)
		{
			NBTBase tag = nbt.tagAt(i);
			if(tag.getName().startsWith("magic."))
			{
				return true;
			}
		}
		
		return false;
	}

	public static boolean canItemContainMagic(ItemStack stack)
	{
		if(stack.stackSize > 1)
			return false;
		
		return stackIsInMap(containerCapacity, stack);		
	}

	public static boolean itemHasEssence(ItemStack stack)
	{
		return stackIsInMap(itemEssences, stack);	
	}

	public static MagicAmounts getEssences(ItemStack stack)
	{
		return getAmountsForStack(itemEssences, stack);
	}
}
