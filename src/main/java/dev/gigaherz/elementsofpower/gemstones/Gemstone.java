package dev.gigaherz.elementsofpower.gemstones;

import com.google.common.collect.ImmutableList;
import dev.gigaherz.elementsofpower.ElementsOfPowerItems;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static dev.gigaherz.elementsofpower.ElementsOfPowerBlocks.*;
import static dev.gigaherz.elementsofpower.ElementsOfPowerItems.*;

public enum Gemstone implements StringRepresentable, ItemLike
{
    RUBY(Element.FIRE, "ruby", 0xFFFF0000,
            () -> ElementsOfPowerItems.RUBY, false, () -> RUBY_BLOCK, List.of(() -> RUBY_ORE, () -> DEEPSLATE_RUBY_ORE),
            () -> RUBY_SPELLDUST, null, true), // red
    SAPPHIRE(Element.WATER, "sapphire", 0xFF0000FF,
            () -> ElementsOfPowerItems.SAPPHIRE, false, () -> SAPPHIRE_BLOCK, List.of(() -> SAPPHIRE_ORE, () -> DEEPSLATE_SAPPHIRE_ORE),
            () -> SAPPHIRE_SPELLDUST, null, true), // blue
    CITRINE(Element.AIR, "citrine", 0xFFFFFF00,
            () -> ElementsOfPowerItems.CITRINE, false, () -> CITRINE_BLOCK, List.of(() -> CITRINE_ORE, () -> DEEPSLATE_CITRINE_ORE),
            () -> CITRINE_SPELLDUST, null, true), // yellow
    AGATE(Element.EARTH, "agate", 0xFF7F3F00,
            () -> ElementsOfPowerItems.AGATE, false, () -> AGATE_BLOCK, List.of(() -> AGATE_ORE, ()-> DEEPSLATE_AGATE_ORE),
            () -> AGATE_SPELLDUST, null, true), // brown
    QUARTZ(Element.LIGHT, "quartz", 0xFFFFFFFF,
            () -> ElementsOfPowerItems.QUARTZ, true, () -> Blocks.QUARTZ_BLOCK, List.of(() -> Blocks.NETHER_QUARTZ_ORE),
            () -> QUARTZ_SPELLDUST, () -> Items.QUARTZ, false), // white
    SERENDIBITE(Element.DARKNESS, "serendibite", 0xFF0F0F0F,
            () -> ElementsOfPowerItems.SERENDIBITE, false, () -> SERENDIBITE_BLOCK, List.of(() -> SERENDIBITE_ORE, () -> DEEPSLATE_SERENDIBITE_ORE),
            () -> SERENDIBITE_SPELLDUST, null, true), // black
    EMERALD(Element.LIFE, "emerald", 0xFF00FF00,
            () -> ElementsOfPowerItems.EMERALD, true, () -> Blocks.EMERALD_BLOCK, List.of(() -> Blocks.EMERALD_ORE, () -> Blocks.DEEPSLATE_EMERALD_ORE),
            () -> EMERALD_SPELLDUST, () -> Items.EMERALD, false), // green

    @Deprecated(forRemoval = true)
    AMETHYST(Element.DEATH, "amethyst", 0xFFAF00FF,
            () -> ElementsOfPowerItems.AMETHYST, false, () -> AMETHYST_BLOCK, List.of(() -> AMETHYST_ORE),
            () -> AMETHYST_SPELLDUST, null, false), // purple

    ELBAITE(Element.DEATH, "elbaite", 0xFFAF00FF,
            () -> ElementsOfPowerItems.ELBAITE, false, () -> ELBAITE_BLOCK, List.of(() -> ELBAITE_ORE, () -> DEEPSLATE_ELBAITE_ORE),
            null, true), // purple

    DIAMOND(null, "diamond", 0xFF7FFFCF,
            () -> ElementsOfPowerItems.DIAMOND, true, () -> Blocks.DIAMOND_BLOCK, List.of(() -> Blocks.DIAMOND_ORE, () -> Blocks.DEEPSLATE_DIAMOND_ORE),
            () -> DIAMOND_SPELLDUST, () -> Items.DIAMOND, false), // clear

