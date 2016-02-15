package gigaherz.elementsofpower.database;

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
    static final List<ItemEssenceConversion> stockEntries = new ArrayList<>();

    public static void registerEssenceSources()
    {
        for (Element e : Element.values())
        { essences(ElementsOfPower.magicOrb, e.ordinal()).element(e, 8); }

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

        essences(Blocks.dirt, 0).earth(3).life(1);
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
        essences(Blocks.dirt, 2).earth(3).life(2);

        collection(
                essences(Blocks.yellow_flower),
                essences(Blocks.red_flower, 0),
                essences(Blocks.red_flower, 1),
                essences(Blocks.red_flower, 2),
                essences(Blocks.red_flower, 3),
                essences(Blocks.red_flower, 5),
                essences(Blocks.red_flower, 7)
        ).life(1);

        collection(
                essences(Blocks.log, 0, 1, 2, 3),
                essences(Blocks.log2, 0, 1)
        ).life(16);
        essences(Blocks.planks, 0, 1, 2, 3, 4, 5).life(4);
        essences(Blocks.wooden_slab, 0, 1, 2, 3, 4, 5).life(2);
        collection(
                essences(Blocks.oak_stairs),
                essences(Blocks.birch_stairs),
                essences(Blocks.jungle_stairs),
                essences(Blocks.acacia_stairs),
                essences(Blocks.dark_oak_stairs),
                essences(Blocks.spruce_stairs)
        ).life(6);

        essences(Items.stick).life(1);

        essences(Items.coal, 0, 1).fire(8);
        essences(Blocks.coal_block).fire(72).earth(8);

        essences(Items.wheat).life(1);
        essences(Blocks.hay_block).earth(1).life(9).air(1);

        essences(Blocks.red_mushroom).earth(2).life(2);
        essences(Blocks.brown_mushroom).earth(2).life(2);
        essences(Blocks.pumpkin).earth(1).life(3);
        essences(Blocks.sponge, 1).water(4).life(2);
        essences(Blocks.vine).life(2);

        essences(Items.nether_star).all(64);

        essences(Items.iron_ingot).earth(18);
        essences(Items.gold_ingot).earth(18);
        essences(Items.gold_nugget).earth(2);

        essences(Items.blaze_rod).fire(12).life(8);

        essences(Items.fish, 0, 1, 2, 3).life(4).water(2);

        essences(Items.diamond).earth(128);
        essences(Items.emerald).earth(100).life(50);

        essences(Items.clay_ball).earth(8).water(2);

        essences(Items.feather).air(4).life(4);

        essences(Items.flint).earth(1);

        essences(Items.string).earth(1).life(1).air(1);

        essences(Items.snowball).water(1).air(1);

        essences(Items.apple).life(2).earth(1).air(1);
        essences(Items.beef).life(8);
        essences(Items.porkchop).life(8);
        essences(Items.mutton).life(8);
        essences(Items.rabbit).life(4);
        essences(Items.carrot).life(2).earth(2);
        essences(Items.melon).life(1).earth(1);
        essences(Items.chicken).life(4).air(2);
        essences(Items.egg).life(2).air(2).light(2);
        essences(Items.potato).life(2).earth(2);
        essences(Items.poisonous_potato).death(2).earth(2);

        essences(Items.redstone).earth(4).light(4);

        essences(Items.milk_bucket).life(2).water(4).earth(54);

        essences(Items.bone).death(4);
        essences(Items.gunpowder).death(2).fire(2).earth(2);

        essences(Items.reeds).life(4).water(4).earth(2);

        //essences(Blocks.nether_brick);
        //essences(Blocks.quartz_block);

        //essences(Items.ender_pearl,0);
        //essences(Items.map,0);
        //essences(Items.milk_bucket,0);
        //essences(Items.potato,0);
        //essences(Items.prismarine_crystals,0);
        //essences(Items.prismarine_shard,0);
        //essences(Items.rabbit_hide,0);
        //essences(Items.slime_ball,0);
        //essences(Items.spider_eye,0);
        //essences(Items.glowstone_dust,0);

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
        List<ItemStack> subItems = new ArrayList<>();

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
