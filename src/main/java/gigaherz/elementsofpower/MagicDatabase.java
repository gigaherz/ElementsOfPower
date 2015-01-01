package gigaherz.elementsofpower;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

public class MagicDatabase {
    public static Map<ItemStack, ItemStack> containerConversion = new HashMap<ItemStack, ItemStack>();
    public static Map<ItemStack, MagicAmounts> containerCapacity = new HashMap<ItemStack, MagicAmounts>();
    public static Map<ItemStack, MagicAmounts> itemEssences = new HashMap<ItemStack, MagicAmounts>();

    public static final Map<Integer, String> magicNames = new HashMap<Integer, String>();

    public static void preInitialize() {
        magicNames.put(1 << 0, "Fire");
        magicNames.put(1 << 1, "Water");
        magicNames.put(1 << 2, "Air");
        magicNames.put(1 << 3, "Earth");
        magicNames.put(1 << 4, "Light");
        magicNames.put(1 << 5, "Darkness");
        magicNames.put(1 << 6, "Life");
        magicNames.put(1 << 7, "Death");
    }

    public static void initialize() {
        containerConversion.put(new ItemStack(Items.dye, 1, 4), new ItemStack(ElementsOfPower.magicContainer, 1, 0));
        containerConversion.put(new ItemStack(Items.emerald, 1), new ItemStack(ElementsOfPower.magicContainer, 1, 1));
        containerConversion.put(new ItemStack(Items.diamond, 1), new ItemStack(ElementsOfPower.magicContainer, 1, 2));
        containerCapacity.put(new ItemStack(Items.dye, 1, 4), new MagicAmounts().all(10));
        containerCapacity.put(new ItemStack(Items.emerald, 1), new MagicAmounts().all(50));
        containerCapacity.put(new ItemStack(Items.diamond, 1), new MagicAmounts().all(100));
        containerCapacity.put(new ItemStack(ElementsOfPower.magicContainer, 1, 0), new MagicAmounts().all(10));
        containerCapacity.put(new ItemStack(ElementsOfPower.magicContainer, 1, 1), new MagicAmounts().all(50));
        containerCapacity.put(new ItemStack(ElementsOfPower.magicContainer, 1, 2), new MagicAmounts().all(100));
        containerCapacity.put(ElementsOfPower.wandLapis, new MagicAmounts().all(10));
        containerCapacity.put(ElementsOfPower.wandEmerald, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.wandDiamond, new MagicAmounts().all(100));
        containerCapacity.put(ElementsOfPower.staffLapis, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.staffEmerald, new MagicAmounts().all(250));
        containerCapacity.put(ElementsOfPower.staffDiamond, new MagicAmounts().all(500));
        itemEssences.put(new ItemStack(Blocks.dirt, 1), new MagicAmounts().earth(1));
        itemEssences.put(new ItemStack(Blocks.grass, 1), new MagicAmounts().earth(1).life(1));
        itemEssences.put(new ItemStack(Blocks.cobblestone, 1), new MagicAmounts().earth(5));
        itemEssences.put(new ItemStack(Blocks.stone, 1), new MagicAmounts().earth(10));
    }

    public static void postInitialize() {
    }

    private static boolean reduceItemsList(List<ItemStack> craftables,
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

    private static boolean compareItemStacks(ItemStack test, ItemStack stack) {
        int dmg = test.getItemDamage();
        return test.getItem() == stack.getItem() && (dmg < 0 || dmg == stack.getItemDamage());
    }

    private static ItemStack findKeyForValue(Map<ItemStack, ItemStack> map, ItemStack stack) {
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

    private static boolean listContains(List<ItemStack> list, ItemStack stack) {
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

    private static ItemStack getExistingInList(List<ItemStack> list, ItemStack stack) {
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

    private static <OType> OType getFromMap(Map<ItemStack, OType> map, ItemStack stack) {
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

    private static <OType> boolean stackIsInMap(Map<ItemStack, OType> map, ItemStack stack) {
        if (stack == null) {
            return false;
        }

        return getFromMap(map, stack) != null;
    }

    public static boolean itemContainsMagic(ItemStack stack) {
        MagicAmounts amounts = getContainedMagic(stack);

        if (amounts == null) {
            return false;
        }

        return !amounts.isEmpty();
    }

    public static boolean canItemContainMagic(ItemStack stack) {
        if (stack.stackSize > 1) {
            return false;
        }

        return stackIsInMap(containerCapacity, stack);
    }

    public static MagicAmounts getMagicLimits(ItemStack stack) {
        if (stack.stackSize > 1) {
            return null;
        }

        return getFromMap(containerCapacity, stack);
    }

    public static boolean itemHasEssence(ItemStack stack) {
        return stackIsInMap(itemEssences, stack);
    }

    public static MagicAmounts getEssences(ItemStack stack) {
        return getFromMap(itemEssences, stack);
    }

    public static MagicAmounts getContainedMagic(ItemStack output) {
        if (output == null) {
            return null;
        }

        if (output.stackSize != 1) {
            return null;
        }

        NBTTagCompound nbt = output.getTagCompound();

        if (nbt == null) {
            return null;
        }

        NBTTagCompound tag = nbt.getCompoundTag("magicContained");
        MagicAmounts amounts = new MagicAmounts();
        int max = 0;

        for (int i = 0; i < 8; i++) {
            try {
                int amount = nbt.getInteger("" + i);

                if (amount > max) {
                    max = amount;
                }

                amounts.amounts[i] = amount;
            } catch (NumberFormatException ex) {
                continue;
            }
        }

        if (max > 0) {
            return amounts;
        }

        return null;
    }

    public static ItemStack setContainedMagic(ItemStack output, MagicAmounts amounts) {
        if (output == null) {
            return null;
        }

        if (output.stackSize != 1) {
            return null;
        }

        if (amounts != null) {
            if (amounts.isEmpty()) {
                amounts = null;
            }
        }

        if (amounts != null) {
            NBTTagCompound nbt = output.getTagCompound();

            if (nbt == null) {
                if (stackIsInMap(containerConversion, output)) {
                    output = getFromMap(containerConversion, output).copy();
                }

                // output.setItemName(par1Str)
                nbt = new NBTTagCompound();
                output.setTagCompound(nbt);
            }

            NBTTagCompound tag = nbt.getCompoundTag("magicContained");
            int max = 0;

            for (int i = 0; i < 8; i++) {
                nbt.setInteger("" + i, amounts.amounts[i]);
            }

            return output;
        } else {
            output.setTagCompound(null);
            ItemStack is = findKeyForValue(containerConversion, output);

            if (is != null) {
                output = is.copy();
            }

            return output;
        }
    }

    public static String getMagicName(int i) {
        return magicNames.get(1 << i);
    }

    public static String getMagicName(EnumSet<MagicEssences> bits) {
        int n = 0;

        for (int i = 0; i < 8; i++) {
            if (bits.contains(MagicEssences.values()[i])) {
                n |= 1 << i;
            }
        }

        return magicNames.get(n);
    }

    public static String getMagicNameCombined(int i) {
        return magicNames.get(i);
    }
}
