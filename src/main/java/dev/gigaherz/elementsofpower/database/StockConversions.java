package dev.gigaherz.elementsofpower.database;

import com.google.common.collect.Maps;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StockConversions
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static final Map<Item, ItemEssenceConversion> CONVERSIONS = Maps.newHashMap();

    public static void addStockConversions(BiFunction<ResourceLocation, List<Item>, List<Item>> tagGetter, BiConsumer<Item, MagicAmounts> consumer)
    {
        CONVERSIONS.clear();

        for (Element e : Element.values)
        {
            essences(e.getOrb()).element(e, 8);
            essences(e.getItem()).element(e, 8).life(2);
        }

        for (Gemstone e : Gemstone.values)
        {
            ItemEssenceCollection gem = essences(e.getTagItems()).earth(1);
            if (e.getElement() != null)
                gem.element(e.getElement(), 1 / 8.0f);

            if (e.generateCustomOre())
            {
                ItemEssenceEntry ore = essences(e.getOre()).earth(8);
                if (e.getElement() != null)
                    ore.element(e.getElement(), 1);
            }

            if (e.generateCustomBlock())
            {
                ItemEssenceEntry block = essences(e.getBlock()).earth(19);
                if (e.getElement() != null)
                    block.element(e.getElement(), 1);
            }
        }

        essences(Blocks.CACTUS).life(3);
        essences(Blocks.CHEST).earth(2).light(1);

        for (DyeColor color : DyeColor.values())
        {
            Item item = DyeItem.byColor(color);
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
        essences(Blocks.BLACKSTONE).earth(5).darkness(5);
        fromTag(tagGetter, "forge:stone",
                Arrays.asList(
                        Items.ANDESITE,
                        Items.DIORITE,
                        Items.GRANITE,
                        Items.INFESTED_STONE,
                        Items.STONE,
                        Items.POLISHED_ANDESITE,
                        Items.POLISHED_DIORITE,
                        Items.POLISHED_GRANITE
                ),
                items -> essences(items).earth(10)
        );

        essences(
                Items.INFESTED_STONE,
                Items.INFESTED_COBBLESTONE,
                Items.INFESTED_STONE_BRICKS,
                Items.INFESTED_MOSSY_STONE_BRICKS,
                Items.INFESTED_CRACKED_STONE_BRICKS,
                Items.INFESTED_CHISELED_STONE_BRICKS
        ).earth(5).life(5);

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

        fromTag(tagGetter, "minecraft:small_flowers",
                Arrays.asList(
                        Items.DANDELION,
                        Items.POPPY,
                        Items.BLUE_ORCHID,
                        Items.ALLIUM,
                        Items.AZURE_BLUET,
                        Items.RED_TULIP,
                        Items.ORANGE_TULIP,
                        Items.WHITE_TULIP,
                        Items.PINK_TULIP,
                        Items.OXEYE_DAISY,
                        Items.CORNFLOWER,
                        Items.LILY_OF_THE_VALLEY,
                        Items.WITHER_ROSE
                ),
                items -> essences(items).life(1)
        );
        // Overwrite WITHER_ROSE if it's in the tag.
        essences(Blocks.WITHER_ROSE).life(1).death(2);

        fromTag(tagGetter, "minecraft:tall_flowers",
                Arrays.asList(
                        Items.SUNFLOWER,
                        Items.LILAC,
                        Items.PEONY,
                        Items.ROSE_BUSH
                ),
                items -> essences(items).life(2)
        );

        fromTag(tagGetter, "minecraft:saplings",
                Arrays.asList(
                        Items.OAK_SAPLING,
                        Items.SPRUCE_SAPLING,
                        Items.BIRCH_SAPLING,
                        Items.JUNGLE_SAPLING,
                        Items.ACACIA_SAPLING,
                        Items.DARK_OAK_SAPLING
                ),
                items -> essences(items).life(4)
        );

        fromTag(tagGetter, "minecraft:logs",
                Arrays.asList(
                        Items.OAK_LOG,
                        Items.SPRUCE_LOG,
                        Items.BIRCH_LOG,
                        Items.JUNGLE_LOG,
                        Items.ACACIA_LOG,
                        Items.DARK_OAK_LOG,
                        Items.CRIMSON_STEM,
                        Items.WARPED_STEM,
                        Items.OAK_WOOD,
                        Items.SPRUCE_WOOD,
                        Items.BIRCH_WOOD,
                        Items.JUNGLE_WOOD,
                        Items.ACACIA_WOOD,
                        Items.DARK_OAK_WOOD,
                        Items.CRIMSON_HYPHAE,
                        Items.WARPED_HYPHAE,
                        Items.STRIPPED_OAK_LOG,
                        Items.STRIPPED_SPRUCE_LOG,
                        Items.STRIPPED_BIRCH_LOG,
                        Items.STRIPPED_JUNGLE_LOG,
                        Items.STRIPPED_ACACIA_LOG,
                        Items.STRIPPED_DARK_OAK_LOG,
                        Items.STRIPPED_CRIMSON_STEM,
                        Items.STRIPPED_WARPED_STEM,
                        Items.STRIPPED_OAK_WOOD,
                        Items.STRIPPED_SPRUCE_WOOD,
                        Items.STRIPPED_BIRCH_WOOD,
                        Items.STRIPPED_JUNGLE_WOOD,
                        Items.STRIPPED_ACACIA_WOOD,
                        Items.STRIPPED_DARK_OAK_WOOD,
                        Items.STRIPPED_CRIMSON_HYPHAE,
                        Items.STRIPPED_WARPED_HYPHAE
                ),
                items -> essences(items).life(16)
        );

        fromTag(tagGetter, "minecraft:leaves",
                Arrays.asList(
                        Items.JUNGLE_LEAVES,
                        Items.OAK_LEAVES,
                        Items.SPRUCE_LEAVES,
                        Items.DARK_OAK_LEAVES,
                        Items.ACACIA_LEAVES,
                        Items.BIRCH_LEAVES
                ),
                items -> essences(items).life(4)
        );

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

        essences(Items.COAL_ORE).earth(8).fire(10);
        essences(Items.IRON_ORE).earth(11);
        essences(Items.GOLD_ORE).earth(8).light(2);
        essences(Items.DIAMOND_ORE).earth(7).death(3);
        essences(Items.EMERALD_ORE).earth(7).death(3);
        essences(Items.REDSTONE_ORE).earth(5).light(2).fire(3);
        essences(Items.ANCIENT_DEBRIS).earth(7).death(3);

        essences(Items.REDSTONE).earth(0.5f).light(0.2f).fire(0.3f);

        essences(Items.BLAZE_ROD).fire(12).life(8);

        essences(
                Items.COD,
                Items.SALMON,
                Items.TROPICAL_FISH,
                Items.PUFFERFISH
        ).life(4).water(2);

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


        CONVERSIONS.values().forEach(conversion -> conversion.apply(consumer));
    }

    private static ItemEssenceCollection collection(ItemEssenceConversion... entries)
    {
        ItemEssenceCollection collection = new ItemEssenceCollection();

        Collections.addAll(collection, entries);

        return collection;
    }

    @Nullable
    private static ItemEssenceCollection fromTag(BiFunction<ResourceLocation, List<Item>, List<Item>> tagGetter, String id, @Nullable List<Item> fallback, Function<List<Item>, ItemEssenceCollection> conversion)
    {
        List<Item> itemsFromTag = tagGetter.apply(new ResourceLocation(id), fallback);

        if (itemsFromTag == null) return essences();

        return conversion.apply(itemsFromTag);
    }

    private static ItemEssenceCollection essences(ItemLike... items)
    {
        ItemEssenceCollection collection = new ItemEssenceCollection();

        for (ItemLike item : items)
        {
            ItemEssenceEntry ee = new ItemEssenceEntry(item, MagicAmounts.EMPTY);
            collection.add(ee);
            if (CONVERSIONS.containsKey(item.asItem()))
                LOGGER.info("Item already added! " + item);
            CONVERSIONS.put(item.asItem(), ee);
        }

        return collection;
    }

    private static ItemEssenceCollection essences(Iterable<? extends ItemLike> items)
    {
        ItemEssenceCollection collection = new ItemEssenceCollection();

        for (ItemLike item : items)
        {
            ItemEssenceEntry ee = new ItemEssenceEntry(item, MagicAmounts.EMPTY);
            collection.add(ee);
            if (CONVERSIONS.containsKey(item.asItem()))
                LOGGER.info("Item already added! " + item);
            CONVERSIONS.put(item.asItem(), ee);
        }

        return collection;
    }

    private static ItemEssenceEntry essences(ItemLike item)
    {
        ItemEssenceEntry ee = new ItemEssenceEntry(item, MagicAmounts.EMPTY);
        if (CONVERSIONS.containsKey(item.asItem()))
            LOGGER.info("Item already added! " + item);
        CONVERSIONS.put(item.asItem(), ee);
        return ee;
    }

    public interface ItemEssenceConversion
    {
        ItemEssenceConversion all(float amount);

        ItemEssenceConversion fire(float amount);

        ItemEssenceConversion water(float amount);

        ItemEssenceConversion air(float amount);

        ItemEssenceConversion earth(float amount);

        ItemEssenceConversion light(float amount);

        ItemEssenceConversion darkness(float amount);

        ItemEssenceConversion life(float amount);

        ItemEssenceConversion death(float amount);

        ItemEssenceConversion element(Element l, float amount);

        void apply(BiConsumer<Item, MagicAmounts> consumer);
    }

    private static class ItemEssenceEntry implements ItemEssenceConversion
    {
        Item item;
        MagicAmounts amounts;

        public ItemEssenceEntry(ItemLike item, MagicAmounts amounts)
        {
            this.item = item.asItem();
            this.amounts = amounts;
        }

        @Override
        public void apply(BiConsumer<Item, MagicAmounts> consumer)
        {
            consumer.accept(item, amounts);
        }

        @Override
        public ItemEssenceEntry all(float amount)
        {
            amounts = amounts.all(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry fire(float amount)
        {
            amounts = amounts.fire(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry water(float amount)
        {
            amounts = amounts.water(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry air(float amount)
        {
            amounts = amounts.air(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry earth(float amount)
        {
            amounts = amounts.earth(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry light(float amount)
        {
            amounts = amounts.light(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry darkness(float amount)
        {
            amounts = amounts.darkness(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry life(float amount)
        {
            amounts = amounts.life(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry death(float amount)
        {
            amounts = amounts.death(amount);
            return this;
        }

        @Override
        public ItemEssenceEntry element(Element l, float amount)
        {
            amounts = amounts.add(l, amount);
            return this;
        }
    }

    private static class ItemEssenceCollection extends ArrayList<ItemEssenceConversion> implements ItemEssenceConversion
    {
        @Override
        public void apply(BiConsumer<Item, MagicAmounts> consumer)
        {
            this.forEach(itemEssenceConversion -> itemEssenceConversion.apply(consumer));
        }

        @Override
        public ItemEssenceCollection all(float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.all(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection fire(float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.fire(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection water(float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.water(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection air(float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.air(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection earth(float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.earth(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection light(float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.light(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection darkness(float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.darkness(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection life(float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.life(amount);
            }
            return this;
        }

        @Override
        public ItemEssenceCollection death(float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.death(amount);
            }
            return this;
        }


        @Override
        public ItemEssenceCollection element(Element l, float amount)
        {
            for (ItemEssenceConversion e : this)
            {
                e.element(l, amount);
            }
            return this;
        }
    }
}
