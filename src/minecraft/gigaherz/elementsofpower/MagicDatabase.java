package gigaherz.elementsofpower;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

public class MagicDatabase
{
    public static Map<ItemStack, ItemStack> containerConversion = new HashMap<ItemStack, ItemStack>();
    public static Map<ItemStack, MagicAmounts> containerCapacity = new HashMap<ItemStack, MagicAmounts>();
    public static Map<ItemStack, MagicAmounts> itemEssences = new HashMap<ItemStack, MagicAmounts>();

    public static final Map<Integer, String> magicNames = new HashMap<Integer, String>();

    public static void preInitialize(Configuration config)
    {
        magicNames.put(1 << 0, "Fire");
        magicNames.put(1 << 1, "Water");
        magicNames.put(1 << 2, "Air");
        magicNames.put(1 << 3, "Earth");
        magicNames.put(1 << 4, "Light");
        magicNames.put(1 << 5, "Darkness");
        magicNames.put(1 << 6, "Life");
        magicNames.put(1 << 7, "Death");
    }

    public static void initialize()
    {
        containerConversion.put(new ItemStack(Item.dyePowder, 1, 4), new ItemStack(ElementsOfPower.lapisContainer, 1));
        containerConversion.put(new ItemStack(Item.emerald, 1), new ItemStack(ElementsOfPower.emeraldContainer, 1));
        containerConversion.put(new ItemStack(Item.diamond, 1), new ItemStack(ElementsOfPower.diamondContainer, 1));
        containerCapacity.put(new ItemStack(Item.dyePowder, 1, 4), new MagicAmounts().all(10));
        containerCapacity.put(new ItemStack(Item.emerald, 1), new MagicAmounts().all(50));
        containerCapacity.put(new ItemStack(Item.diamond, 1), new MagicAmounts().all(100));
        containerCapacity.put(new ItemStack(ElementsOfPower.lapisContainer, 1), new MagicAmounts().all(10));
        containerCapacity.put(new ItemStack(ElementsOfPower.emeraldContainer, 1), new MagicAmounts().all(50));
        containerCapacity.put(new ItemStack(ElementsOfPower.diamondContainer, 1), new MagicAmounts().all(100));
        containerCapacity.put(ElementsOfPower.wandLapis, new MagicAmounts().all(10));
        containerCapacity.put(ElementsOfPower.wandEmerald, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.wandDiamond, new MagicAmounts().all(100));
        containerCapacity.put(ElementsOfPower.staffLapis, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.staffEmerald, new MagicAmounts().all(250));
        containerCapacity.put(ElementsOfPower.staffDiamond, new MagicAmounts().all(500));
        itemEssences.put(new ItemStack(Block.dirt, 1), new MagicAmounts().earth(1));
        itemEssences.put(new ItemStack(Block.grass, 1), new MagicAmounts().earth(1).life(1));
        itemEssences.put(new ItemStack(Block.cobblestone, 1), new MagicAmounts().earth(5));
        itemEssences.put(new ItemStack(Block.stone, 1), new MagicAmounts().earth(10));
    }

    public static void postInitialize()
    {
        //dumpAllRecipes();
    	dumpAllItems();
    }

