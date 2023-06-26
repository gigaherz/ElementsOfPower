package dev.gigaherz.elementsofpower;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.information.datagen.ForcedInformationProvider;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import dev.gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction;
import dev.gigaherz.elementsofpower.cocoons.CocoonFeature;
import dev.gigaherz.elementsofpower.cocoons.CocoonPlacement;
import dev.gigaherz.elementsofpower.database.GemstoneExaminer;
import dev.gigaherz.elementsofpower.database.StockConversions;
import dev.gigaherz.elementsofpower.gemstones.AnalyzedFilteringIngredient;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import dev.gigaherz.elementsofpower.misc.TextureVariantsGen;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import dev.gigaherz.elementsofpower.spells.blocks.DustBlock;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import dev.gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.holdersets.AndHolderSet;
import net.minecraftforge.registries.holdersets.NotHolderSet;
import net.minecraftforge.registries.holdersets.OrHolderSet;

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

        gen.addProvider(event.includeServer(), new RegistryProvider(gen.getPackOutput(), event.getLookupProvider()));

        if ("true".equals(System.getProperty("elementsofpower.doAequivaleoDatagen", "true")) && ModList.get().isLoaded("aequivaleo"))
        {
            gen.addProvider(event.includeServer(), new AequivaleoGens(gen, itemTags));
        }

        gen.addProvider(event.includeClient(), new GearTexturesGen(gen.getPackOutput(), existingFileHelper));

        gen.addProvider(event.includeClient(), new BlockStates(gen.getPackOutput(), existingFileHelper));
    }

    private static class GearTexturesGen extends TextureVariantsGen
    {
        public GearTexturesGen(PackOutput packOutput, ExistingFileHelper existingFileHelper)
        {
            super(packOutput, existingFileHelper, ElementsOfPowerMod.MODID);
        }

        @Override
        protected void genTextures(BiConsumer<ResourceLocation, Supplier<NativeImage>> consumer)
        {
            for(var gem : Gemstone.values())
            {
                ResourceLocation targetPaletteFile = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(gem.getItem()));
                targetPaletteFile = new ResourceLocation(gem.isVanilla() ? "minecraft" : targetPaletteFile.getNamespace(), "item/" + targetPaletteFile.getPath());
                if (gem != Gemstone.DIAMOND)
                {
                    genPaletteSwap(consumer, "item/"+gem.getSerializedName()+"_necklace",
                            new ResourceLocation("elementsofpower:item/diamond_necklace"),
                            new ResourceLocation("minecraft:item/diamond"),
                            targetPaletteFile);
                    genPaletteSwap(consumer, "item/"+gem.getSerializedName()+"_ring",
                            new ResourceLocation("elementsofpower:item/diamond_ring"),
                            new ResourceLocation("minecraft:item/diamond"),
                            targetPaletteFile);
                    genPaletteSwap(consumer, "item/"+gem.getSerializedName()+"_bracelet",
                            new ResourceLocation("elementsofpower:item/diamond_bracelet"),
                            new ResourceLocation("minecraft:item/diamond"),
                            targetPaletteFile);
                    genPaletteSwap(consumer, "item/"+gem.getSerializedName()+"_wand",
                            new ResourceLocation("elementsofpower:item/diamond_wand"),
                            new ResourceLocation("minecraft:item/diamond"),
                            targetPaletteFile);
                    genPaletteSwap(consumer, "item/"+gem.getSerializedName()+"_staff_tip",
                            new ResourceLocation("elementsofpower:item/diamond_staff_tip"),
                            new ResourceLocation("minecraft:item/diamond"),
                            targetPaletteFile);
                    genPaletteSwap(consumer, "item/"+gem.getSerializedName()+"_staff_full_tip",
                            new ResourceLocation("elementsofpower:item/diamond_staff_full_tip"),
                            new ResourceLocation("minecraft:item/diamond"),
                            targetPaletteFile);
                }
                if (gem != Gemstone.EMERALD)
                {
                    genPaletteSwap(consumer, "item/"+gem.getSerializedName()+"_staff_augment",
                            new ResourceLocation("elementsofpower:item/emerald_staff_augment"),
                            new ResourceLocation("minecraft:item/emerald"),
                            targetPaletteFile);
                    genPaletteSwap(consumer, "item/"+gem.getSerializedName()+"_staff_full_augment",
                            new ResourceLocation("elementsofpower:item/emerald_staff_full_augment"),
                            new ResourceLocation("minecraft:item/emerald"),
                            targetPaletteFile);
                }
            }
        }
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

                List<Object> gameObjects = Lists.newArrayList();
                if (item instanceof GemstoneItem gem)
                {
                    gameObjects.add(item);
                    gem.addToTab(gameObjects::add);
                }
                else
                {
                    gameObjects.add(item);
                    gameObjects.add(item.getDefaultInstance());
                }
                save(specFor(gameObjects).withCompounds(compoundRefs));
            });
        }

        private List<Item> getItemsFromTag(ResourceLocation rl, List<Item> fallback)
        {
            TagBuilder tagbuilder = itemTags.getTagByName(rl);
            if (tagbuilder == null)
            {
                var tag = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, rl));
                if (tag.isPresent())
                {
                    return tag.get().stream().map(Holder::get).toList();
                }
                return fallback;
            }
            return tagbuilder.build().stream().flatMap(this::getItemsFromTag).collect(Collectors.toList());
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

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.BRACELET.get())
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


    public static class RegistryProvider extends DatapackBuiltinEntriesProvider
    {

        public RegistryProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
        {
            super(output, lookupProvider, BUILDER, Set.of(ElementsOfPowerMod.MODID));
        }

        @Override
        public String getName() {
            return "Datapack registries";
        }

        public enum BiomeValue implements StringRepresentable
        {
            NEUTRAL("neutral", 1),
            FOR("for", 2),
            AGAINST("against", 0);

            private final String name;
            private final int value;

            BiomeValue(String name, int value)
            {
                this.name = name;
                this.value = value;
            }

            public int value()
            {
                return value;
            }

            @Override
            public String getSerializedName()
            {
                return name;
            }
        }

        public record BiomeValues(BiomeValue heat, BiomeValue humidity, BiomeValue life)
        {
            private int getBiomeBonus(@Nullable Element e)
            {
                if (e == null)
                    return 1;
                return switch (e)
                        {
                            case FIRE -> heat.value();
                            case WATER -> humidity.value();
                            case LIFE -> life.value();
                            case CHAOS -> 2 - life.value();
                            default -> 1;
                        };
            }
        }

        private static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        private static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        private static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_COCOON = ResourceKey.create(Registries.CONFIGURED_FEATURE, ElementsOfPowerMod.location("overworld_cocoon"));
        private static final ResourceKey<PlacedFeature> PLACED_COCOON = ResourceKey.create(Registries.PLACED_FEATURE, ElementsOfPowerMod.location("overworld_cocoon"));
        private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, context -> {
                    context.register(CONFIGURED_COCOON, new ConfiguredFeature<>(ElementsOfPowerMod.COCOON_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
                    for (var heat : BiomeValue.values())
                    {
                        for (var humidity : BiomeValue.values())
                        {
                            for (var life : BiomeValue.values())
                            {
                                var name = heat.getSerializedName() + "_" + humidity.getSerializedName() + "_" + life.getSerializedName();
                                var values = new BiomeValues(heat, humidity, life);
                                for (Gemstone g : Gemstone.values)
                                {
                                    if (g.generateInWorld())
                                    {
                                        var ores = g.getOres();
                                        var stone_ore = ores.get(0);
                                        var deepslate_ore = ores.size() >= 2 ? ores.get(1) : null;

                                        List<OreConfiguration.TargetBlockState> targets = new ArrayList<>();

                                        targets.add(OreConfiguration.target(STONE_ORE_REPLACEABLES, stone_ore.defaultBlockState()));

                                        if (deepslate_ore != null)
                                            targets.add(OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, deepslate_ore.defaultBlockState()));

                                        int numPerVein = 3 + values.getBiomeBonus(g.getElement());
                                        var name2 = g.getSerializedName() + "_ore_" + name;
                                        var key = ResourceKey.create(Registries.CONFIGURED_FEATURE, ElementsOfPowerMod.location(name2));
                                        context.register(key, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(targets, numPerVein, 0.0f)));
                                    }
                                }
                            }
                        }
                    }
                })
                .add(Registries.PLACED_FEATURE, context -> {
                    var configuredFeatureRegistry = context.lookup(Registries.CONFIGURED_FEATURE);
                    context.register(PLACED_COCOON, new PlacedFeature(configuredFeatureRegistry.get(CONFIGURED_COCOON).orElseThrow(), List.of(CocoonPlacement.INSTANCE)));
                    for (var heat : BiomeValue.values())
                    {
                        for (var humidity : BiomeValue.values())
                        {
                            for (var life : BiomeValue.values())
                            {
                                var name = heat.getSerializedName() + "_" + humidity.getSerializedName() + "_" + life.getSerializedName();
                                for (Gemstone g : Gemstone.values)
                                {
                                    if (g.generateInWorld())
                                    {
                                        var name2 = g.getSerializedName() + "_ore_" + name;
                                        var key = ResourceKey.create(Registries.CONFIGURED_FEATURE, ElementsOfPowerMod.location(name2));
                                        var key2 = ResourceKey.create(Registries.PLACED_FEATURE, ElementsOfPowerMod.location(name2));
                                        var configured = configuredFeatureRegistry.get(key).orElseThrow();
                                        context.register(key2, new PlacedFeature(configured, List.of(
                                                CountPlacement.of(16),
                                                InSquarePlacement.spread(),
                                                HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)),
                                                BiomeFilter.biome())));
                                    }
                                }
                            }
                        }
                    }
                })
                .add(ForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                    var biomes = context.lookup(Registries.BIOME);
                    var placedFeatures = context.lookup(Registries.PLACED_FEATURE);

                    var biomes0 = new HolderLookup.RegistryLookup<Biome>() {

                        @Override
                        public Optional<Holder.Reference<Biome>> get(ResourceKey<Biome> pResourceKey)
                        {
                            return biomes.get(pResourceKey);
                        }

                        @Override
                        public Optional<HolderSet.Named<Biome>> get(TagKey<Biome> pTagKey)
                        {
                            return biomes.get(pTagKey);
                        }

                        @Override
                        public Stream<Holder.Reference<Biome>> listElements()
                        {
                            return Stream.empty();
                        }

                        @Override
                        public Stream<HolderSet.Named<Biome>> listTags()
                        {
                            return Stream.empty();
                        }

                        @Override
                        public ResourceKey<? extends Registry<? extends Biome>> key()
                        {
                            return Registries.BIOME;
                        }

                        @Override
                        public boolean canSerializeIn(HolderOwner<Biome> pOwner)
                        {
                            return true;
                        }

                        @Override
                        public Lifecycle registryLifecycle()
                        {
                            return Lifecycle.stable();
                        }
                    };

                    final HolderSet.Named<Biome> isOverworld = biomes.get(BiomeTags.IS_OVERWORLD).orElseThrow();
                    final HolderSet.Named<Biome> isHot = biomes.get(Tags.Biomes.IS_HOT_OVERWORLD).orElseThrow();
                    final HolderSet.Named<Biome> isCold = biomes.get(Tags.Biomes.IS_COLD_OVERWORLD).orElseThrow();
                    final HolderSet.Named<Biome> isWet = biomes.get(Tags.Biomes.IS_WET_OVERWORLD).orElseThrow();
                    final HolderSet.Named<Biome> isDry = biomes.get(Tags.Biomes.IS_DRY_OVERWORLD).orElseThrow();
                    final HolderSet.Named<Biome> isDense = biomes.get(Tags.Biomes.IS_DENSE_OVERWORLD).orElseThrow();
                    final HolderSet.Named<Biome> isSparse = biomes.get(Tags.Biomes.IS_SPARSE_OVERWORLD).orElseThrow();
                    for (var heat : BiomeValue.values())
                    {
                        for (var humidity : BiomeValue.values())
                        {
                            for (var life : BiomeValue.values())
                            {
                                var name = heat.getSerializedName() + "_" + humidity.getSerializedName() + "_" + life.getSerializedName();
                                for (Gemstone g : Gemstone.values)
                                {
                                    if (g.generateInWorld())
                                    {
                                        var name2 = g.getSerializedName() + "_ore_" + name;
                                        var key2 = ResourceKey.create(Registries.PLACED_FEATURE, ElementsOfPowerMod.location(name2));
                                        var placed = placedFeatures.get(key2).orElseThrow();
                                        var key = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, ElementsOfPowerMod.location(name2));

                                        HolderSet<Biome> heatHolderSet = switch (heat)
                                                {
                                                    case FOR -> isHot;
                                                    case AGAINST -> isCold;
                                                    default -> new NotHolderSet<>(biomes0, new OrHolderSet<>(List.of(isHot, isCold)));
                                                };
                                        HolderSet<Biome> humidityHolderSet = switch (humidity)
                                                {
                                                    case FOR -> isWet;
                                                    case AGAINST -> isDry;
                                                    default -> new NotHolderSet<>(biomes0, new OrHolderSet<>(List.of(isWet, isDry)));
                                                };
                                        HolderSet<Biome> lifeHolderSet = switch (life)
                                                {
                                                    case FOR -> isDense;
                                                    case AGAINST -> isSparse;
                                                    default -> new NotHolderSet<>(biomes0, new OrHolderSet<>(List.of(isDense, isSparse)));
                                                };
                                        context.register(key, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                                                new AndHolderSet<>(List.of(isOverworld, heatHolderSet, humidityHolderSet, lifeHolderSet)),
                                                HolderSet.direct(placed),
                                                GenerationStep.Decoration.UNDERGROUND_ORES
                                        ));
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private static TagKey<Item> itemTag(String p_203855_) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(p_203855_));
    }

    private static TagKey<Block> blockTag(String p_203847_) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(p_203847_));
    }
}
