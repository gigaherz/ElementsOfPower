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
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import dev.gigaherz.elementsofpower.spells.blocks.DustBlock;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import dev.gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ElementsofPowerDataGen
{
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new Recipes(gen.getPackOutput()));
        gen.addProvider(event.includeServer(), LootGen.create(gen.getPackOutput()));

        BlockTagGens blockTags = new BlockTagGens(gen.getPackOutput(), existingFileHelper);
        ItemTagGens itemTags = new ItemTagGens(gen.getPackOutput(), existingFileHelper);
        gen.addProvider(event.includeServer(), blockTags);
        gen.addProvider(event.includeServer(), itemTags);

        if ("true".equals(System.getProperty("elementsofpower.doAequivaleoDatagen", "true")))
        {
            gen.addProvider(event.includeServer(), new AequivaleoGens(gen, itemTags));
        }

        gen.addProvider(event.includeClient(), new BlockStates(gen.getPackOutput(), existingFileHelper));
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
                if (item instanceof GemstoneItem gem)
                    gem.addToTab(stacks::add);
                else
                    stacks.add(new ItemStack(item));
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

    private static class ItemTagGens extends IntrinsicHolderTagsProvider<Item>
    {
        public ItemTagGens(PackOutput packOutput, ExistingFileHelper existingFileHelper)
        {
            super(packOutput, Registries.ITEM, CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor()),
                    (p_255627_) -> p_255627_.builtInRegistryHolder().key(), ElementsOfPowerMod.MODID, existingFileHelper);
        }

        @Nullable
        public TagBuilder getTagByName(ResourceLocation tag)
        {
            return this.builders.get(tag);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookup)
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

    private static class BlockTagGens extends IntrinsicHolderTagsProvider<Block>
    {
        public BlockTagGens(PackOutput packOutput, ExistingFileHelper existingFileHelper)
        {
            super(packOutput, Registries.BLOCK, CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor()),
                    (p_255627_) -> p_255627_.builtInRegistryHolder().key(), ElementsOfPowerMod.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookup)
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
        }
    }

    private static class LootGen
    {
        public static LootTableProvider create(PackOutput gen)
        {
            return new LootTableProvider(gen, Set.of(), List.of(
                    new LootTableProvider.SubProviderEntry(LootGen.BlockTables::new, LootContextParamSets.BLOCK)
                    //new LootTableProvider.SubProviderEntry(LootGen.ChestTables::new, LootContextParamSets.CHEST)
            ));
        }

        public static class BlockTables extends BlockLootSubProvider
        {
            protected BlockTables()
            {
                super(Set.of(), FeatureFlags.REGISTRY.allFlags());
            }

            @Override
            protected void generate()
            {
                this.dropSelf(ElementsOfPowerBlocks.ESSENTIALIZER.get());

                Element.stream_without_balance().forEach(e -> {
                    if (e != Element.BALANCE && e.getCocoon() != null)
                        this.add(e.getCocoon(), this::dropWithOrbs);
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

            protected LootTable.Builder dropWithOrbs(Block block)
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
        public BlockStates(PackOutput gen, ExistingFileHelper existingFileHelper)
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
        public Recipes(PackOutput gen)
        {
            super(gen);
        }

        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> consumer)
        {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.ANALYZER.get())
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

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.ESSENTIALIZER.get())
                    .pattern("IQI")
                    .pattern("ONO")
                    .pattern("IOI")
                    .define('I', Items.IRON_INGOT)
                    .define('O', Items.OBSIDIAN)
                    .define('Q', GemstoneExaminer.GEMSTONES)
                    .define('N', Items.NETHER_STAR)
                    .unlockedBy("has_star", has(Items.NETHER_STAR))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.WAND.get())
                    .pattern(" G")
                    .pattern("S ")
                    .define('G', Items.GOLD_INGOT)
                    .define('S', Items.STICK)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.STAFF.get())
                    .pattern(" GW")
                    .pattern(" SG")
                    .pattern("S  ")
                    .define('G', Items.IRON_BLOCK)
                    .define('S', Items.STICK)
                    .define('W', ElementsOfPowerItems.WAND.get())
                    .unlockedBy("has_wand", has(ElementsOfPowerItems.WAND.get()))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.RING.get())
                    .pattern(" GG")
                    .pattern("G G")
                    .pattern(" G ")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.NECKLACE.get())
                    .pattern("GGG")
                    .pattern("G G")
                    .pattern(" G ")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.HEADBAND.get())
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
                    SimpleCookingRecipeBuilder.smelting(Ingredient.of(oreItems), RecipeCategory.MISC, gem, 1.0F, 200)
                            .unlockedBy("has_ore", has(tag))
                            .save(consumer, ElementsOfPowerMod.location("smelting/" + gem.getSerializedName()));
                }
                if (gem.generateCustomBlock())
                {
                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, gem.getBlock())
                            .pattern("ggg")
                            .pattern("ggg")
                            .pattern("ggg")
                            .define('g', AnalyzedFilteringIngredient.wrap(Ingredient.of(gem.getItem())))
                            .unlockedBy("has_item", has(gem.getItem()))
                            .save(consumer);

                    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, gem.getItem(), 9)
                            .requires(Ingredient.of(gem.getBlock()))
                            .unlockedBy("has_item", has(gem.getItem()))
                            .save(consumer, ElementsOfPowerMod.location(gem.getSerializedName() + "_from_block"));
                }
            }
        }
    }

    private static TagKey<Item> itemTag(String p_203855_) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(p_203855_));
    }

    private static TagKey<Block> blockTag(String p_203847_) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(p_203847_));
    }
}