    private static void dumpAllItems()
    {
        try
        {
            FileOutputStream fos = new FileOutputStream("items.csv");
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

            for (Block block : Block.blocksList)
            {
            	if(block == null)
            		continue;

            	String bn = block.getBlockName();
            	if(bn == null)
            	{
            		bn = "UNNAMED (" + block.getClass().getName() + ")";
            	}
            	
        		List<ItemStack> subBlocks = new ArrayList<ItemStack>();
        		
        		block.getSubBlocks(0, CreativeTabs.tabAllSearch, subBlocks);
        		
        		if(subBlocks.size() == 0)
        			continue;
        		
        		ItemStack sb0 = subBlocks.get(0);
        		if(sb0.itemID != 0)
        		{
	            	for(int i=0;i<subBlocks.size(); i++)
	        		{
	                	ItemStack is = new ItemStack(block, 1, i);
	                	
	                	Item itemBlock = is.getItem();
	                	    	
	                	String disp = null;
	                	
	                	if(itemBlock != null) 
	                		disp = itemBlock.getItemDisplayName(is);
	                	
	                	
	                	if(disp == null || disp.length() == 0)
	                		disp = "UNNAMED (" + bn + ")";
	                	
	                	
	                	out.write(disp);
	                    out.write(";" + is.itemID);
	                    out.write(";" + is.getItemDamage());    
	                    out.write("\r\n");                	                    	
	        		}
        		}
            }

            for (Item item : Item.itemsList)
            {
            	if(item == null)
            		continue;
            	
            	if(item.getHasSubtypes())
            	{
            		List subItems = new ArrayList();
            		
            		item.getSubItems(0, CreativeTabs.tabAllSearch, subItems);
            		
            		for(int i=0;i<subItems.size(); i++)
            		{
                    	ItemStack is = new ItemStack(item, 1, i);

                    	String disp = null;
                    	disp = is.getDisplayName();
                    	
                    	if(disp == null || disp.length() == 0)
                    		disp = "UNNAMED (" + is.getItemName() + ")";
                    	
                    	out.write(disp);
                        out.write(";" + is.itemID);
                        out.write(";" + is.getItemDamage());    
                        out.write("\r\n");                	                    	
            		}
            	}
            	else
        		{
                	ItemStack is = new ItemStack(item, 1);

                	String disp = null;
                	disp = is.getDisplayName();
                	
                	if(disp == null || disp.length() == 0)
                		disp = "UNNAMED (" + is.getItemName() + ")";
                	
                	out.write(disp);
                    out.write(";" + is.itemID);
                    out.write(";" + is.getItemDamage());    
                    out.write("\r\n");                	                    	
        		}
            }

            out.close();
        }
        catch (IOException e)
        {
            return;
        }
    }

    private static void dumpAllRecipes()
    {
        try
        {
            FileOutputStream fos = new FileOutputStream("sources.csv");
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
            List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
            List<ItemStack> craftables = new ArrayList<ItemStack>();

            for (IRecipe recipe : recipeList)
            {
                ItemStack output = recipe.getRecipeOutput();

                if (output == null)
                {
                    continue;
                }

                if (!listContains(craftables, output))
                {
                    craftables.add(output);
                }
            }

            dumpRecipesMethod1(out, recipeList, craftables);
            out.close();
        }
        catch (IOException e)
        {
            return;
        }
    }

    private static void dumpRecipesMethod1(OutputStreamWriter out,
            List<IRecipe> recipeList, List<ItemStack> craftables) throws IOException
    {
        List<ItemStack> nonCraftables = new ArrayList<ItemStack>();
        Map<ItemStack, List<ItemStack>> outputs = new HashMap<ItemStack, List<ItemStack>>();
        boolean dumpRemaining = false;

        while (true)
        {
            int dumped = 0;

            for (IRecipe recipe : recipeList)
            {
                ItemStack output = recipe.getRecipeOutput();

                if (output == null)
                {
                    continue;
                }

                if (stackIsInMap(outputs, output))
                {
                    continue;
                }

                List<ItemStack> items = getInputsListForRecipe(recipe);

                if (items == null)
                {
                    continue;
                }

                List<ItemStack> itemsResolved = new ArrayList<ItemStack>();
                boolean foundAll = reduceItemsList(craftables, nonCraftables,
                        items, itemsResolved);
                items = itemsResolved;

                if (!foundAll && !dumpRemaining)
                {
                    continue;
                }

                outputs.put(output, items);
                dumpRecipe(out, output, items);
                dumped++;
            }

            if (dumped == 0)
            {
                if (dumpRemaining)
                {
                    break;
                }
                else
                {
                    dumpRemaining = true;
                }
            }
            else
            {
                dumpRemaining = false;
            }
        }

        for (ItemStack is : nonCraftables)
        {
            dumpRecipe(out, is, null);
        }
    }

    private static boolean reduceItemsList(List<ItemStack> craftables,
            List<ItemStack> nonCraftables, List<ItemStack> items,
            List<ItemStack> itemsResolved)
    {
        boolean foundAll = true;

        for (ItemStack is : items)
        {
            if (is == null)
            {
                continue;
            }

            if (!listContains(craftables, is))
                if (!listContains(nonCraftables, is))
                {
                    nonCraftables.add(is);
                }

            ItemStack existing = getExistingInList(itemsResolved, is);

            if (existing != null)
            {
                if (existing != is)
                {
                    existing.stackSize ++;
                }
            }
            else
            {
                ItemStack isc = is.copy();
                isc.stackSize = 1;
                itemsResolved.add(isc);
            }
        }

        return foundAll;
    }

