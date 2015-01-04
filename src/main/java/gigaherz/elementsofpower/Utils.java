package gigaherz.elementsofpower;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by gigaherz on 04/01/2015.
 */
public class Utils {
    public static void dumpAllItems() {
        try {
            FileOutputStream fos = new FileOutputStream("items.csv");
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

            for (Object obj : Block.blockRegistry) {
                if (!(obj instanceof Block))
                    return;

                Block block = (Block) obj;

                if (block == null)
                    continue;

                List<ItemStack> subBlocks = new ArrayList<ItemStack>();

                block.getSubBlocks(Item.getItemFromBlock(block), CreativeTabs.tabAllSearch, subBlocks);

                if (subBlocks.size() == 0)
                    continue;


                for (ItemStack is : subBlocks) {
                    out.write(is.getUnlocalizedName());
                    out.write(";" + is.getItemDamage());
                    out.write("\r\n");
                }
            }

            for (Object obj : Item.itemRegistry) {
                if (!(obj instanceof Item))
                    return;

                Item item = (Item) obj;

                if (item == null)
                    continue;

                if (item.getHasSubtypes()) {
                    List subItems = new ArrayList();

                    item.getSubItems(item, CreativeTabs.tabAllSearch, subItems);

                    for (int i = 0; i < subItems.size(); i++) {
                        ItemStack is = new ItemStack(item, 1, i);

                        out.write(is.getUnlocalizedName());
                        out.write(";" + is.getItemDamage());
                        out.write("\r\n");
                    }
                } else {
                    ItemStack is = new ItemStack(item, 1);

                    out.write(is.getUnlocalizedName());
                    out.write(";" + is.getItemDamage());
                    out.write("\r\n");
                }
            }

            out.close();
        } catch (IOException e) {
            return;
        }
    }

    public static void dumpAllRecipes() {
        try {
            FileOutputStream fos = new FileOutputStream("sources.csv");
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
            List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
            List<ItemStack> craftables = new ArrayList<ItemStack>();

            for (IRecipe recipe : recipeList) {
                ItemStack output = recipe.getRecipeOutput();

                if (output == null) {
                    continue;
                }

                if (!listContains(craftables, output)) {
                    craftables.add(output);
                }
            }

            dumpRecipesMethod1(out, recipeList, craftables);
            out.close();
        } catch (IOException e) {
            return;
        }
    }

    public static void dumpRecipesMethod1(OutputStreamWriter out,
                                          List<IRecipe> recipeList, List<ItemStack> craftables) throws IOException {
        List<ItemStack> nonCraftables = new ArrayList<ItemStack>();
        Map<ItemStack, List<ItemStack>> outputs = new HashMap<ItemStack, List<ItemStack>>();
        boolean dumpRemaining = false;

        while (true) {
            int dumped = 0;

            for (IRecipe recipe : recipeList) {
                ItemStack output = recipe.getRecipeOutput();

                if (output == null) {
                    continue;
                }

                if (stackIsInMap(outputs, output)) {
                    continue;
                }

                List<ItemStack> items = getInputsListForRecipe(recipe);

                if (items == null) {
                    continue;
                }

                List<ItemStack> itemsResolved = new ArrayList<ItemStack>();
                boolean foundAll = reduceItemsList(craftables, nonCraftables,
                        items, itemsResolved);
                items = itemsResolved;

                if (!foundAll && !dumpRemaining) {
                    continue;
                }

                outputs.put(output, items);
                dumpRecipe(out, output, items);
                dumped++;
            }

            if (dumped == 0) {
                if (dumpRemaining) {
                    break;
                } else {
                    dumpRemaining = true;
                }
            } else {
                dumpRemaining = false;
            }
        }

        for (ItemStack is : nonCraftables) {
            dumpRecipe(out, is, null);
        }
    }

    public static boolean reduceItemsList(List<ItemStack> craftables,
                                          List<ItemStack> nonCraftables, List<ItemStack> items,
                                          List<ItemStack> itemsResolved) {
        boolean foundAll = true;

        for (ItemStack is : items) {
            if (is == null) {
                continue;
            }

            if (!listContains(craftables, is))
                if (!listContains(nonCraftables, is)) {
                    nonCraftables.add(is);
                }

            ItemStack existing = getExistingInList(itemsResolved, is);

            if (existing != null) {
                if (existing != is) {
                    existing.stackSize++;
                }
            } else {
                ItemStack isc = is.copy();
                isc.stackSize = 1;
                itemsResolved.add(isc);
            }
        }

        return foundAll;
    }

    public static void dumpRecipe(
            OutputStreamWriter out,
            ItemStack output,
            List<ItemStack> items) throws IOException {
        out.write(output.getUnlocalizedName());
        out.write(";" + output.stackSize);
        out.write(";" + output.getItemDamage());

        if (items != null) {
            for (ItemStack is : items) {
                out.write(";" + is.getUnlocalizedName());
                out.write(";" + is.stackSize);
                out.write(";" + is.getItemDamage());
            }
        }

        out.write("\r\n");
    }

    private static List<ItemStack> getInputsListForRecipe(IRecipe recipe) {
        if (recipe instanceof ShapelessRecipes) {
            ShapelessRecipes sr = (ShapelessRecipes) recipe;
            return sr.recipeItems;
        } else if (recipe instanceof ShapedRecipes) {
            ShapedRecipes sr = (ShapedRecipes) recipe;
            return Arrays.asList(sr.recipeItems);
        }

        return null;
    }

    public static boolean compareItemStacks(ItemStack test, ItemStack stack) {
        int dmg = test.getItemDamage();
        return test.getItem() == stack.getItem() && (dmg < 0 || dmg == stack.getItemDamage());
    }

    public static boolean listContains(List<ItemStack> list, ItemStack stack) {
        if (stack == null) {
            return false;
        }

        for (ItemStack k : list) {
            if (compareItemStacks(k, stack)) {
                return true;
            }
        }

        return false;
    }

    public static ItemStack getExistingInList(List<ItemStack> list, ItemStack stack) {
        if (stack == null) {
            return null;
        }

        for (ItemStack k : list) {
            if (compareItemStacks(k, stack)) {
                return k;
            }
        }

        return null;
    }

    public static <OType> OType getFromMap(Map<ItemStack, OType> map, ItemStack stack) {
        if (stack == null) {
            return null;
        }

        for (ItemStack k : map.keySet()) {
            int dmg = k.getItemDamage();

            if (compareItemStacks(k, stack)) {
                return map.get(k);
            }
        }

        return null;
    }

    public static <OType> boolean stackIsInMap(Map<ItemStack, OType> map, ItemStack stack) {
        if (stack == null) {
            return false;
        }

        return getFromMap(map, stack) != null;
    }

    public static ItemStack findKeyForValue(Map<ItemStack, ItemStack> map, ItemStack stack) {
        if (stack == null) {
            return null;
        }

        for (ItemStack k : map.keySet()) {
            ItemStack v = map.get(k);

            if (compareItemStacks(v, stack)) {
                return k;
            }
        }

        return null;
    }

}
