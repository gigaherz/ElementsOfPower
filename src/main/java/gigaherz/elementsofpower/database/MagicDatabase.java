package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.Utils;
import gigaherz.elementsofpower.items.ItemWand;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import java.util.HashMap;
import java.util.Map;

public class MagicDatabase {
    private static class ItemEssenceEntry {
        ItemStack item;
        MagicAmounts amounts;

        public ItemEssenceEntry(ItemStack item, MagicAmounts amounts) {
            this.item = item;
            this.amounts = amounts;
        }

        public ItemEssenceEntry all(int amount) {
            amounts.all(amount);
            return this;
        }

        public ItemEssenceEntry fire(int amount) {
            amounts.fire(amount);
            return this;
        }

        public ItemEssenceEntry water(int amount) {
            amounts.water(amount);
            return this;
        }

        public ItemEssenceEntry air(int amount) {
            amounts.air(amount);
            return this;
        }

        public ItemEssenceEntry earth(int amount) {
            amounts.earth(amount);
            return this;
        }

        public ItemEssenceEntry light(int amount) {
            amounts.light(amount);
            return this;
        }

        public ItemEssenceEntry darkness(int amount) {
            amounts.darkness(amount);
            return this;
        }

        public ItemEssenceEntry life(int amount) {
            amounts.life(amount);
            return this;
        }

        public ItemEssenceEntry death(int amount) {
            amounts.death(amount);
            return this;
        }
    }

    public static Map<ItemStack, ItemStack> containerConversion = new HashMap<ItemStack, ItemStack>();
    public static Map<ItemStack, MagicAmounts> containerCapacity = new HashMap<ItemStack, MagicAmounts>();
    public static Map<ItemStack, MagicAmounts> itemEssences = new HashMap<ItemStack, MagicAmounts>();

    public final static String[] magicNames = {
            "element.fire",
            "element.water",
            "element.air",
            "element.earth",
            "element.light",
            "element.darkness",
            "element.life",
            "element.death",
    };

    static final ItemEssenceEntry[] stockEntries = {

            essences(Blocks.dirt).earth(3).life(1),
            essences(Blocks.grass).earth(2).life(2),
            essences(Blocks.cobblestone).earth(5),
            essences(Blocks.stone).earth(10),
            essences(Blocks.sand).earth(2).air(2),
            essences(Blocks.gravel).earth(3).air(1),
            essences(Blocks.clay).earth(3).water(1),
            essences(Blocks.hardened_clay).earth(5).fire(1),
            essences(Blocks.stained_hardened_clay).earth(5).fire(1),
            essences(Blocks.log).life(16),
            essences(Blocks.log2).life(16),
            essences(Blocks.planks).life(4),
            essences(Items.stick).life(1),
            essences(Items.coal).fire(8),
            essences(Blocks.coal_ore).fire(8).earth(2),
            essences(Blocks.coal_block).fire(72).earth(8)
    };

    private static ItemEssenceEntry essences(Item item) {
        return new ItemEssenceEntry(new ItemStack(item), new MagicAmounts());
    }

    private static ItemEssenceEntry essences(Block block) {
        return new ItemEssenceEntry(new ItemStack(block), new MagicAmounts());
    }

    private static ItemEssenceEntry essences(Item item, int meta) {
        return new ItemEssenceEntry(new ItemStack(item, 1, meta), new MagicAmounts());
    }

    public static void initialize() {
        registerContainerConversions();
        registerContainerCapacity();
        registerEssenceSources();
    }

    private static void registerContainerCapacity() {
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
    }

    private static void registerContainerConversions() {
        containerConversion.put(new ItemStack(Items.dye, 1, 4), new ItemStack(ElementsOfPower.magicContainer, 1, 0));
        containerConversion.put(new ItemStack(Items.emerald, 1), new ItemStack(ElementsOfPower.magicContainer, 1, 1));
        containerConversion.put(new ItemStack(Items.diamond, 1), new ItemStack(ElementsOfPower.magicContainer, 1, 2));
    }

    private static void registerEssenceSources() {
        for (ItemEssenceEntry source : stockEntries) {
            itemEssences.put(source.item, source.amounts);
        }
    }

    public static void postInitialize() {
        Utils.dumpAllRecipes();
        Utils.dumpAllItems();
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

        return Utils.stackIsInMap(containerCapacity, stack);
    }

    public static MagicAmounts getMagicLimits(ItemStack stack) {
        if (stack.stackSize > 1) {
            return null;
        }

        return Utils.getFromMap(containerCapacity, stack);
    }

    public static boolean itemHasEssence(ItemStack stack) {
        return Utils.stackIsInMap(itemEssences, stack);
    }

    public static MagicAmounts getEssences(ItemStack stack) {
        return Utils.getFromMap(itemEssences, stack);
    }

    public static MagicAmounts getContainedMagic(ItemStack output) {
        if (output == null) {
            return null;
        }

        if (output.stackSize != 1) {
            return null;
        }

        if (output.getItem() instanceof ItemWand) {
            int meta = output.getMetadata();
            if (meta == 3 || meta == 7)
                return new MagicAmounts().all(999);
        }

        NBTTagCompound nbt = output.getTagCompound();

        if (nbt == null) {
            return null;
        }

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

        Item item = output.getItem();
        if (item instanceof ItemWand) {
            if(((ItemWand)item).isCreative(output))
                return output;
        }

        if (amounts != null) {
            if (amounts.isEmpty()) {
                amounts = null;
            }
        }

        if (amounts != null) {
            NBTTagCompound nbt = output.getTagCompound();

            if (nbt == null) {
                if (Utils.stackIsInMap(containerConversion, output)) {
                    output = Utils.getFromMap(containerConversion, output).copy();
                }

                // output.setItemName(par1Str)
                nbt = new NBTTagCompound();
                output.setTagCompound(nbt);
            }

            for (int i = 0; i < 8; i++) {
                nbt.setInteger("" + i, amounts.amounts[i]);
            }

            return output;
        } else {
            output.setTagCompound(null);
            ItemStack is = Utils.findKeyForValue(containerConversion, output);

            if (is != null) {
                output = is.copy();
            }

            return output;
        }
    }

    public static String getMagicName(int i) {
        return StatCollector.translateToLocal(magicNames[i]);
    }

}
