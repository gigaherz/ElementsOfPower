package dev.gigaherz.elementsofpower;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.information.datagen.ForcedInformationProvider;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import dev.gigaherz.elementsofpower.advancements.SpellCastTrigger;
import dev.gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction;
import dev.gigaherz.elementsofpower.cocoons.CocoonFeature;
import dev.gigaherz.elementsofpower.cocoons.CocoonPlacement;
import dev.gigaherz.elementsofpower.database.GemstoneExaminer;
import dev.gigaherz.elementsofpower.database.StockConversions;
import dev.gigaherz.elementsofpower.gemstones.*;
import dev.gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import dev.gigaherz.elementsofpower.misc.TextureVariantsGen;
import dev.gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import dev.gigaherz.elementsofpower.recipes.GemstoneChangeRecipe;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import dev.gigaherz.elementsofpower.spells.blocks.DustBlock;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import dev.gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;

import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gigaherz.elementsofpower.ElementsOfPowerMod.location;

class ElementsOfPowerDataGen
{
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new Recipes(gen.getPackOutput()));
        gen.addProvider(event.includeServer(), LootGen.create(gen.getPackOutput()));

        BlockTagGens blockTags = new BlockTagGens(gen.getPackOutput(), event.getLookupProvider(), existingFileHelper);
        ItemTagGens itemTags = new ItemTagGens(gen.getPackOutput(), event.getLookupProvider(), existingFileHelper);
        gen.addProvider(event.includeServer(), blockTags);
        gen.addProvider(event.includeServer(), itemTags);

        gen.addProvider(event.includeServer(), new RegistryProvider(gen.getPackOutput(), event.getLookupProvider()));

        if ("true".equals(System.getProperty("elementsofpower.doAequivaleoDatagen", "true")) && ModList.get().isLoaded("aequivaleo"))
        {
            gen.addProvider(event.includeServer(), new AequivaleoGens(gen, itemTags, event.getLookupProvider()));
        }

        gen.addProvider(event.includeClient(), new GearTexturesGen(gen.getPackOutput(), existingFileHelper));

        gen.addProvider(event.includeClient(), new BlockStates(gen.getPackOutput(), existingFileHelper));

        gen.addProvider(event.includeServer(), new AdvancementProvider(gen.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper(), List.of(new AdvancementsGen())));
    }

    private static class AdvancementsGen
            implements AdvancementProvider.AdvancementGenerator
    {
        private static final ResourceLocation TAB_BACKGROUND = new ResourceLocation("minecraft:textures/block/obsidian.png");


        @Override
        public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper)
        {
            var root = Advancement.Builder.advancement()
                    .display(ElementsOfPowerItems.WAND.get().getStack(Gemstone.RUBY),
                            Component.translatable("advancement.elementsofpower.root.title"),
                            Component.translatable("advancement.elementsofpower.root.description"),
                            TAB_BACKGROUND, AdvancementType.GOAL, false, false, false)
                    /* criterions */
                    /* unlocks */
                    .addCriterion("dummy", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[0]))
                    .save(saver, location("root"), existingFileHelper);

            var discover_gems = Advancement.Builder.advancement()
                    .parent(root)
                    .display(ElementsOfPowerItems.WAND.get().getStack(Gemstone.RUBY),
                            Component.translatable("advancement.elementsofpower.discover_gems.title"),
                            Component.translatable("advancement.elementsofpower.discover_gems.description"),
                            null, AdvancementType.GOAL, true, false, false)
                    .addCriterion("has_gemstone", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.RUBY.get(),
                            ElementsOfPowerItems.SAPPHIRE.get(),
                            ElementsOfPowerItems.CITRINE.get(),
                            ElementsOfPowerItems.AGATE.get(),
                            ElementsOfPowerItems.QUARTZ.get(), Items.DIAMOND,
                            ElementsOfPowerItems.ONYX.get(),
                            ElementsOfPowerItems.EMERALD.get(),
                            ElementsOfPowerItems.RUBELLITE.get(),
                            ElementsOfPowerItems.DIAMOND.get(), Items.DIAMOND,
                            ElementsOfPowerItems.CREATIVITE.get()))
                    .save(saver, location("discover_gems"), existingFileHelper);

            var acquire_wand = Advancement.Builder.advancement()
                    .parent(discover_gems)
                    .display(ElementsOfPowerItems.WAND.get().getStack(Gemstone.RUBY),
                            Component.translatable("advancement.elementsofpower.acquire_wand.title"),
                            Component.translatable("advancement.elementsofpower.acquire_wand.description"),
                            null, AdvancementType.GOAL, true, false, false)
                    .addCriterion("has_wand", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.STAFF.get()))
                    .save(saver, location("acquire_wand"), existingFileHelper);

            var first_spell = Advancement.Builder.advancement()
                    .parent(acquire_wand)
                    .display(ElementsOfPowerItems.WAND.get().getStack(Gemstone.RUBY),
                            Component.translatable("advancement.elementsofpower.first_spell.title"),
                            Component.translatable("advancement.elementsofpower.first_spell.description"),
                            null, AdvancementType.GOAL, true, false, false)
                    .addCriterion("spell_cast", SpellCastTrigger.TriggerInstance.playerCastsSpell())
                    /* unlocks */
                    .save(saver, location("first_spell"), existingFileHelper);

            var acquire_staff = Advancement.Builder.advancement()
                    .parent(acquire_wand)
                    .display(ElementsOfPowerItems.WAND.get().getStack(Gemstone.RUBY),
                            Component.translatable("advancement.elementsofpower.acquire_staff.title"),
                            Component.translatable("advancement.elementsofpower.acquire_staff.description"),
                            null, AdvancementType.GOAL, true, false, false)
                    .addCriterion("has_staff", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.STAFF.get()))
                    .save(saver, location("acquire_staff"), existingFileHelper);

            //var advanced_spell = Advancement.Builder.advancement()
            //        .parent(/*first_spell*/acquire_wand)
            //        .display(ElementsOfPowerItems.WAND.get().getStack(Gemstone.RUBY),
            //                Component.translatable("advancement.elementsofpower.advanced_spell.title"),
            //                Component.translatable("advancement.elementsofpower.advanced_spell.description"),
            //                null, AdvancementType.GOAL, true, false, false)
            //        /* criterions */
            //        /* unlocks */
            //        .save(saver, location("advanced_spell"), existingFileHelper);

            var acquire_trinket = Advancement.Builder.advancement()
                    .parent(discover_gems)
                    .display(ElementsOfPowerItems.WAND.get().getStack(Gemstone.RUBY),
                            Component.translatable("advancement.elementsofpower.acquire_trinket.title"),
                            Component.translatable("advancement.elementsofpower.acquire_trinket.description"),
                            null, AdvancementType.GOAL, true, false, false)
                    .addCriterion("has_ring", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.RING.get()))
                    .addCriterion("has_necklace", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.NECKLACE.get()))
                    .addCriterion("has_bracelet", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.BRACELET.get()))
                    .requirements(AdvancementRequirements.Strategy.OR)
                    .save(saver, location("acquire_trinket"), existingFileHelper);

            //var master_spell = Advancement.Builder.advancement()
            //        .parent(advanced_spell)
            //        .display(ElementsOfPowerItems.WAND.get().getStack(Gemstone.RUBY),
            //                Component.translatable("advancement.elementsofpower.master_spell.title"),
            //                Component.translatable("advancement.elementsofpower.master_spell.description"),
            //                null, AdvancementType.GOAL, true, true, false)
            //        /* criterions */
            //        /* unlocks */
            //        .save(saver, location("master_spell"), existingFileHelper);

            var fully_geared_up = Advancement.Builder.advancement()
                    .parent(acquire_staff)
                    .display(ElementsOfPowerItems.WAND.get().getStack(Gemstone.RUBY),
                            Component.translatable("advancement.elementsofpower.fully_geared_up.title"),
                            Component.translatable("advancement.elementsofpower.fully_geared_up.description"),
                            null, AdvancementType.GOAL, true, true, false)
                    .addCriterion("has_staff", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.STAFF.get()))
                    .addCriterion("has_ring", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.RING.get()))
                    .addCriterion("has_necklace", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.NECKLACE.get()))
                    .addCriterion("has_bracelet", InventoryChangeTrigger.TriggerInstance.hasItems(ElementsOfPowerItems.BRACELET.get()))
                    .requirements(AdvancementRequirements.Strategy.AND)
                    .save(saver, location("fully_geared_up"), existingFileHelper);
        }
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
                ResourceLocation targetPaletteFile = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(gem.getItem()));
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

        protected AequivaleoGens(DataGenerator dataGenerator, ItemTagGens itemTags, CompletableFuture<HolderLookup.Provider> holderLookupProvider)
        {
            super(ElementsOfPowerMod.MODID, dataGenerator, holderLookupProvider);
            this.itemTags = itemTags;
        }

        @Override
        public void calculateDataToSave()
        {
            StockConversions.addStockConversions(this::getItemsFromTag, (item, amounts) -> {
                List<CompoundInstance> compoundRefs = Element.stream_without_balance()
                        .map(e -> Pair.of(e, amounts.get(e)))
                        .filter(p -> p.getSecond() > 0)
                        .map(p -> new CompoundInstance(AequivaleoPlugin.BY_ELEMENT.get(p.getFirst()).value(), (double) p.getSecond()))
                        .collect(Collectors.toList());

                List<Object> gameObjects = Lists.newArrayList();
                /*if (item instanceof GemstoneItem gem)
                {
                    gameObjects.add(item);
                    gem.creativeTabStacks(gameObjects::add);
                }
                else*/
                {
                    gameObjects.add(item);
                    //gameObjects.add(item.getDefaultInstance());
                }
                save(specFor(gameObjects).withCompounds(compoundRefs));
            });
        }

        @SuppressWarnings("deprecation")
        private List<Item> getItemsFromTag(ResourceLocation rl, List<Item> fallback)
        {
            TagBuilder tagbuilder = itemTags.getTagByName(rl);
            if (tagbuilder == null)
            {
                var tag = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, rl));
                return tag.map(holders -> holders.stream().map(Holder::value).toList()).orElse(fallback);
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
                return Stream.of(BuiltInRegistries.ITEM.get(itemId));
            }
        }
    }

    private static class ItemTagGens extends IntrinsicHolderTagsProvider<Item>
    {
        @SuppressWarnings("deprecation")
        public ItemTagGens(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper existingFileHelper)
        {
            super(packOutput, Registries.ITEM, lookup, (item) -> item.builtInRegistryHolder().key(), ElementsOfPowerMod.MODID, existingFileHelper);
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
        @SuppressWarnings("deprecation")
        public BlockTagGens(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper existingFileHelper)
        {
            super(packOutput, Registries.BLOCK, lookup, (block) -> block.builtInRegistryHolder().key(), ElementsOfPowerMod.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookup)
        {
            this.tag(CocoonFeature.REPLACEABLE_TAG)
                    .add(Blocks.SAND, Blocks.RED_SAND, Blocks.DIRT, Blocks.NETHERRACK);

            this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ElementsOfPowerBlocks.ESSENTIALIZER.get());
            this.tag(BlockTags.NEEDS_IRON_TOOL).add(ElementsOfPowerBlocks.ESSENTIALIZER.get());

            this.tag(BlockTags.MINEABLE_WITH_AXE).add(ElementsOfPowerBlocks.ANALYZER.get());

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
                this.dropSelf(ElementsOfPowerBlocks.ANALYZER.get());

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
                return BuiltInRegistries.BLOCK.entrySet().stream()
                        .filter(e -> e.getKey().location().getNamespace().equals(ElementsOfPowerMod.MODID))
                        .map(Map.Entry::getValue)
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
            densityBlock(ElementsOfPowerBlocks.LIGHT.get(), LightBlock.DENSITY, (value) -> location("transparent"));
            densityBlock(ElementsOfPowerBlocks.CUSHION.get(), CushionBlock.DENSITY, (density) -> location("block/dust_" + density));
        }

        private void densityBlock(Block block, IntegerProperty densityProperty)
        {
            densityBlock(block, densityProperty, (density) -> location("block/" + Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getPath() + "_" + density));
        }

        private void densityBlock(Block block, IntegerProperty densityProperty, Function<Integer, ResourceLocation> texMapper)
        {
            Map<Integer, ModelFile> densityModels = Maps.asMap(
                    new HashSet<>(densityProperty.getPossibleValues()),
                    density -> {
                        return models().cubeAll(location(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getPath() + "_" + density).getPath(), texMapper.apply(density));
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
        protected void buildRecipes(RecipeOutput recipeOutput)
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
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.ESSENTIALIZER.get())
                    .pattern("IQI")
                    .pattern("ONO")
                    .pattern("IOI")
                    .define('I', Items.IRON_INGOT)
                    .define('O', Items.OBSIDIAN)
                    .define('Q', GemstoneExaminer.GEMSTONES)
                    .define('N', Items.NETHER_STAR)
                    .unlockedBy("has_star", has(Items.NETHER_STAR))
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.WAND.get())
                    .pattern(" G")
                    .pattern("S ")
                    .define('G', Items.GOLD_INGOT)
                    .define('S', Items.STICK)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.STAFF.get())
                    .pattern(" GW")
                    .pattern(" SG")
                    .pattern("S  ")
                    .define('G', Items.IRON_BLOCK)
                    .define('S', Items.STICK)
                    .define('W', ElementsOfPowerItems.WAND.get())
                    .unlockedBy("has_wand", has(ElementsOfPowerItems.WAND.get()))
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.RING.get())
                    .pattern(" GG")
                    .pattern("G G")
                    .pattern(" G ")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.NECKLACE.get())
                    .pattern("GGG")
                    .pattern("G G")
                    .pattern(" G ")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(recipeOutput);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ElementsOfPowerItems.BRACELET.get())
                    .pattern(" G ")
                    .pattern("G G")
                    .pattern("GGG")
                    .define('G', Items.GOLD_INGOT)
                    .unlockedBy("has_gold", has(Items.GOLD_INGOT))
                    .save(recipeOutput);

            SpecialRecipeBuilder.special(GemstoneChangeRecipe::new).save(recipeOutput, location("gemstone_change").toString());
            SpecialRecipeBuilder.special(ContainerChargeRecipe::new).save(recipeOutput, location("container_charge").toString());

            for (Gemstone gem : Gemstone.values())
            {
                if (gem.generateCustomOre())
                {
                    TagKey<Item> tag = itemTag(new ResourceLocation("elementsofpower", gem.getSerializedName() + "_ores").toString());
                    Item[] oreItems = gem.getOres().stream().map(Block::asItem).toArray(Item[]::new);
                    SimpleCookingRecipeBuilder.smelting(Ingredient.of(oreItems), RecipeCategory.MISC, gem, 1.0F, 200)
                            .unlockedBy("has_ore", has(tag))
                            .save(recipeOutput, location("smelting/" + gem.getSerializedName()));
                }
                if (gem.generateCustomBlock())
                {
                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, gem.getBlock())
                            .pattern("ggg")
                            .pattern("ggg")
                            .pattern("ggg")
                            .define('g', AnalyzedFilteringIngredient.wrap(Ingredient.of(gem.getItem())))
                            .unlockedBy("has_item", has(gem.getItem()))
                            .save(recipeOutput);

                    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, gem.getItem(), 9)
                            .requires(Ingredient.of(gem.getBlock()))
                            .unlockedBy("has_item", has(gem.getItem()))
                            .save(recipeOutput, location(gem.getSerializedName() + "_from_block"));
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

        private static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        private static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        private static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_COCOON = ResourceKey.create(Registries.CONFIGURED_FEATURE, location("overworld_cocoon"));
        private static final ResourceKey<PlacedFeature> PLACED_COCOON = ResourceKey.create(Registries.PLACED_FEATURE, location("overworld_cocoon"));
        private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, context -> {
                    context.register(CONFIGURED_COCOON, new ConfiguredFeature<>(ElementsOfPowerMod.COCOON_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
                    for (Gemstone g : Gemstone.values)
                    {
                        if (g.generateInWorld())
                        {
                            var name2 = g.getSerializedName() + "_ore";
                            var key = ResourceKey.create(Registries.CONFIGURED_FEATURE, location(name2));
                            var map = new HashMap<BiomeValues, OreConfiguration>();

                            for (var heat : BiomeValue.values())
                            {
                                for (var humidity : BiomeValue.values())
                                {
                                    for (var life : BiomeValue.values())
                                    {
                                        var values = new BiomeValues(heat, humidity, life);

                                        var ores = g.getOres();
                                        var stone_ore = ores.get(0);
                                        var deepslate_ore = ores.size() >= 2 ? ores.get(1) : null;

                                        List<OreConfiguration.TargetBlockState> targets = new ArrayList<>();

                                        targets.add(OreConfiguration.target(STONE_ORE_REPLACEABLES, stone_ore.defaultBlockState()));

                                        if (deepslate_ore != null)
                                            targets.add(OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, deepslate_ore.defaultBlockState()));

                                        int numPerVein = 3 + values.getBiomeBonus(g.getElement());
                                        map.put(values, new OreConfiguration(targets, numPerVein, 0.0f));
                                    }
                                }
                            }

                            context.register(key, new ConfiguredFeature<>(ElementsOfPowerMod.GEMSTONE_ORE_FEATURE.get(), new GemstoneOreFeature.Configuration(map)));
                        }
                    }
                })
                .add(Registries.PLACED_FEATURE, context -> {
                    var configuredFeatureRegistry = context.lookup(Registries.CONFIGURED_FEATURE);
                    context.register(PLACED_COCOON, new PlacedFeature(configuredFeatureRegistry.get(CONFIGURED_COCOON).orElseThrow(), List.of(CocoonPlacement.INSTANCE)));
                    for (Gemstone g : Gemstone.values)
                    {
                        if (g.generateInWorld())
                        {
                            var name2 = g.getSerializedName() + "_ore";
                            var key = ResourceKey.create(Registries.CONFIGURED_FEATURE, location(name2));
                            var key2 = ResourceKey.create(Registries.PLACED_FEATURE, location(name2));
                            var configured = configuredFeatureRegistry.get(key).orElseThrow();
                            context.register(key2, new PlacedFeature(configured, List.of(
                                    CountPlacement.of(10),
                                    InSquarePlacement.spread(),
                                    HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)),
                                    BiomeFilter.biome())));
                        }
                    }
                })
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
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

                    final HolderSet.Named<Biome> isVoid = biomes.get(Tags.Biomes.IS_VOID).orElseThrow();
                    context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, location("cocoon")),
                            new BiomeModifiers.AddFeaturesBiomeModifier(
                            new NotHolderSet<>(biomes0, isVoid),
                            HolderSet.direct(placedFeatures.get(PLACED_COCOON).orElseThrow()),
                            GenerationStep.Decoration.UNDERGROUND_DECORATION
                    ));

                    final HolderSet.Named<Biome> isOverworld = biomes.get(BiomeTags.IS_OVERWORLD).orElseThrow();
                    for (Gemstone g : Gemstone.values)
                    {
                        if (g.generateInWorld())
                        {
                            var name2 = g.getSerializedName() + "_ore";
                            var key2 = ResourceKey.create(Registries.PLACED_FEATURE, location(name2));
                            var placed = placedFeatures.get(key2).orElseThrow();
                            var key = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, location(name2));

                            context.register(key, new BiomeModifiers.AddFeaturesBiomeModifier(
                                    isOverworld,
                                    HolderSet.direct(placed),
                                    GenerationStep.Decoration.UNDERGROUND_ORES
                            ));
                        }
                    }
                });
    }

    private static TagKey<Item> itemTag(String tagName) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(tagName));
    }

    private static TagKey<Block> blockTag(String tagName) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(tagName));
    }
}