    CREATIVITE(null, "creativite", 0xFF000000,
            () -> ElementsOfPowerItems.CREATIVITE, false, null, List.of(),
            null, false);

    private final Element element;
    private final String name;
    private final int tintColor;

    private final boolean isVanilla;
    private final Supplier<GemstoneItem> itemSupplier;
    @Nullable
    private final Supplier<Block> blockSupplier;
    private final List<Supplier<Block>> oreSuppliers;
    @Nullable
    private final Supplier<Item> vanillaGemstoneSupplier;

    @Deprecated(forRemoval = true)
    @Nullable
    private final Supplier<Item> spelldustItemSupplier;

    private final boolean generateInWorld;

    Gemstone(@Nullable Element element, String name, int tintColor,
             @Nullable Supplier<GemstoneItem> itemSupplier,
             boolean isVanilla, @Nullable Supplier<Block> blockSupplier, List<Supplier<Block>> oreSuppliers, @Nullable Supplier<Item> vanillaGemstoneSupplier, boolean generateInWorld)
    {
        this(element, name, tintColor, itemSupplier, isVanilla, blockSupplier, oreSuppliers, null, vanillaGemstoneSupplier, generateInWorld);
    }

    @Deprecated(forRemoval = true)
    Gemstone(@Nullable Element element, String name, int tintColor,
             @Nullable Supplier<GemstoneItem> itemSupplier,
             boolean isVanilla, @Nullable Supplier<Block> blockSupplier, List<Supplier<Block>> oreSuppliers,
             @Nullable Supplier<Item> spelldustItemSupplier, @Nullable Supplier<Item> vanillaGemstoneSupplier, boolean generateInWorld)
    {
        this.element = element;
        this.name = name;
        this.tintColor = tintColor;
        this.isVanilla = isVanilla;
        this.blockSupplier = blockSupplier;
        this.oreSuppliers = oreSuppliers;
        this.itemSupplier = itemSupplier;
        this.spelldustItemSupplier = spelldustItemSupplier;
        this.vanillaGemstoneSupplier = vanillaGemstoneSupplier;
        this.generateInWorld = generateInWorld;
    }

    @Nullable
    public Element getElement()
    {
        return element;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }

    public String getContainerTranslationKey()
    {
        return "elementsofpower.gem_container." + name;
    }

    public int getTintColor()
    {
        return tintColor;
    }

    public boolean generateCustomBlock()
    {
        return !isVanilla && blockSupplier != null;
    }

    public boolean generateCustomOre()
    {
        return !isVanilla && oreSuppliers.size() > 0;
    }

    public boolean generateInWorld()
    {
        return generateCustomOre() && generateInWorld;
    }

    @Deprecated(forRemoval = true)
    public boolean generateSpelldust()
    {
        return spelldustItemSupplier != null;
    }

    public boolean isVanilla()
    {
        return isVanilla;
    }

    @Override
    public Item asItem()
    {
        return getItem();
    }

    public Block getBlock()
    {
        return blockSupplier != null ? blockSupplier.get() : Blocks.AIR;
    }

    public List<Block> getOres()
    {
        return oreSuppliers.stream().map(Supplier::get).toList();
    }

    public GemstoneItem getItem()
    {
        return Objects.requireNonNull(itemSupplier).get();
    }

    @Nullable
    public Item getVanillaItem()
    {
        return vanillaGemstoneSupplier != null ? vanillaGemstoneSupplier.get() : null;
    }

    @Deprecated(forRemoval = true)
    public Item getSpelldust()
    {
        return spelldustItemSupplier != null ? spelldustItemSupplier.get() : Items.AIR;
    }

    @Nullable
    public static Gemstone byName(String name)
    {
        for (Gemstone g : values())
        {
            if (g.getSerializedName().equals(name))
                return g;
        }
        return null;
    }

    public Item[] getTagItems()
    {
        if (vanillaGemstoneSupplier != null)
            return new Item[]{getItem(), vanillaGemstoneSupplier.get()};
        else
            return new Item[]{getItem()};
    }

    public static final ImmutableList<Gemstone> values = ImmutableList.copyOf(values());

    public static Stream<Gemstone> stream()
    {
        return values.stream();
    }
}