    private static void dumpRecipe(
            OutputStreamWriter out,
            ItemStack output,
            List<ItemStack> items) throws IOException
    {
        out.write(output.getDisplayName());
        out.write(";" + output.stackSize);
        out.write(";" + output.itemID);
        out.write(";" + output.getItemDamage());

        if (items != null)
        {
            for (ItemStack is : items)
            {
                out.write(";" + is.getDisplayName());
                out.write(";" + is.stackSize);
                out.write(";" + is.itemID);
                out.write(";" + is.getItemDamage());
            }
        }

        out.write("\r\n");
    }

    private static List<ItemStack> getInputsListForRecipe(IRecipe recipe)
    {
        if (recipe instanceof ShapelessRecipes)
        {
            ShapelessRecipes sr = (ShapelessRecipes)recipe;
            return sr.recipeItems;
        }
        else if (recipe instanceof ShapedRecipes)
        {
            ShapedRecipes sr = (ShapedRecipes)recipe;
            return Arrays.asList(sr.recipeItems);
        }

        return null;
    }

    private static ItemStack findKeyForValue(Map<ItemStack, ItemStack> map, ItemStack stack)
    {
        if (stack == null)
        {
            return null;
        }

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

    private static boolean listContains(List<ItemStack> list, ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        for (ItemStack k : list)
        {
            int dmg = k.getItemDamage();

            if (k.itemID == stack.itemID && (dmg < 0 || dmg == stack.getItemDamage()))
            {
                return true;
            }
        }

        return false;
    }

    private static ItemStack getExistingInList(List<ItemStack> list, ItemStack stack)
    {
        if (stack == null)
        {
            return null;
        }

        for (ItemStack k : list)
        {
            int dmg = k.getItemDamage();

            if (k.itemID == stack.itemID && (dmg < 0 || dmg == stack.getItemDamage()))
            {
                return k;
            }
        }

        return null;
    }

    private static <OType> OType getFromMap(Map<ItemStack, OType> map, ItemStack stack)
    {
        if (stack == null)
        {
            return null;
        }

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
        if (stack == null)
        {
            return false;
        }

        return getFromMap(map, stack) != null;
    }

    public static boolean itemContainsMagic(ItemStack stack)
    {
        MagicAmounts amounts = getContainedMagic(stack);

        if (amounts == null)
        {
            return false;
        }

        return ! amounts.isEmpty();
    }

    public static boolean canItemContainMagic(ItemStack stack)
    {
        if (stack.stackSize > 1)
        {
            return false;
        }

        return stackIsInMap(containerCapacity, stack);
    }
    
	public static MagicAmounts getMagicLimits(ItemStack stack)
	{
		if (stack.stackSize > 1)
        {
            return null;
        }

        return getFromMap(containerCapacity, stack);
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
        {
            return null;
        }

        if (output.stackSize != 1)
        {
            return null;
        }

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

                if (amount > max)
                {
                    max = amount;
                }

                amounts.amounts[i] = amount;
            }
            catch (NumberFormatException ex)
            {
                continue;
            }
        }

        if (max > 0)
        {
            return amounts;
        }

        return null;
    }

    public static ItemStack setContainedMagic(ItemStack output, MagicAmounts amounts)
    {
        if (output == null)
        {
            return null;
        }

        if (output.stackSize != 1)
        {
            return null;
        }

        if (amounts != null)
        {
            if (amounts.isEmpty())
            {
                amounts = null;
            }
        }

        if (amounts != null)
        {
            NBTTagCompound nbt = output.stackTagCompound;

            if (nbt == null)
            {
                if (stackIsInMap(containerConversion, output))
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
            output.stackTagCompound = null;
            ItemStack is = findKeyForValue(containerConversion, output);

            if (is != null)
            {
                output = is.copy();
            }

            return output;
        }
    }

    public static String getMagicName(int i)
    {
        return magicNames.get(1 << i);
    }

    public static String getMagicName(EnumSet<MagicEssences> bits)
    {
        int n = 0;

        for (int i = 0; i < 8; i++)
        {
            if (bits.contains(MagicEssences.values()[i]))
            {
                n |= 1 << i;
            }
        }

        return magicNames.get(n);
    }

    public static String getMagicNameCombined(int i)
    {
        return magicNames.get(i);
    }
}

enum MagicEssences
{
    fire, water, air, earth, light, darkness, life, death
}
