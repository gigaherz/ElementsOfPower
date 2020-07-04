package gigaherz.elementsofpower.database;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockConversions
{
    private static final List<ItemEssenceConversion> CONVERSIONS = Lists.newArrayList();

    public static void addStockConversions()
    {
        CONVERSIONS.clear();

        for (Element e : Element.values())
        {
            essences(e.getItem()).element(e, 8);
        }

        essences(Blocks.CACTUS).life(3);
        essences(Blocks.CHEST).earth(2).light(1);

        for (DyeColor color : DyeColor.values())
        {
            Item item = DyeItem.getItem(color);
            essences(item).earth(1).life(1);
        }
        essences(Items.INK_SAC).water(2).darkness(2);
        essences(Items.LAPIS_LAZULI).earth(8);
        essences(Items.BONE_MEAL).earth(1).death(1);

        essences(Blocks.CLAY).earth(4).water(4);
        essences(Items.BRICK).earth(1).fire(1);
        essences(Blocks.BRICKS).earth(4).fire(4);

        essences(Blocks.DIRT).earth(3).life(1);
        essences(Blocks.COARSE_DIRT).earth(3).life(1);
        essences(Blocks.GRAVEL).earth(3).air(1);
        essences(Blocks.SAND).earth(2).air(2);
        essences(Blocks.RED_SAND).earth(2).air(2);
        essences(Blocks.SANDSTONE).earth(8).air(8);
        essences(Blocks.RED_SANDSTONE).earth(8).air(8);
        essences(Blocks.SMOOTH_SANDSTONE).earth(8).air(8);
        essences(Blocks.SMOOTH_RED_SANDSTONE).earth(8).air(8);
        essences(Blocks.OBSIDIAN).earth(10).darkness(10);
        essences(Blocks.NETHERRACK).earth(1).fire(1);

        essences(Blocks.COBBLESTONE).earth(5);
        essences(Blocks.STONE).earth(10);
        essences(Blocks.SMOOTH_STONE).earth(10);
        essences(Blocks.DIORITE).earth(10);
        essences(Blocks.POLISHED_DIORITE).earth(10);
        essences(Blocks.ANDESITE).earth(10);
        essences(Blocks.POLISHED_ANDESITE).earth(10);
        essences(Blocks.GRANITE).earth(10);
        essences(Blocks.POLISHED_GRANITE).earth(10);

        essences(Blocks.WHITE_TERRACOTTA,
                Blocks.ORANGE_TERRACOTTA,
                Blocks.MAGENTA_TERRACOTTA,
                Blocks.LIGHT_BLUE_TERRACOTTA,
                Blocks.YELLOW_TERRACOTTA,
                Blocks.LIME_TERRACOTTA,
                Blocks.PINK_TERRACOTTA,
                Blocks.GRAY_TERRACOTTA,
                Blocks.LIGHT_GRAY_TERRACOTTA,
                Blocks.CYAN_TERRACOTTA,
                Blocks.PURPLE_TERRACOTTA,
                Blocks.BLUE_TERRACOTTA,
                Blocks.BROWN_TERRACOTTA,
                Blocks.GREEN_TERRACOTTA,
                Blocks.RED_TERRACOTTA,
                Blocks.BLACK_TERRACOTTA).earth(5).fire(1);

        essences(Blocks.TALL_GRASS).earth(2).life(2);
        essences(Blocks.GRASS).earth(2).life(2);
        essences(Blocks.GRASS_BLOCK).earth(3).life(2);
        essences(Blocks.PODZOL).earth(3).life(1).death(1);

        essences(
                Blocks.DANDELION,
                Blocks.POPPY,
                Blocks.BLUE_ORCHID,
                Blocks.ALLIUM,
                Blocks.AZURE_BLUET,
                Blocks.RED_TULIP,
                Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP,
                Blocks.PINK_TULIP,
                Blocks.OXEYE_DAISY,
                Blocks.CORNFLOWER,
                Blocks.LILY_OF_THE_VALLEY
        ).life(1);
        essences(Blocks.WITHER_ROSE).life(1).death(2);

        essences(
                Blocks.OAK_SAPLING,
                Blocks.BIRCH_SAPLING,
                Blocks.JUNGLE_SAPLING,
                Blocks.SPRUCE_SAPLING,
                Blocks.DARK_OAK_SAPLING,
                Blocks.ACACIA_SAPLING
        ).life(4);
        essences(
                Blocks.OAK_LOG,
                Blocks.BIRCH_LOG,
                Blocks.JUNGLE_LOG,
                Blocks.SPRUCE_LOG,
                Blocks.DARK_OAK_LOG,
                Blocks.ACACIA_LOG
        ).life(16);
        essences(
                Blocks.OAK_WOOD,
                Blocks.BIRCH_WOOD,
                Blocks.JUNGLE_WOOD,
                Blocks.SPRUCE_WOOD,
                Blocks.DARK_OAK_WOOD,
                Blocks.ACACIA_WOOD
        ).life(16);
        essences(
                Blocks.STRIPPED_OAK_LOG,
                Blocks.STRIPPED_BIRCH_LOG,
                Blocks.STRIPPED_JUNGLE_LOG,
                Blocks.STRIPPED_SPRUCE_LOG,
                Blocks.STRIPPED_DARK_OAK_LOG,
                Blocks.STRIPPED_ACACIA_LOG
        ).life(16);
        essences(
                Blocks.STRIPPED_OAK_WOOD,
                Blocks.STRIPPED_BIRCH_WOOD,
                Blocks.STRIPPED_JUNGLE_WOOD,
                Blocks.STRIPPED_SPRUCE_WOOD,
                Blocks.STRIPPED_DARK_OAK_WOOD,
                Blocks.STRIPPED_ACACIA_WOOD
        ).life(16);
        essences(
                Blocks.OAK_STAIRS,
                Blocks.BIRCH_STAIRS,
                Blocks.JUNGLE_STAIRS,
                Blocks.ACACIA_STAIRS,
                Blocks.DARK_OAK_STAIRS,
                Blocks.SPRUCE_STAIRS
        ).life(6);

        essences(Items.STICK).life(1);

        essences(Items.COAL, Items.CHARCOAL).fire(8);
        essences(Blocks.COAL_BLOCK).fire(72).earth(8);

        essences(Items.WHEAT).life(1);
        essences(Blocks.HAY_BLOCK).earth(1).life(9).air(1);

        essences(Blocks.RED_MUSHROOM).earth(2).life(2);
        essences(Blocks.BROWN_MUSHROOM).earth(2).life(2);
        essences(Blocks.PUMPKIN).earth(1).life(3);
        essences(Blocks.SPONGE).water(1).life(2);
        essences(Blocks.WET_SPONGE).water(4).life(2);
        essences(Blocks.VINE).life(2);

        essences(Items.NETHER_STAR).all(64);

        essences(Items.IRON_INGOT).earth(18);
        essences(Items.GOLD_INGOT).earth(18);
        essences(Items.GOLD_NUGGET).earth(2);

        essences(Items.BLAZE_ROD).fire(12).life(8);

        essences(
                Items.COD,
                Items.SALMON,
                Items.TROPICAL_FISH,
                Items.PUFFERFISH
        ).life(4).water(2);

        essences(Items.DIAMOND).earth(128);
        essences(Items.EMERALD).earth(100).life(50);
        essences(Items.QUARTZ).earth(100).light(50);

        essences(Gemstone.RUBY.getItem()).earth(100).fire(50);
        essences(Gemstone.SAPPHIRE.getItem()).earth(100).water(50);
        essences(Gemstone.CITRINE.getItem()).earth(100).air(50);
        essences(Gemstone.AGATE.getItem()).earth(100).earth(50);
        essences(Gemstone.QUARTZ.getItem()).earth(100).light(50);
        essences(Gemstone.SERENDIBITE.getItem()).earth(100).darkness(50);
        essences(Gemstone.EMERALD.getItem()).earth(100).life(50);
        essences(Gemstone.AMETHYST.getItem()).earth(100).death(50);
        essences(Gemstone.DIAMOND.getItem()).earth(128);

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

        essences(Items.SUGAR_CANE).life(4).water(4).earth(2);

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


        CONVERSIONS.forEach(ItemEssenceConversion::apply);
    }

    private static ItemEssenceCollection collection(ItemEssenceConversion... entries)
    {
        ItemEssenceCollection collection = new ItemEssenceCollection();

        Collections.addAll(collection, entries);

        return collection;
    }

    private static ItemEssenceCollection essences(IItemProvider... items)
    {
        ItemEssenceCollection collection = new ItemEssenceCollection();

        for (IItemProvider item : items)
        {
            ItemEssenceEntry ee = new ItemEssenceEntry(item, MagicAmounts.EMPTY);
            collection.add(ee);
            CONVERSIONS.add(ee);
        }

        return collection;
    }

    private static ItemEssenceEntry essences(IItemProvider itemProvider)
    {
        ItemEssenceEntry ee = new ItemEssenceEntry(itemProvider, MagicAmounts.EMPTY);
        CONVERSIONS.add(ee);
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
        Item item;
        MagicAmounts amounts;

        public ItemEssenceEntry(IItemProvider item, MagicAmounts amounts)
        {
            this.item = item.asItem();
            this.amounts = amounts;
        }

        @Override
        public void apply()
        {
            EssenceConversions.SERVER.addConversion(item, amounts);
        }

        @Override
        public ItemEssenceEntry all(int amount)
        {
            amounts = amounts.all(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry fire(int amount)
        {
            amounts = amounts.fire(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry water(int amount)
        {
            amounts = amounts.water(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry air(int amount)
        {
            amounts = amounts.air(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry earth(int amount)
        {
            amounts = amounts.earth(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry light(int amount)
        {
            amounts = amounts.light(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry darkness(int amount)
        {
            amounts = amounts.darkness(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry life(int amount)
        {
            amounts = amounts.life(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry death(int amount)
        {
            amounts = amounts.death(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry element(Element l, int amount)
        {
            amounts = amounts.add(l, amount);
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
