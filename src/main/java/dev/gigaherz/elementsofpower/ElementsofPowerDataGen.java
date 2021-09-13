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
import dev.gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import dev.gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import dev.gigaherz.elementsofpower.recipes.GemstoneChangeRecipe;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import dev.gigaherz.elementsofpower.spells.blocks.DustBlock;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import dev.gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
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
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
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
            gen.addProvider(new LootGen(gen));

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
                item.fillItemCategory(CreativeModeTab.TAB_SEARCH, stacks);
                gameObjects.addAll(stacks);
                save(specFor(gameObjects).withCompounds(compoundRefs));
            });
        }

        private List<Item> getItemsFromTag(ResourceLocation rl, List<Item> fallback)
        {
            Tag.Builder tag = itemTags.getTagByName(rl);
            if (tag == null)
                return fallback;
            return tag.getEntries().flatMap(this::getItemsFromTag).collect(Collectors.toList());
        }

        private Stream<Item> getItemsFromTag(Tag.BuilderEntry proxy)
        {
            Tag.Entry entry = proxy.getEntry();
            if (entry instanceof Tag.ElementEntry)
            {
                ResourceLocation itemId = new ResourceLocation(((Tag.ElementEntry) entry).toString());
                return Stream.of(ForgeRegistries.ITEMS.getValue(itemId));
            }
            if (entry instanceof Tag.TagEntry)
            {
                ResourceLocation tagId = ((Tag.TagEntry) entry).getId();
                return getItemsFromTag(tagId, Collections.emptyList()).stream();
            }
            return Stream.empty();
        }
    }

    private static class ItemTagGens extends ItemTagsProvider implements DataProvider
    {
        public ItemTagGens(DataGenerator gen, BlockTagsProvider blockTags, ExistingFileHelper existingFileHelper)
        {
            super(gen, blockTags, ElementsOfPowerMod.MODID, existingFileHelper);
        }

        @Nullable
        public Tag.Builder getTagByName(ResourceLocation tag)
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
                    Tag.Named<Item> tag = ItemTags.bind(new ResourceLocation("forge", "gems/" + gem.getSerializedName()).toString());
                    this.tag(tag).add(gem.getTagItems());
                    gemsTag.addTag(tag);

                    Tag.Named<Item> eTag = GemstoneExaminer.GEMS.get(gem);
                    this.tag(eTag).add(gem.getTagItems());
                    examinerTag.addTag(eTag);
                }
                if (gem.generateCustomOre())
                {
                    Block ore = gem.getOre();
                    Tag.Named<Item> tag = ItemTags.bind(new ResourceLocation("forge", "ores/" + gem.getSerializedName()).toString());
                    this.tag(tag).add(ore.asItem());
                    oresTag.addTag(tag);
                }
                if (gem.generateCustomBlock())
                {
                    Block block = gem.getBlock();
                    Tag.Named<Item> tag = ItemTags.bind(new ResourceLocation("forge", "storage_blocks/" + gem.getSerializedName()).toString());
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

            for (Gemstone type : Gemstone.values())
            {
                if (type.generateCustomBlock())
                {
                    this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                            .add(type.getBlock());
                }
                if (type.generateCustomOre())
                {
                    this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                            .add(type.getBlock());
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
                this.dropSelf(ElementsOfPowerBlocks.ESSENTIALIZER);

                Element.stream().forEach(e -> this.add(e.getCocoon(), BlockTables::dropWithOrbs));

                Gemstone.stream().forEach(g -> {
                    if (g.generateCustomBlock())
                        this.dropSelf(g.getBlock());
                    if (g.generateCustomOre())
                        this.add(g.getOre(), (block) -> createOreDrop(block, g.getItem()));
                });
            }

            protected static LootTable.Builder dropWithOrbs(Block block)
            {
                LootTable.Builder builder = LootTable.lootTable();
                for (Element e : Element.values)
                {
                    LootPool.Builder pool = LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(e.getOrb())
                                    .apply(ApplyOrbSizeFunction.builder().with(e)));
                    builder = builder.withPool(applyExplosionCondition(block, pool));
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
                    new HashSet<>(densityProperty.getPossibleValues()),
                    density -> {
                        return models().cubeAll(ElementsOfPowerMod.location(Objects.requireNonNull(block.getRegistryName()).getPath() + "_" + density).getPath(), texMapper.apply(density));
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
            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.ANALYZER)
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

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.ESSENTIALIZER)
                    .pattern("IQI")
                    .pattern("ONO")
                    .pattern("IOI")
                    .define('I', Items.IRON_INGOT)
                    .define('O', Items.OBSIDIAN)
                    .define('Q', GemstoneExaminer.GEMSTONES)
                    .define('N', Items.NETHER_STAR)
                    .unlockedBy("has_star", has(Items.NETHER_STAR))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.WAND)
                    .pattern(" G")
                    .pattern("S ")
                    .define('G', Items.GOLD_INGOT)
                    .define('S', Items.STICK)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.STAFF)
                    .pattern(" GW")
                    .pattern(" SG")
                    .pattern("S  ")
                    .define('G', Items.IRON_BLOCK)
                    .define('S', Items.STICK)
                    .define('W', ElementsOfPowerItems.WAND)
                    .unlockedBy("has_wand", has(ElementsOfPowerItems.WAND))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.RING)
                    .pattern(" GG")
                    .pattern("G G")
                    .pattern(" G ")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.NECKLACE)
                    .pattern("GGG")
                    .pattern("G G")
                    .pattern(" G ")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ElementsOfPowerItems.HEADBAND)
                    .pattern(" G ")
                    .pattern("G G")
                    .pattern("GGG")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(consumer);

            SpecialRecipeBuilder.special(GemstoneChangeRecipe.SERIALIZER).save(consumer, ElementsOfPowerMod.location("gemstone_change").toString());
            SpecialRecipeBuilder.special(ContainerChargeRecipe.SERIALIZER).save(consumer, ElementsOfPowerMod.location("container_charge").toString());

            for (Gemstone gemstone : Gemstone.values())
            {
                if (gemstone.generateCustomOre())
                {
                    SimpleCookingRecipeBuilder.smelting(Ingredient.of(gemstone.getOre()), gemstone, 1.0F, 200)
                            .unlockedBy("has_ore", has(gemstone.getOre()))
                            .save(consumer, ElementsOfPowerMod.location("smelting/" + gemstone.getSerializedName()));
                }
                if (gemstone.generateCustomBlock())
                {
                    ShapedRecipeBuilder.shaped(gemstone.getBlock())
                            .pattern("ggg")
                            .pattern("ggg")
                            .pattern("ggg")
                            .define('g', AnalyzedFilteringIngredient.wrap(Ingredient.of(gemstone.getItem())))
                            .unlockedBy("has_item", has(gemstone.getItem()))
                            .save(consumer);

                    ShapelessRecipeBuilder.shapeless(gemstone.getItem(), 9)
                            .requires(Ingredient.of(gemstone.getBlock()))
                            .unlockedBy("has_item", has(gemstone.getItem()))
                            .save(consumer, ElementsOfPowerMod.location(gemstone.getSerializedName() + "_from_block"));
                }
            }
        }
    }
}
