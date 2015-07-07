package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.Utils;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.recipes.RecipeTools;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private static class ItemEssenceCollection extends ArrayList<ItemEssenceEntry> {

        public ItemEssenceCollection all(int amount) {
            for(ItemEssenceEntry e : this)
                e.all(amount);
            return this;
        }

        public ItemEssenceCollection fire(int amount) {
            for(ItemEssenceEntry e : this)
                e.fire(amount);
            return this;
        }

        public ItemEssenceCollection water(int amount) {
            for(ItemEssenceEntry e : this)
                e.water(amount);
            return this;
        }

        public ItemEssenceCollection air(int amount) {
            for(ItemEssenceEntry e : this)
                e.air(amount);
            return this;
        }

        public ItemEssenceCollection earth(int amount) {
            for(ItemEssenceEntry e : this)
                e.earth(amount);
            return this;
        }

        public ItemEssenceCollection light(int amount) {
            for(ItemEssenceEntry e : this)
                e.light(amount);
            return this;
        }

        public ItemEssenceCollection darkness(int amount) {
            for(ItemEssenceEntry e : this)
                e.darkness(amount);
            return this;
        }

        public ItemEssenceCollection life(int amount) {
            for(ItemEssenceEntry e : this)
                e.life(amount);
            return this;
        }

        public ItemEssenceCollection death(int amount) {
            for(ItemEssenceEntry e : this)
                e.death(amount);
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

    static final List<ItemEssenceEntry> stockEntries = new ArrayList<ItemEssenceEntry>();
    
    static void registerEntries()
    {
        //essences(Blocks.dirt).earth(3).life(1);
        //essences(Blocks.grass).earth(2).life(2);
        //essences(Blocks.cobblestone).earth(5);
        //essences(Blocks.stone).earth(10);
        //essences(Blocks.sand).earth(2).air(2);
        //essences(Blocks.gravel).earth(3).air(1);
        //essences(Blocks.clay).earth(3).water(1);
        //essences(Blocks.hardened_clay).earth(5).fire(1);
        //essences(Blocks.stained_hardened_clay).earth(5).fire(1);
        //essences(Blocks.log).life(16);
        //essences(Blocks.log2).life(16);
        //essences(Blocks.planks).life(4);
        //essences(Items.stick).life(1);
        //essences(Items.coal).fire(8);
        //essences(Blocks.coal_ore).fire(8).earth(2);
        //essences(Blocks.coal_block).fire(72).earth(8);

        //essences(Blocks.coal_block,0);
        //essences(Blocks.iron_block,0);
        //essences(Blocks.lapis_block,0);
        //essences(Blocks.brick_block,0);
        //essences(Blocks.quartz_ore,0);

        essences(Blocks.cactus).life(3);
        essences(Blocks.chest).earth(2).light(1);

        //essences(Items.dye);

        essences(Blocks.clay).earth(3).water(1);
        essences(Blocks.dirt).earth(3).life(1);
        essences(Blocks.gravel, 0).earth(3).air(1);
        essences(Blocks.sand, 0).earth(2).air(2);
        essences(Blocks.sand, 1).earth(2).air(2);
        essences(Blocks.obsidian, 0).earth(10).darkness(10);

        collection(
                essences(Blocks.yellow_flower),
                essences(Blocks.red_flower, 0),
                essences(Blocks.red_flower, 1),
                essences(Blocks.red_flower, 2),
                essences(Blocks.red_flower, 3),
                essences(Blocks.red_flower, 5),
                essences(Blocks.red_flower, 7)).life(1);

        collection(
            essences(Blocks.log, 0),
            essences(Blocks.log, 1),
            essences(Blocks.log, 2),
            essences(Blocks.log, 3),
            essences(Blocks.log, 4),
            essences(Blocks.log, 5)).life(4);

        //essences(Blocks.stone_slab, 0);
        //essences(Blocks.stone_slab, 1);
        //essences(Blocks.stone_slab, 5);
        //essences(Blocks.stone_slab, 7);
        //essences(Blocks.stone_slab2, 0);
        //essences(Blocks.wooden_slab,0);

        essences(Blocks.hay_block,0).earth(1).life(9).air(1);

        essences(Blocks.red_mushroom,0).earth(2).life(2);
        essences(Blocks.brown_mushroom, 0).earth(2).life(2);
        essences(Blocks.pumpkin,0).earth(1).life(3);
        essences(Blocks.sponge,1).water(4).life(2);
        essences(Blocks.vine, 0).life(2);

        //essences(Blocks.netherrack, 0);
        //essences(Blocks.nether_brick, 0);
        //essences(Blocks.quartz_block,0);

        //essences(Blocks.piston,0);
        //essences(Blocks.snow,0);
        //essences(Blocks.stonebrick,0);
        //essences(Blocks.tnt,0);

        //essences(Items.blaze_powder,0);
        //essences(Items.blaze_rod,0);

        //essences(Items.apple,0);
        //essences(Items.beef,0);
        //essences(Items.carrot,0);
        //essences(Items.chicken,0);
        //essences(Items.egg,0);

        //essences(Items.fish,0);
        //essences(Items.fish,1);

        //essences(Items.bone,0);
        //essences(Items.bowl,0);
        //essences(Items.clay_ball,0);
        //essences(Items.coal,0);
        //essences(Items.diamond,0);
        //essences(Items.emerald,0);
        //essences(Items.ender_pearl,0);
        //essences(Items.feather,0);
        //essences(Items.flint,0);
        //essences(Items.gold_nugget,0);
        //essences(Items.iron_ingot,0);
        //essences(Items.map,0);
        //essences(Items.melon,0);
        //essences(Items.milk_bucket,0);
        //essences(Items.mutton,0);
        //essences(Items.nether_star,0);
        //essences(Items.porkchop,0);
        //essences(Items.potato,0);
        //essences(Items.prismarine_crystals,0);
        //essences(Items.prismarine_shard,0);
        //essences(Items.rabbit_hide,0);
        //essences(Items.rabbit,0);
        //essences(Items.redstone,0);
        //essences(Items.reeds,0);
        //essences(Items.slime_ball,0);
        //essences(Items.snowball,0);
        //essences(Items.spider_eye,0);
        //essences(Items.stick,0);
        //essences(Items.string,0);
        //essences(Items.gunpowder,0);
        //essences(Items.wheat,0);
        //essences(Items.glowstone_dust,0);
    }

    private static ItemEssenceCollection collection(ItemEssenceEntry... entries)
    {
        ItemEssenceCollection collection = new ItemEssenceCollection();
        for(ItemEssenceEntry ee : entries)
        {
            collection.add(ee);
        }

        return collection;
    }

    private static ItemEssenceCollection essences(Item item)
    {
        List<ItemStack> subItems = new ArrayList<ItemStack>();
                
        item.getSubItems(item, CreativeTabs.tabAllSearch,subItems);

        ItemEssenceCollection collection = new ItemEssenceCollection();
        for(ItemStack is : subItems)
        {
            ItemEssenceEntry ee = new ItemEssenceEntry(is, new MagicAmounts());
            collection.add(ee);
            stockEntries.add(ee);
        }

        return collection;
    }

    private static ItemEssenceEntry essences(Block block) {
        ItemEssenceEntry ee = new ItemEssenceEntry(new ItemStack(block), new MagicAmounts());
        stockEntries.add(ee);
        return ee;
    }

    private static ItemEssenceEntry essences(Item item, int meta) {
        ItemEssenceEntry ee = new ItemEssenceEntry(new ItemStack(item, 1, meta), new MagicAmounts());
        stockEntries.add(ee);
        return ee;
    }

    private static ItemEssenceEntry essences(Block block, int meta) {
        ItemEssenceEntry ee = new ItemEssenceEntry(new ItemStack(block, 1, meta), new MagicAmounts());
        stockEntries.add(ee);
        return ee;
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
        registerEntries();
        RecipeTools.gatherRecipes();
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
