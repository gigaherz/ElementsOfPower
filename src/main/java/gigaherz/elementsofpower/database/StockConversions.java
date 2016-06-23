package gigaherz.elementsofpower.database;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gemstones.Element;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockConversions
{
    static final List<ItemEssenceConversion> stockEntries = Lists.newArrayList();

    public static void registerEssenceSources()
    {
        for (Element e : Element.values())
        { essences(ElementsOfPower.magicOrb, e.ordinal()).element(e, 8); }

        essences(Blocks.CACTUS).life(3);
        essences(Blocks.CHEST).earth(2).light(1);

        essences(Items.DYE, 0).water(2).darkness(2);
        essences(Items.DYE, 4).earth(8);
        essences(Items.DYE, 15).earth(1).death(1);
        essences(Items.DYE, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14).earth(1).life(1);

        essences(Items.CLAY_BALL).earth(1).water(1);
        essences(Blocks.CLAY).earth(4).water(4);
        essences(Items.BRICK).earth(1).fire(1);
        essences(Blocks.BRICK_BLOCK).earth(4).fire(4);

        essences(Blocks.DIRT, 0).earth(3).life(1);
        essences(Blocks.GRAVEL).earth(3).air(1);
        essences(Blocks.SAND, 0, 1).earth(2).air(2);
        essences(Blocks.SANDSTONE, 0, 1, 2).earth(8).air(8);
        essences(Blocks.OBSIDIAN).earth(10).darkness(10);
        essences(Blocks.NETHERRACK).earth(1).fire(1);

        essences(Blocks.COBBLESTONE).earth(5);
        essences(Blocks.STONE, 0, 1, 2, 3, 4, 5, 6).earth(10);
        essences(Blocks.HARDENED_CLAY).earth(5).fire(1);
        essences(Blocks.STAINED_HARDENED_CLAY).earth(5).fire(1);

        essences(Blocks.GRASS).earth(2).life(2);
        essences(Blocks.DIRT, 2).earth(3).life(2);

        collection(
                essences(Blocks.YELLOW_FLOWER),
                essences(Blocks.RED_FLOWER, 0),
                essences(Blocks.RED_FLOWER, 1),
                essences(Blocks.RED_FLOWER, 2),
                essences(Blocks.RED_FLOWER, 3),
                essences(Blocks.RED_FLOWER, 5),
                essences(Blocks.RED_FLOWER, 7)
        ).life(1);

        collection(
                essences(Blocks.LOG, 0, 1, 2, 3),
                essences(Blocks.LOG2, 0, 1)
        ).life(16);
        essences(Blocks.PLANKS, 0, 1, 2, 3, 4, 5).life(4);
        essences(Blocks.WOODEN_SLAB, 0, 1, 2, 3, 4, 5).life(2);
        collection(
                essences(Blocks.OAK_STAIRS),
                essences(Blocks.BIRCH_STAIRS),
                essences(Blocks.JUNGLE_STAIRS),
                essences(Blocks.ACACIA_STAIRS),
                essences(Blocks.DARK_OAK_STAIRS),
                essences(Blocks.SPRUCE_STAIRS)
        ).life(6);

        essences(Items.STICK).life(1);

        essences(Items.COAL, 0, 1).fire(8);
        essences(Blocks.COAL_BLOCK).fire(72).earth(8);

        essences(Items.WHEAT).life(1);
        essences(Blocks.HAY_BLOCK).earth(1).life(9).air(1);

        essences(Blocks.RED_MUSHROOM).earth(2).life(2);
        essences(Blocks.BROWN_MUSHROOM).earth(2).life(2);
        essences(Blocks.PUMPKIN).earth(1).life(3);
        essences(Blocks.SPONGE, 1).water(4).life(2);
        essences(Blocks.VINE).life(2);

        essences(Items.NETHER_STAR).all(64);

        essences(Items.IRON_INGOT).earth(18);
        essences(Items.GOLD_INGOT).earth(18);
        essences(Items.GOLD_NUGGET).earth(2);

        essences(Items.BLAZE_ROD).fire(12).life(8);

        essences(Items.FISH, 0, 1, 2, 3).life(4).water(2);

        essences(Items.DIAMOND).earth(128);
        essences(Items.EMERALD).earth(100).life(50);
        essences(Items.QUARTZ).earth(100).light(50);

        essences(ElementsOfPower.gemRuby).earth(100).fire(50);
        essences(ElementsOfPower.gemSapphire).earth(100).water(50);
        essences(ElementsOfPower.gemCitrine).earth(100).air(50);
        essences(ElementsOfPower.gemAgate).earth(100).earth(50);
        essences(ElementsOfPower.gemQuartz).earth(100).light(50);
        essences(ElementsOfPower.gemSerendibite).earth(100).darkness(50);
        essences(ElementsOfPower.gemEmerald).earth(100).life(50);
        essences(ElementsOfPower.gemAmethyst).earth(100).death(50);
        essences(ElementsOfPower.gemDiamond).earth(128);

        essences(Items.CLAY_BALL).earth(8).water(2);

        essences(Items.FEATHER).air(4).life(4);

        essences(Items.FLINT).earth(1);

        essences(Items.STRING).earth(1).life(1).air(1);

        essences(Items.SNOWBALL).water(1).air(1);

        essences(Items.APPLE).life(2).earth(1).air(1);
        essences(Items.BEEF).life(8);
        essences(Items.PORKCHOP).life(8);
        essences(Items.MUTTON).life(8);
        essences(Items.RABBIT).life(4);
        essences(Items.CARROT).life(2).earth(2);
        essences(Items.MELON).life(1).earth(1);
        essences(Items.CHICKEN).life(4).air(2);
        essences(Items.EGG).life(2).air(2).light(2);
        essences(Items.POTATO).life(2).earth(2);
        essences(Items.POISONOUS_POTATO).death(2).earth(2);

        essences(Items.REDSTONE).earth(4).light(4);

        essences(Items.MILK_BUCKET).life(2).water(4).earth(54);

        essences(Items.BONE).death(4);
        essences(Items.GUNPOWDER).death(2).fire(2).earth(2);

        essences(Items.REEDS).life(4).water(4).earth(2);

        //essences(Blocks.NETHER_BRICK);
        //essences(Blocks.QUARTZ_BLOCK);

        //essences(Items.ENDER_PEARL,0);
        //essences(Items.MAP,0);
        //essences(Items.MILK_BUCKET,0);
        //essences(Items.POTATO,0);
        //essences(Items.PRISMARINE_CRYSTALS,0);
        //essences(Items.PRISMARINE_SHARD,0);
        //essences(Items.RABBIT_HIDE,0);
        //essences(Items.SLIME_BALL,0);
        //essences(Items.SPIDER_EYE,0);
        //essences(Items.GLOWSTONE_DUST,0);


        stockEntries.forEach(ItemEssenceConversion::apply);
    }

    private static ItemEssenceCollection collection(ItemEssenceConversion... entries)
    {
        ItemEssenceCollection collection = new ItemEssenceCollection();

        Collections.addAll(collection, entries);

        return collection;
    }

    private static ItemEssenceCollection essences(Item item, int... metaValues)
    {
        List<ItemStack> subItems = Lists.newArrayList();

        if (metaValues.length == 0)
        {
            subItems.add(new ItemStack(item));
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
        Item item = Item.getItemFromBlock(block);
        assert item != null;
        return essences(item, itemMetaValues);
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

    private static ItemEssenceEntry essences(ItemStack stack)
    {
        ItemEssenceEntry ee = new ItemEssenceEntry(stack, new MagicAmounts());
        stockEntries.add(ee);
        return ee;
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

        ItemEssenceConversion element(Element l, int amount);

        void apply();
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
        public void apply()
        {
            EssenceConversions.addConversion(item, amounts);
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

        @Override
        public ItemEssenceEntry element(Element l, int amount)
        {
            amounts.element(l, amount);
            return this;
        }
    }

    private static class ItemEssenceCollection extends ArrayList<ItemEssenceConversion> implements ItemEssenceConversion
    {
        @Override
        public void apply()
        {
            this.forEach(ItemEssenceConversion::apply);
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


        @Override
        public ItemEssenceCollection element(Element l, int amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.element(l, amount);
            }
            return this;
        }
    }
}
