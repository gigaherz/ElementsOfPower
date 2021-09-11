package gigaherz.elementsofpower;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.information.datagen.ForcedInformationProvider;
import com.ldtteam.aequivaleo.api.compound.information.datagen.LockedInformationProvider;
import com.ldtteam.aequivaleo.api.compound.information.datagen.ValueInformationProvider;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceRef;
import com.mojang.datafixers.util.Pair;
import gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction;
import gigaherz.elementsofpower.cocoons.CocoonFeature;
import gigaherz.elementsofpower.database.GemstoneExaminer;
import gigaherz.elementsofpower.database.StockConversions;
import gigaherz.elementsofpower.gemstones.AnalyzedFilteringIngredient;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.GemstoneItem;
import gigaherz.elementsofpower.gemstones.Quality;
import gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import gigaherz.elementsofpower.recipes.GemstoneChangeRecipe;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.spells.blocks.CushionBlock;
import gigaherz.elementsofpower.spells.blocks.DustBlock;
import gigaherz.elementsofpower.spells.blocks.LightBlock;
import gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.*;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.loot.*;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
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

        if (event.includeServer())
        {
            gen.addProvider(new Recipes(gen));
            gen.addProvider(new LootTables(gen));

            BlockTagGens blockTags = new BlockTagGens(gen, existingFileHelper);
            ItemTagGens itemTags = new ItemTagGens(gen, blockTags, existingFileHelper);
            gen.addProvider(blockTags);
            gen.addProvider(itemTags);

            if ("true".equals(System.getProperty("elementsofpower.doAequivaleoDatagen", "true")))
            {
                gen.addProvider(new AequivaleoGens(gen, itemTags));
            }
        }
        if (event.includeClient())
        {
            gen.addProvider(new BlockStates(gen, existingFileHelper));
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
                List<CompoundInstance> compoundRefs = Element.stream()
                        .map(e -> Pair.of(e, amounts.get(e)))
                        .filter(p -> p.getSecond() > 0)
                        .map(p -> new CompoundInstance(AequivaleoPlugin.BY_ELEMENT.get(p.getFirst()).get(), (double) p.getSecond()))
                        .collect(Collectors.toList());

                Set<Object> gameObjects = Sets.newHashSet(item);
                NonNullList<ItemStack> stacks = NonNullList.create();
                item.fillItemGroup(ItemGroup.SEARCH, stacks);
                gameObjects.addAll(stacks);
                save(specFor(gameObjects).withCompounds(compoundRefs));
            });
        }

        private List<Item> getItemsFromTag(ResourceLocation rl, List<Item> fallback)
        {
            ITag.Builder tag = itemTags.getTagByName(rl);
            if (tag == null)
                return fallback;
            return tag.getProxyStream().flatMap(this::getItemsFromTag).collect(Collectors.toList());
        }

        private Stream<Item> getItemsFromTag(ITag.Proxy proxy)
        {
            ITag.ITagEntry entry = proxy.getEntry();
            if (entry instanceof ITag.ItemEntry)
            {
                ResourceLocation itemId = new ResourceLocation(((ITag.ItemEntry)entry).toString());
                return Stream.of(ForgeRegistries.ITEMS.getValue(itemId));
            }
            if (entry instanceof ITag.TagEntry)
            {
                ResourceLocation tagId = ((ITag.TagEntry)entry).getId();
                return getItemsFromTag(tagId, Collections.emptyList()).stream();
            }
            return Stream.empty();
        }
    }

    private static class ItemTagGens extends ItemTagsProvider implements IDataProvider
    {
        public ItemTagGens(DataGenerator gen, BlockTagsProvider blockTags, ExistingFileHelper existingFileHelper)
        {
            super(gen, blockTags, ElementsOfPowerMod.MODID, existingFileHelper);
        }

        @Nullable
        public ITag.Builder getTagByName(ResourceLocation tag)
        {
            return this.tagToBuilder.get(tag);
        }

        @Override
        protected void registerTags()
        {
            GemstoneExaminer.GEMS.forEach((gem, tag) -> {
            });

            Builder<Item> gemsTag = this.getOrCreateBuilder(Tags.Items.GEMS);
            Builder<Item> oresTag = this.getOrCreateBuilder(Tags.Items.ORES);
            Builder<Item> blocksTag = this.getOrCreateBuilder(Tags.Items.STORAGE_BLOCKS);
            Builder<Item> examinerTag = this.getOrCreateBuilder(GemstoneExaminer.GEMSTONES);
            Gemstone.values.forEach(gem -> {
                if (gem != Gemstone.CREATIVITE)
                {
                    ITag.INamedTag<Item> tag = ItemTags.makeWrapperTag(new ResourceLocation("forge", "gems/" + gem.getString()).toString());
                    this.getOrCreateBuilder(tag).add(gem.getTagItems());
                    gemsTag.addTag(tag);

                    ITag.INamedTag<Item> eTag = GemstoneExaminer.GEMS.get(gem);
                    this.getOrCreateBuilder(eTag).add(gem.getTagItems());
                    examinerTag.addTag(eTag);
                }
                if (gem.generateCustomOre())
                {
                    Block ore = gem.getOre();
                    ITag.INamedTag<Item> tag = ItemTags.makeWrapperTag(new ResourceLocation("forge", "ores/" + gem.getString()).toString());
                    this.getOrCreateBuilder(tag).add(ore.asItem());
                    oresTag.addTag(tag);
                }
                if (gem.generateCustomBlock())
                {
                    Block block = gem.getBlock();
                    ITag.INamedTag<Item> tag = ItemTags.makeWrapperTag(new ResourceLocation("forge", "storage_blocks/" + gem.getString()).toString());
                    this.getOrCreateBuilder(tag).add(block.asItem());
                    blocksTag.addTag(tag);
                }
            });
        }
    }

    private static class BlockTagGens extends BlockTagsProvider implements IDataProvider
    {
        public BlockTagGens(DataGenerator gen, ExistingFileHelper existingFileHelper)
        {
            super(gen, ElementsOfPowerMod.MODID, existingFileHelper);
        }

        @Override
        protected void registerTags()
        {
            this.getOrCreateBuilder(CocoonFeature.REPLACEABLE_TAG)
                    .add(Blocks.SAND, Blocks.RED_SAND, Blocks.DIRT, Blocks.NETHERRACK);
        }
    }

    private static class LootTables extends LootTableProvider implements IDataProvider
    {
        public LootTables(DataGenerator gen)
        {
            super(gen);
        }

        private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> tables = ImmutableList.of(
                Pair.of(LootTables.BlockTables::new, LootParameterSets.BLOCK)
                //Pair.of(FishingLootTables::new, LootParameterSets.FISHING),
                //Pair.of(ChestLootTables::new, LootParameterSets.CHEST),
                //Pair.of(EntityLootTables::new, LootParameterSets.ENTITY),
                //Pair.of(GiftLootTables::new, LootParameterSets.GIFT)
        );

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
        {
            return tables;
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker)
        {
            map.forEach((p_218436_2_, p_218436_3_) -> {
                LootTableManager.validateLootTable(validationtracker, p_218436_2_, p_218436_3_);
            });
        }

        public static class BlockTables extends BlockLootTables
        {
            @Override
            protected void addTables()
            {
                this.registerDropSelfLootTable(ElementsOfPowerBlocks.ESSENTIALIZER);

                Element.stream().forEach(e -> this.registerLootTable(e.getCocoon(), BlockTables::dropWithOrbs));

                Gemstone.stream().forEach(g -> {
                    if (g.generateCustomBlock())
                        this.registerDropSelfLootTable(g.getBlock());
                    if (g.generateCustomOre())
                        this.registerLootTable(g.getOre(), (block) -> droppingItemWithFortune(block, g.getItem()));
                });
            }

            protected static LootTable.Builder dropWithOrbs(Block block)
            {
                LootTable.Builder builder = LootTable.builder();
                for (Element e : Element.values)
                {
                    LootPool.Builder pool = LootPool.builder()
                            .rolls(ConstantRange.of(1))
                            .addEntry(ItemLootEntry.builder(e.getOrb())
                                    .acceptFunction(ApplyOrbSizeFunction.builder().with(e)));
                    builder = builder.addLootPool(withSurvivesExplosion(block, pool));
                }
                return builder;
            }

            @Override
            protected Iterable<Block> getKnownBlocks()
            {
                return ForgeRegistries.BLOCKS.getValues().stream()
                        .filter(b -> b.getRegistryName().getNamespace().equals(ElementsOfPowerMod.MODID))
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
            densityBlock(ElementsOfPowerBlocks.MIST, MistBlock.DENSITY);
            densityBlock(ElementsOfPowerBlocks.DUST, DustBlock.DENSITY);
            densityBlock(ElementsOfPowerBlocks.LIGHT, LightBlock.DENSITY, (value) -> ElementsOfPowerMod.location("transparent"));
            densityBlock(ElementsOfPowerBlocks.CUSHION, CushionBlock.DENSITY, (density) -> ElementsOfPowerMod.location("block/dust_" + density));
        }

        private void densityBlock(Block block, IntegerProperty densityProperty)
        {
            densityBlock(block, densityProperty, (density) -> ElementsOfPowerMod.location("block/" + Objects.requireNonNull(block.getRegistryName()).getPath() + "_" + density));
        }

        private void densityBlock(Block block, IntegerProperty densityProperty, Function<Integer, ResourceLocation> texMapper)
        {
            Map<Integer, ModelFile> densityModels = Maps.asMap(
                    new HashSet<>(densityProperty.getAllowedValues()),
                    density -> {
                        return models().cubeAll(ElementsOfPowerMod.location(Objects.requireNonNull(block.getRegistryName()).getPath() + "_" + density).getPath(), texMapper.apply(density));
                    });

            getVariantBuilder(block)
                    .forAllStates(state -> ConfiguredModel.builder()
                            .modelFile(densityModels.get(state.get(MistBlock.DENSITY)))
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
        protected void registerRecipes(Consumer<IFinishedRecipe> consumer)
        {
            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.ANALYZER)
                    .patternLine("glg")
                    .patternLine("i  ")
                    .patternLine("psp")
                    .key('l', Items.GOLD_INGOT)
                    .key('i', Items.IRON_INGOT)
                    .key('g', Tags.Items.GLASS_PANES)
                    .key('p', ItemTags.PLANKS)
                    .key('s', ItemTags.WOODEN_SLABS)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.ESSENTIALIZER)
                    .patternLine("IQI")
                    .patternLine("ONO")
                    .patternLine("IOI")
                    .key('I', Items.IRON_INGOT)
                    .key('O', Items.OBSIDIAN)
                    .key('Q', GemstoneExaminer.GEMSTONES)
                    .key('N', Items.NETHER_STAR)
                    .addCriterion("has_star", hasItem(Items.NETHER_STAR))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.WAND)
                    .patternLine(" G")
                    .patternLine("S ")
                    .key('G', Items.GOLD_INGOT)
                    .key('S', Items.STICK)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.STAFF)
                    .patternLine(" GW")
                    .patternLine(" SG")
                    .patternLine("S  ")
                    .key('G', Items.IRON_BLOCK)
                    .key('S', Items.STICK)
                    .key('W', ElementsOfPowerItems.WAND)
                    .addCriterion("has_wand", hasItem(ElementsOfPowerItems.WAND))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.RING)
                    .patternLine(" GG")
                    .patternLine("G G")
                    .patternLine(" G ")
                    .key('G', Items.GOLD_INGOT)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.NECKLACE)
                    .patternLine("GGG")
                    .patternLine("G G")
                    .patternLine(" G ")
                    .key('G', Items.GOLD_INGOT)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.HEADBAND)
                    .patternLine(" G ")
                    .patternLine("G G")
                    .patternLine("GGG")
                    .key('G', Items.GOLD_INGOT)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            CustomRecipeBuilder.customRecipe(GemstoneChangeRecipe.SERIALIZER).build(consumer, ElementsOfPowerMod.location("gemstone_change").toString());
            CustomRecipeBuilder.customRecipe(ContainerChargeRecipe.SERIALIZER).build(consumer, ElementsOfPowerMod.location("container_charge").toString());

            for (Gemstone gemstone : Gemstone.values())
            {
                if (gemstone.generateCustomOre())
                {
                    CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(gemstone.getOre()), gemstone, 1.0F, 200)
                            .addCriterion("has_ore", hasItem(gemstone.getOre()))
                            .build(consumer, ElementsOfPowerMod.location("smelting/" + gemstone.getString()));
                }
                if (gemstone.generateCustomBlock())
                {
                    ShapedRecipeBuilder.shapedRecipe(gemstone.getBlock())
                            .patternLine("ggg")
                            .patternLine("ggg")
                            .patternLine("ggg")
                            .key('g', AnalyzedFilteringIngredient.wrap(Ingredient.fromItems(gemstone.getItem())))
                            .addCriterion("has_item", hasItem(gemstone.getItem()))
                            .build(consumer);

                    ShapelessRecipeBuilder.shapelessRecipe(gemstone.getItem(), 9)
                            .addIngredient(Ingredient.fromItems(gemstone.getBlock()))
                            .addCriterion("has_item", hasItem(gemstone.getItem()))
                            .build(consumer, ElementsOfPowerMod.location(gemstone.getString() + "_from_block"));
                }
            }
        }
    }
}
