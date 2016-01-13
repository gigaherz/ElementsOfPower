package gigaherz.elementsofpower.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.recipes.RecipeTools;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class MagicDatabase
{
    static final List<ItemEssenceConversion> stockEntries = new ArrayList<>();

    public static Map<ItemStack, ItemStack> containerConversion = new HashMap<>();
    public static Map<ItemStack, MagicAmounts> containerCapacity = new HashMap<>();
    public static Map<ItemStack, MagicAmounts> itemEssences = new HashMap<>();
    public static Map<String, MagicAmounts> essenceOverrides = new HashMap<>();

    public final static String[] magicNames = {
            ElementsOfPower.MODID + ".element.fire",
            ElementsOfPower.MODID + ".element.water",
            ElementsOfPower.MODID + ".element.air",
            ElementsOfPower.MODID + ".element.earth",
            ElementsOfPower.MODID + ".element.light",
            ElementsOfPower.MODID + ".element.darkness",
            ElementsOfPower.MODID + ".element.life",
            ElementsOfPower.MODID + ".element.death",
    };

    public static String getMagicName(int i)
    {
        return StatCollector.translateToLocal(magicNames[i]);
    }

    public static void initialize()
    {
        registerContainerConversions();
        registerContainerCapacity();
        registerEssenceSources();
        loadConfigOverrides();
        applyOverrides();
    }

    public static void postInitialize()
    {
        RecipeTools.gatherRecipes();
        registerEssencesForRecipes();
    }

    static final Gson SERIALIZER = new GsonBuilder()
            .registerTypeAdapter(MagicAmounts.class, new MagicAmounts.Serializer()).create();

    private static void loadConfigOverrides()
    {
        try
        {
            Reader r = new FileReader(ElementsOfPower.overrides);
            Type type = new TypeToken<Map<String, MagicAmounts>>()
            {
            }.getType();

            Map<String, MagicAmounts> ovr = SERIALIZER.<Map<String, MagicAmounts>>fromJson(r, type);
            if (ovr != null)
            {
                essenceOverrides.putAll(ovr);
            }
        }
        catch (FileNotFoundException e)
        {
            saveConfigOverrides();
        }
    }

    private static void saveConfigOverrides()
    {
        try
        {
            Writer w = new FileWriter(ElementsOfPower.overrides);
            w.write(SERIALIZER.toJson(essenceOverrides));
            w.flush();
            w.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void addCustomOverride(ItemStack stack, MagicAmounts amounts)
    {
        loadConfigOverrides();

        String itemName = Item.itemRegistry.getNameForObject(stack.getItem()).toString();
        String entryName = String.format("%s@%d", itemName, stack.getMetadata());

        essenceOverrides.put(entryName, amounts);

        saveConfigOverrides();
    }

    private static void applyOverrides()
    {
        for (Map.Entry<String, MagicAmounts> e : essenceOverrides.entrySet())
        {
            String itemName;
            String entryName = e.getKey();
            int meta;
            int pos = entryName.lastIndexOf('@');
            if (pos <= 0)
            {
                itemName = entryName;
                meta = 0;
            }
            else
            {
                itemName = entryName.substring(0, pos);
                meta = Integer.parseInt(entryName.substring(pos + 1));
            }

            Item item = Item.itemRegistry.getObject(new ResourceLocation(itemName));

            ItemStack stack = new ItemStack(item, 1, meta);
            MagicAmounts m = e.getValue();
            if (Utils.stackIsInMap(itemEssences, stack))
            {
                ElementsOfPower.logger.error("Stack already inserted! " + stack.toString());
                continue;
            }

            itemEssences.put(stack, m);
        }
    }

    private static void registerEssencesForRecipes()
    {
        for (Map.Entry<ItemStack, List<ItemStack>> it : RecipeTools.itemSources.entrySet())
        {
            ItemStack output = it.getKey();
            List<ItemStack> inputs = it.getValue();

            int stackSize = output.stackSize;
            if (stackSize < 1)
            {
                ElementsOfPower.logger.warn("StackSize is invalid! " + output.toString());
                continue;
            }

            if (output.stackSize > 1)
            {
                output = output.copy();
                output.stackSize = 1;
            }

            boolean ma = itemHasEssence(output);
            if (ma)
                continue;

            boolean allFound = true;
            MagicAmounts am = new MagicAmounts();
            for (ItemStack b : inputs)
            {
                MagicAmounts m = getEssences(b, true);

                if (m == null || m.isEmpty())
                {
                    allFound = false;
                    break;
                }

                am.add(m);
            }

            if (!allFound)
                continue;

            if (stackSize > 1)
            {
                for (int i = 0; i < am.amounts.length; i++)
                {
                    am.amounts[i] /= stackSize;
                }
            }

            if (Utils.stackIsInMap(itemEssences, output))
            {
                ElementsOfPower.logger.error("Stack already inserted! " + output.toString());
                continue;
            }

            itemEssences.put(output, am);
        }
    }

    static void registerEssenceSources()
    {
        essences(Blocks.cactus).life(3);
        essences(Blocks.chest).earth(2).light(1);

        essences(Items.dye, 0).water(2).darkness(2);
        essences(Items.dye, 4).earth(8);
        essences(Items.dye, 15).earth(1).death(1);
        essences(Items.dye, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14).earth(1).life(1);

        essences(Items.clay_ball).earth(1).water(1);
        essences(Blocks.clay).earth(4).water(4);
        essences(Items.brick).earth(1).fire(1);
        essences(Blocks.brick_block).earth(4).fire(4);

        essences(Blocks.dirt).earth(3).life(1);
        essences(Blocks.gravel).earth(3).air(1);
        essences(Blocks.sand, 0, 1).earth(2).air(2);
        essences(Blocks.sandstone, 0, 1, 2).earth(8).air(8);
        essences(Blocks.obsidian).earth(10).darkness(10);
        essences(Blocks.netherrack).earth(1).fire(1);

        essences(Blocks.cobblestone).earth(5);
        essences(Blocks.stone, 0, 1, 2, 3, 4, 5, 6).earth(10);
        essences(Blocks.hardened_clay).earth(5).fire(1);
        essences(Blocks.stained_hardened_clay).earth(5).fire(1);

        essences(Blocks.grass).earth(2).life(2);

        collection(
                essences(Blocks.yellow_flower),
                essences(Blocks.red_flower, 0),
                essences(Blocks.red_flower, 1),
                essences(Blocks.red_flower, 2),
                essences(Blocks.red_flower, 3),
                essences(Blocks.red_flower, 5),
                essences(Blocks.red_flower, 7)).life(1);

        essences(Blocks.log, 0, 1, 2, 3).life(16);
        essences(Blocks.log2, 0, 1).life(16);
        essences(Blocks.planks).life(4);
        essences(Items.stick).life(1);

        essences(Items.coal, 0, 1).fire(8);
        essences(Blocks.coal_block).fire(72).earth(8);

        essences(Items.wheat).life(1);
        essences(Blocks.hay_block).earth(1).life(9).air(1);

        essences(Blocks.red_mushroom, 0).earth(2).life(2);
        essences(Blocks.brown_mushroom, 0).earth(2).life(2);
        essences(Blocks.pumpkin, 0).earth(1).life(3);
        essences(Blocks.sponge, 1).water(4).life(2);
        essences(Blocks.vine, 0).life(2);

        essences(Items.nether_star, 0).all(64);

        //essences(Blocks.coal_ore).fire(8).earth(2);
        //essences(Blocks.quartz_ore,0);

        //essences(Blocks.iron_block);
        //essences(Blocks.nether_brick);
        //essences(Blocks.quartz_block);

        //essences(Blocks.stone_slab, 0);
        //essences(Blocks.stone_slab, 1);
        //essences(Blocks.stone_slab, 5);
        //essences(Blocks.stone_slab, 7);
        //essences(Blocks.stone_slab2, 0);
        //essences(Blocks.wooden_slab,0);

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
        //essences(Items.string,0);
        //essences(Items.gunpowder,0);
        //essences(Items.glowstone_dust,0);

        for (ItemEssenceConversion source : stockEntries)
        {
            source.putInto(itemEssences);
        }
    }

    private static ItemEssenceCollection collection(ItemEssenceConversion... entries)
    {
        ItemEssenceCollection collection = new ItemEssenceCollection();

        Collections.addAll(collection, entries);

        return collection;
    }

    private static ItemEssenceCollection essences(Item item, int... metaValues)
    {
        List<ItemStack> subItems = new ArrayList<>();

        if (metaValues.length == 0)
        {
            subItems.add(new ItemStack(item, 1));
        }
        else
        {
            for (int v : metaValues)
            {
                subItems.add(new ItemStack(item, 1, v));
            }
        }

        ItemEssenceCollection collection = new ItemEssenceCollection();
        for (ItemStack is : subItems)
        {
            ItemEssenceEntry ee = new ItemEssenceEntry(is, new MagicAmounts());
            collection.add(ee);
            stockEntries.add(ee);
        }

        return collection;
    }

    private static ItemEssenceCollection essences(Block block, int... itemMetaValues)
    {
        return essences(Item.getItemFromBlock(block), itemMetaValues);
    }

    private static ItemEssenceEntry essences(Item item, int meta)
    {
        ItemEssenceEntry ee = new ItemEssenceEntry(new ItemStack(item, 1, meta), new MagicAmounts());
        stockEntries.add(ee);
        return ee;
    }

    private static ItemEssenceEntry essences(Block block, int meta)
    {
        ItemEssenceEntry ee = new ItemEssenceEntry(new ItemStack(block, 1, meta), new MagicAmounts());
        stockEntries.add(ee);
        return ee;
    }

    private static void registerContainerCapacity()
    {
        containerCapacity.put(new ItemStack(Items.dye, 1, 4), new MagicAmounts().all(10));
        containerCapacity.put(new ItemStack(Items.emerald, 1), new MagicAmounts().all(50));
        containerCapacity.put(new ItemStack(Items.diamond, 1), new MagicAmounts().all(100));

        containerCapacity.put(ElementsOfPower.containerLapis, new MagicAmounts().all(10));
        containerCapacity.put(ElementsOfPower.containerEmerald, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.containerDiamond, new MagicAmounts().all(100));

        containerCapacity.put(ElementsOfPower.wandLapis, new MagicAmounts().all(10));
        containerCapacity.put(ElementsOfPower.wandEmerald, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.wandDiamond, new MagicAmounts().all(100));

        containerCapacity.put(ElementsOfPower.staffLapis, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.staffEmerald, new MagicAmounts().all(250));
        containerCapacity.put(ElementsOfPower.staffDiamond, new MagicAmounts().all(500));

        containerCapacity.put(ElementsOfPower.ringLapis, new MagicAmounts().all(25));
        containerCapacity.put(ElementsOfPower.ringEmerald, new MagicAmounts().all(100));
        containerCapacity.put(ElementsOfPower.ringDiamond, new MagicAmounts().all(250));
    }

    private static void registerContainerConversions()
    {
        containerConversion.put(new ItemStack(Items.dye, 1, 4), ElementsOfPower.containerLapis);
        containerConversion.put(new ItemStack(Items.emerald, 1), ElementsOfPower.containerEmerald);
        containerConversion.put(new ItemStack(Items.diamond, 1), ElementsOfPower.containerDiamond);
    }

    public static boolean itemContainsMagic(ItemStack stack)
    {
        if (isInfiniteContainer(stack))
            return true;

        MagicAmounts amounts = getContainedMagic(stack);

        return amounts != null && !amounts.isEmpty();
    }

    public static boolean canItemContainMagic(ItemStack stack)
    {
        if (stack.stackSize != 1)
        {
            stack = stack.copy();
            stack.stackSize = 1;
        }
        return Utils.stackIsInMap(containerCapacity, stack);
    }

    public static MagicAmounts getMagicLimits(ItemStack stack)
    {
        if (stack.stackSize != 1)
        {
            return null;
        }

        MagicAmounts m = Utils.getFromMap(containerCapacity, stack);
        if (m == null)
            return null;

        return m.copy();
    }

    public static boolean itemHasEssence(ItemStack stack)
    {
        if (stack.stackSize > 1)
        {
            stack = stack.copy();
            stack.stackSize = 1;
        }
        return Utils.stackIsInMap(itemEssences, stack);
    }

    public static MagicAmounts getEssences(ItemStack stack, boolean wholeStack)
    {
        int stackSize = stack.stackSize;
        if (stackSize > 1)
        {
            stack = stack.copy();
            stack.stackSize = 1;
        }
        MagicAmounts m = Utils.getFromMap(itemEssences, stack);
        if (m == null)
            return null;

        m = m.copy();

        if (stackSize > 1 && wholeStack)
        {
            for (int i = 0; i < m.amounts.length; i++)
            {
                m.amounts[i] *= stackSize;
            }
        }

        return m;
    }

    public static boolean isInfiniteContainer(ItemStack stack)
    {
        Item item = stack.getItem();
        return item instanceof ItemMagicContainer
                && ((ItemMagicContainer) item).isInfinite(stack);
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

        if (isInfiniteContainer(output))
            return new MagicAmounts().all(999);

        if (!(output.getItem() instanceof ItemMagicContainer))
            return null;

        NBTTagCompound nbt = output.getTagCompound();

        if (nbt == null)
        {
            return null;
        }

        MagicAmounts amounts = new MagicAmounts();
        float max = 0;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            try
            {
                float amount = nbt.getFloat("" + i);

                if (amount > max)
                {
                    max = amount;
                }

                amounts.amounts[i] = amount;
            }
            catch (NumberFormatException ex)
            {
                throw new ReportedException(new CrashReport("Exception while parsing NBT magic infromation", ex));
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

        if (isInfiniteContainer(output))
            return output;

        if (amounts != null)
        {
            if (amounts.isEmpty())
            {
                amounts = null;
            }
        }

        if (amounts != null)
        {
            NBTTagCompound nbt = output.getTagCompound();

            if (nbt == null)
            {
                if (Utils.stackIsInMap(containerConversion, output))
                {
                    output = Utils.getFromMap(containerConversion, output).copy();
                }

                nbt = new NBTTagCompound();
                output.setTagCompound(nbt);
            }

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                nbt.setFloat("" + i, amounts.amounts[i]);
            }

            return output;
        }
        else
        {
            output.setTagCompound(null);

            ItemStack is = Utils.findKeyForValue(containerConversion, output);
            if (is != null)
            {
                output = is.copy();
            }

            return output;
        }
    }

    public static boolean isContainerFull(ItemStack stack)
    {
        MagicAmounts limits = getMagicLimits(stack);
        MagicAmounts amounts = getContainedMagic(stack);

        if (amounts == null)
            return false;

        if (limits == null)
            return true;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts.amounts[i] < limits.amounts[i])
                return false;
        }

        return true;
    }

    public static boolean canTransferAnything(ItemStack stack, MagicAmounts self)
    {
        if (isInfiniteContainer(stack))
            return false;

        MagicAmounts limits = MagicDatabase.getMagicLimits(stack);
        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);

        if (limits == null)
            return true;

        if (amounts == null)
            return true;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (self.amounts[i] > 0 && amounts.amounts[i] < limits.amounts[i])
                return true;
        }

        return false;
    }

    public interface ItemEssenceConversion
    {
        ItemEssenceConversion all(int amount);

        ItemEssenceConversion fire(int amount);

        ItemEssenceConversion water(int amount);

        ItemEssenceConversion air(int amount);

        ItemEssenceConversion earth(int amount);

        ItemEssenceConversion light(int amount);

        ItemEssenceConversion darkness(int amount);

        ItemEssenceConversion life(int amount);

        ItemEssenceConversion death(int amount);

        void putInto(Map<ItemStack, MagicAmounts> essences);
    }

    private static class ItemEssenceEntry implements ItemEssenceConversion
    {
        ItemStack item;
        MagicAmounts amounts;

        public ItemEssenceEntry(ItemStack item, MagicAmounts amounts)
        {
            this.item = item;
            this.amounts = amounts;
        }

        @Override
        public void putInto(Map<ItemStack, MagicAmounts> essences)
        {
            essences.put(item, amounts);
        }

        @Override
        public ItemEssenceEntry all(int amount)
        {
            amounts.all(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry fire(int amount)
        {
            amounts.fire(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry water(int amount)
        {
            amounts.water(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry air(int amount)
        {
            amounts.air(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry earth(int amount)
        {
            amounts.earth(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry light(int amount)
        {
            amounts.light(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry darkness(int amount)
        {
            amounts.darkness(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry life(int amount)
        {
            amounts.life(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry death(int amount)
        {
            amounts.death(amount);
            return this;
        }
    }

    private static class ItemEssenceCollection extends ArrayList<ItemEssenceConversion> implements ItemEssenceConversion
    {
        @Override
        public void putInto(Map<ItemStack, MagicAmounts> essences)
        {
            for (ItemEssenceConversion c : this)
            {
                c.putInto(essences);
            }
        }

        @Override
        public ItemEssenceCollection all(int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.all(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection fire(int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.fire(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection water(int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.water(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection air(int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.air(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection earth(int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.earth(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection light(int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.light(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection darkness(int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.darkness(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection life(int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.life(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection death(int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.death(amount);
            }
            return this;
        }
    }
}
