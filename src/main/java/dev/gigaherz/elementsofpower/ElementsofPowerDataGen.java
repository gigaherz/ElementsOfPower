package dev.gigaherz.elementsofpower;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.information.datagen.ForcedInformationProvider;
import com.mojang.datafixers.util.Pair;
import dev.gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction;
import dev.gigaherz.elementsofpower.cocoons.CocoonFeature;
import dev.gigaherz.elementsofpower.database.GemstoneExaminer;
import dev.gigaherz.elementsofpower.database.StockConversions;
import dev.gigaherz.elementsofpower.gemstones.AnalyzedFilteringIngredient;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import dev.gigaherz.elementsofpower.spells.blocks.DustBlock;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import dev.gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ibm.icu.impl.ValidIdentifiers.Datatype.x;

class ElementsofPowerDataGen
{
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new Recipes(gen));
        gen.addProvider(event.includeServer(), new LootGen(gen));

        BlockTagGens blockTags = new BlockTagGens(gen, existingFileHelper);
        ItemTagGens itemTags = new ItemTagGens(gen, blockTags, existingFileHelper);
        gen.addProvider(event.includeServer(), blockTags);
        gen.addProvider(event.includeServer(), itemTags);

        if ("true".equals(System.getProperty("elementsofpower.doAequivaleoDatagen", "true")))
        {
            gen.addProvider(event.includeServer(), new AequivaleoGens(gen, itemTags));
        }

        gen.addProvider(event.includeClient(), new BlockStates(gen, existingFileHelper));
    }

    private static class AequivaleoGens extends ForcedInformationProvider
    {
        private final ItemTagGens itemTags;

        protected AequivaleoGens(DataGenerator dataGenerator, ItemTagGens itemTags)
        {
            super(ElementsOfPowerMod.MODID, dataGenerator);
            this.itemTags = itemTags;
        }

        @Override
        public void calculateDataToSave()
        {
            StockConversions.addStockConversions(this::getItemsFromTag, (item, amounts) -> {
                List<CompoundInstance> compoundRefs = Element.stream_without_balance()
                        .map(e -> Pair.of(e, amounts.get(e)))
                        .filter(p -> p.getSecond() > 0)
                        .map(p -> new CompoundInstance(AequivaleoPlugin.BY_ELEMENT.get(p.getFirst()).get(), (double) p.getSecond()))
                        .collect(Collectors.toList());

                Set<Object> gameObjects = Sets.newHashSet(item);
                NonNullList<ItemStack> stacks = NonNullList.create();
                item.fillItemCategory(CreativeModeTab.TAB_SEARCH, stacks);
                gameObjects.addAll(stacks);
                save(specFor(gameObjects).withCompounds(compoundRefs));
            });
        }

        private List<Item> getItemsFromTag(ResourceLocation rl, List<Item> fallback)
        {
            TagBuilder tag = itemTags.getTagByName(rl);
            if (tag == null)
                return fallback;
            return tag.build().stream().flatMap(this::getItemsFromTag).collect(Collectors.toList());
        }

        private Stream<Item> getItemsFromTag(TagEntry entry)
        {
            if (entry.tag)
            {
                ResourceLocation tagId = entry.getId();
                return getItemsFromTag(tagId, Collections.emptyList()).stream();
            }
            else
            {
                ResourceLocation itemId = entry.getId();
                return Stream.of(ForgeRegistries.ITEMS.getValue(itemId));
            }
        }
    }

    private static class ItemTagGens extends ItemTagsProvider implements DataProvider
    {
        public ItemTagGens(DataGenerator gen, BlockTagsProvider blockTags, ExistingFileHelper existingFileHelper)
        {
            super(gen, blockTags, ElementsOfPowerMod.MODID, existingFileHelper);
        }

        @Nullable
        public TagBuilder getTagByName(ResourceLocation tag)
        {
            return this.builders.get(tag);
        }

        @Override
        protected void addTags()
        {
            GemstoneExaminer.GEMS.forEach((gem, tag) -> {
            });

            TagAppender<Item> gemsTag = this.tag(Tags.Items.GEMS);
            TagAppender<Item> oresTag = this.tag(Tags.Items.ORES);
            TagAppender<Item> blocksTag = this.tag(Tags.Items.STORAGE_BLOCKS);
            TagAppender<Item> examinerTag = this.tag(GemstoneExaminer.GEMSTONES);
            Gemstone.values.forEach(gem -> {
                if (gem != Gemstone.CREATIVITE)
                {
                    TagKey<Item> tag = itemTag(new ResourceLocation("forge", "gems/" + gem.getSerializedName()).toString());
                    this.tag(tag).add(gem.getTagItems());
                    gemsTag.addTag(tag);

                    TagKey<Item> eTag = GemstoneExaminer.GEMS.get(gem);
                    this.tag(eTag).add(gem.getTagItems());
                    examinerTag.addTag(eTag);
                }
                if (gem.generateCustomOre())
                {
                    Item[] oreItems = gem.getOres().stream().map(Block::asItem).toArray(Item[]::new);

                    TagKey<Item> tag = itemTag(new ResourceLocation("forge", "ores/" + gem.getSerializedName()).toString());
                    this.tag(tag).add(oreItems);
                    oresTag.addTag(tag);

                    this.tag(itemTag(new ResourceLocation("elementsofpower", gem.getSerializedName() + "_ores").toString()))
                            .add(oreItems);
                }
                if (gem.generateCustomBlock())
                {
                    Block block = gem.getBlock();
                    TagKey<Item> tag = itemTag(new ResourceLocation("forge", "storage_blocks/" + gem.getSerializedName()).toString());
                    this.tag(tag).add(block.asItem());
                    blocksTag.addTag(tag);
                }
            });
        }
    }

    private static class BlockTagGens extends BlockTagsProvider implements DataProvider
    {
        public BlockTagGens(DataGenerator gen, ExistingFileHelper existingFileHelper)
        {
            super(gen, ElementsOfPowerMod.MODID, existingFileHelper);
        }

        @Override
        protected void addTags()
        {
            this.tag(CocoonFeature.REPLACEABLE_TAG)
                    .add(Blocks.SAND, Blocks.RED_SAND, Blocks.DIRT, Blocks.NETHERRACK);

            this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ElementsOfPowerBlocks.ESSENTIALIZER.get());
            this.tag(BlockTags.NEEDS_IRON_TOOL).add(ElementsOfPowerBlocks.ESSENTIALIZER.get());

            for (Gemstone type : Gemstone.values())
            {
                if (type.generateCustomBlock())
                {
                    this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                            .add(type.getBlock());
                    this.tag(BlockTags.NEEDS_IRON_TOOL)
                            .add(type.getBlock());
                }
                if (type.generateCustomOre())
                {
                    this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                            .add(type.getOres().toArray(Block[]::new));
                    this.tag(BlockTags.NEEDS_IRON_TOOL)
                            .add(type.getOres().toArray(Block[]::new));
                }
            }

            TagAppender<Block> oresTag = this.tag(Tags.Blocks.ORES);
            TagAppender<Block> blocksTag = this.tag(Tags.Blocks.STORAGE_BLOCKS);
            Gemstone.values.forEach(gem -> {
                if (gem.generateCustomOre())
                {
                    TagKey<Block> tag = blockTag(new ResourceLocation("forge", "ores/" + gem.getSerializedName()).toString());
                    this.tag(tag).add(gem.getOres().toArray(Block[]::new));
                    oresTag.addTag(tag);
                }
                if (gem.generateCustomBlock())
                {
                    Block block = gem.getBlock();
                    TagKey<Block> tag = blockTag(new ResourceLocation("forge", "storage_blocks/" + gem.getSerializedName()).toString());
                    this.tag(tag).add(block);
                    blocksTag.addTag(tag);
                }
            });

            enum Quantity {
                VeryLow("very_low"),
                Low("low"),
                High("high");

                private final String quantityString;

                Quantity(String quantityString)
                {
                    this.quantityString = quantityString;
                }

                public String getQuantityString()
                {
                    return quantityString;
                }

                public static final Quantity[] values = values();
            }

            final Map<Element, Map<Quantity, TagAppender<Block>>> tagMap = new HashMap<>();
            for(var e : Element.values)
            {
                if (e == Element.BALANCE) continue;
                var qMap = new HashMap<Quantity, TagAppender<Block>>();
                for(var q : Quantity.values)
                {
                    var tag = this.tag(TagKey.create(Registry.BLOCK_REGISTRY, ElementsOfPowerMod.location("contained_magic/" + e.getName() + "_" + q.getQuantityString())));

                    qMap.put(q, tag);
                }
                tagMap.put(e, qMap);
            }

            for(var block : ForgeRegistries.BLOCKS)
            {
                MagicAmounts am = MagicAmounts.EMPTY;

                Material mat = block.defaultBlockState().getMaterial();
                if (mat == Material.AIR)
                {
                    am = am.air(0.25f);
                }
                else if (mat == Material.WATER)
                {
                    am = am.water(1.5f);
                }
                else if (mat == Material.LAVA)
                {
                    am = am.fire(1);
                    am = am.earth(0.5f);
                }
                else if (mat == Material.FIRE)
                {
                    am = am.fire(1);
                    am = am.air(0.5f);
                }
                else if (mat == Material.STONE)
                {
                    am = am.earth(1);
                    if (block == Blocks.NETHERRACK)
                    {
                        am = am.fire(0.5f);
                    }
                    else if (block == Blocks.END_STONE || block == Blocks.END_STONE_BRICKS)
                    {
                        am = am.darkness(0.5f);
                    }
                }
                else if (mat == Material.SAND)
                {
                    am = am.earth(0.5f);
                    if (block == Blocks.SOUL_SAND)
                    {
                        am = am.death(1);
                    }
                    else
                    {
                        am = am.air(1);
                    }
                }
                else if (mat == Material.WOOD)
                {
                    am = am.life(1);
                    am = am.earth(0.5f);
                }
                else if (mat == Material.LEAVES)
                {
                    am = am.life(1);
                }
                else if (mat == Material.PLANT)
                {
                    am = am.life(1);
                }
                else if (mat == Material.CACTUS)
                {
                    am = am.life(1);
                    am = am.earth(0.5f);
                }
                else if (mat == Material.GRASS)
                {
                    am = am.life(0.5f);
                    am = am.earth(1);
                }
                else if (mat == Material.DIRT)
                {
                    am = am.earth(1);
                    if (block == Blocks.PODZOL)
                    {
                        am = am.life(0.5f);
                    }
                }
                else if (mat == Material.METAL)
                {
                    am = am.earth(1);
                }
                else if (mat == Material.GLASS)
                {
                    am = am.earth(0.5f);
                    am = am.light(0.5f);
                    am = am.air(0.5f);
                }
                else if (mat == Material.BUILDABLE_GLASS)
                {
                    am = am.earth(0.5f);
                    am = am.light(1);
                }
                else if (mat == Material.ICE || mat == Material.ICE_SOLID)
                {
                    am = am.water(1);
                    am = am.darkness(0.5f);
                }
                else if (mat == Material.TOP_SNOW || mat == Material.SNOW)
                {
                    am = am.water(0.5f);
                    am = am.darkness(0.5f);
                }
                else if (mat == Material.CLAY)
                {
                    am = am.earth(0.5f);
                    am = am.water(1);
                }
                else if (mat == Material.VEGETABLE)
                {
                    am = am.earth(0.5f);
                    am = am.life(0.25f);
                }
                else if (mat == Material.EGG)
                {
                    am = am.darkness(1);
                }

                for(var e : Element.values)
                {
                    if (e == Element.BALANCE) continue;
                    var q = am.get(e);
                    if (q > 0 && q <= 0.25f)
                    {
                        tagMap.get(e).get(Quantity.VeryLow).add(block);
                    }
                    else if(q > 0.25f && q <= 0.5f)
                    {
                        tagMap.get(e).get(Quantity.Low).add(block);
                    }
                    else if(q >= 1.0f)
                    {
                        tagMap.get(e).get(Quantity.High).add(block);
                    }
                }
            }
        }
    }

    private static class LootGen extends LootTableProvider implements DataProvider
    {
        public LootGen(DataGenerator gen)
        {
            super(gen);
        }

        private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = ImmutableList.of(
                Pair.of(BlockTables::new, LootContextParamSets.BLOCK)
                //Pair.of(FishingLootTables::new, LootParameterSets.FISHING),
                //Pair.of(ChestLootTables::new, LootParameterSets.CHEST),
                //Pair.of(EntityLootTables::new, LootParameterSets.ENTITY),
                //Pair.of(GiftLootTables::new, LootParameterSets.GIFT)
        );

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
        {
            return tables;
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker)
        {
            map.forEach((p_218436_2_, p_218436_3_) -> {
                LootTables.validate(validationtracker, p_218436_2_, p_218436_3_);
            });
        }

        public static class BlockTables extends BlockLoot
        {
            @Override
            protected void addTables()
            {
                this.dropSelf(ElementsOfPowerBlocks.ESSENTIALIZER.get());

                Element.stream_without_balance().forEach(e -> {
                    if (e != Element.BALANCE && e.getCocoon() != null)
                        this.add(e.getCocoon(), BlockTables::dropWithOrbs);
                });

                Gemstone.stream().forEach(g -> {
                    if (g.generateCustomBlock())
                        this.dropSelf(g.getBlock());
                    if (g.generateCustomOre())
                    {
                        for(var ore : g.getOres())
                        {
                            this.add(ore, (block) -> createOreDrop(block, g.getItem()));
                        }
                    }
                });
            }

            protected static LootTable.Builder dropWithOrbs(Block block)
            {
                LootTable.Builder builder = LootTable.lootTable();
                for (Element e : Element.values_without_balance)
                {
                    if (e.getOrb() != null)
                    {
                        LootPool.Builder pool = LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(e.getOrb())
                                        .apply(ApplyOrbSizeFunction.builder().with(e)));
                        builder = builder.withPool(applyExplosionCondition(block, pool));
                    }
                }
                return builder;
            }

            @Override
            protected Iterable<Block> getKnownBlocks()
            {
                return ForgeRegistries.BLOCKS.getValues().stream()
                        .filter(b -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(b)).getNamespace().equals(ElementsOfPowerMod.MODID))
                        .collect(Collectors.toList());
            }
        }
    }

    private static class BlockStates extends BlockStateProvider
    {
        public BlockStates(DataGenerator gen, ExistingFileHelper existingFileHelper)
        {
            super(gen, ElementsOfPowerMod.MODID, existingFileHelper);
        }

        @Override
        protected void registerStatesAndModels()
        {
            densityBlock(ElementsOfPowerBlocks.MIST.get(), MistBlock.DENSITY);
            densityBlock(ElementsOfPowerBlocks.DUST.get(), DustBlock.DENSITY);
            densityBlock(ElementsOfPowerBlocks.LIGHT.get(), LightBlock.DENSITY, (value) -> ElementsOfPowerMod.location("transparent"));
            densityBlock(ElementsOfPowerBlocks.CUSHION.get(), CushionBlock.DENSITY, (density) -> ElementsOfPowerMod.location("block/dust_" + density));
        }

        private void densityBlock(Block block, IntegerProperty densityProperty)
        {
            densityBlock(block, densityProperty, (density) -> ElementsOfPowerMod.location("block/" + Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath() + "_" + density));
        }

        private void densityBlock(Block block, IntegerProperty densityProperty, Function<Integer, ResourceLocation> texMapper)
        {
            Map<Integer, ModelFile> densityModels = Maps.asMap(
                    new HashSet<>(densityProperty.getPossibleValues()),
                    density -> {
                        return models().cubeAll(ElementsOfPowerMod.location(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath() + "_" + density).getPath(), texMapper.apply(density));
                    });

            getVariantBuilder(block)
                    .forAllStates(state -> ConfiguredModel.builder()
                            .modelFile(densityModels.get(state.getValue(MistBlock.DENSITY)))
                            .build()
                    );
        }
    }

    private static class Recipes extends RecipeProvider
    {
        public Recipes(DataGenerator gen)
        {
            super(gen);
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
        {
            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.ANALYZER.get())
                    .pattern("glg")
                    .pattern("i  ")
                    .pattern("psp")
                    .define('l', Items.GOLD_INGOT)
                    .define('i', Items.IRON_INGOT)
                    .define('g', Tags.Items.GLASS_PANES)
                    .define('p', ItemTags.PLANKS)
                    .define('s', ItemTags.WOODEN_SLABS)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.ESSENTIALIZER.get())
                    .pattern("IQI")
                    .pattern("ONO")
                    .pattern("IOI")
                    .define('I', Items.IRON_INGOT)
                    .define('O', Items.OBSIDIAN)
                    .define('Q', GemstoneExaminer.GEMSTONES)
                    .define('N', Items.NETHER_STAR)
                    .unlockedBy("has_star", has(Items.NETHER_STAR))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.WAND.get())
                    .pattern(" G")
                    .pattern("S ")
                    .define('G', Items.GOLD_INGOT)
                    .define('S', Items.STICK)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.STAFF.get())
                    .pattern(" GW")
                    .pattern(" SG")
                    .pattern("S  ")
                    .define('G', Items.IRON_BLOCK)
                    .define('S', Items.STICK)
                    .define('W', ElementsOfPowerItems.WAND.get())
                    .unlockedBy("has_wand", has(ElementsOfPowerItems.WAND.get()))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.RING.get())
                    .pattern(" GG")
                    .pattern("G G")
                    .pattern(" G ")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.NECKLACE.get())
                    .pattern("GGG")
                    .pattern("G G")
                    .pattern(" G ")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.HEADBAND.get())
                    .pattern(" G ")
                    .pattern("G G")
                    .pattern("GGG")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            SpecialRecipeBuilder.special(ElementsOfPowerMod.GEMSTONE_CHANGE.get()).save(consumer, ElementsOfPowerMod.location("gemstone_change").toString());
            SpecialRecipeBuilder.special(ElementsOfPowerMod.CONTAINER_CHARGE.get()).save(consumer, ElementsOfPowerMod.location("container_charge").toString());

            for (Gemstone gem : Gemstone.values())
            {
                if (gem.generateCustomOre())
                {
                    TagKey<Item> tag = itemTag(new ResourceLocation("elementsofpower", gem.getSerializedName() + "_ores").toString());
                    Item[] oreItems = gem.getOres().stream().map(Block::asItem).toArray(Item[]::new);
                    SimpleCookingRecipeBuilder.smelting(Ingredient.of(oreItems), gem, 1.0F, 200)
                            .unlockedBy("has_ore", has(tag))
                            .save(consumer, ElementsOfPowerMod.location("smelting/" + gem.getSerializedName()));
                }
                if (gem.generateCustomBlock())
                {
                    ShapedRecipeBuilder.shaped(gem.getBlock())
                            .pattern("ggg")
                            .pattern("ggg")
                            .pattern("ggg")
                            .define('g', AnalyzedFilteringIngredient.wrap(Ingredient.of(gem.getItem())))
                            .unlockedBy("has_item", has(gem.getItem()))
                            .save(consumer);

                    ShapelessRecipeBuilder.shapeless(gem.getItem(), 9)
                            .requires(Ingredient.of(gem.getBlock()))
                            .unlockedBy("has_item", has(gem.getItem()))
                            .save(consumer, ElementsOfPowerMod.location(gem.getSerializedName() + "_from_block"));
                }
            }
        }
    }

    private static TagKey<Item> itemTag(String p_203855_) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(p_203855_));
    }

    private static TagKey<Block> blockTag(String p_203847_) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(p_203847_));
    }
}
