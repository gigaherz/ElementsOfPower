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

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.gigaherz.elementsofpower.ElementsOfPowerBlocks.*;

public enum Gemstone implements StringRepresentable, ItemLike
{
    RUBY(Element.FIRE, "ruby", 0xFFFF0000,
            () -> ElementsOfPowerItems.RUBY, false, () -> RUBY_BLOCK, List.of(() -> RUBY_ORE, () -> DEEPSLATE_RUBY_ORE),
            null, true), // red
    SAPPHIRE(Element.WATER, "sapphire", 0xFF0000FF,
            () -> ElementsOfPowerItems.SAPPHIRE, false, () -> SAPPHIRE_BLOCK, List.of(() -> SAPPHIRE_ORE, () -> DEEPSLATE_SAPPHIRE_ORE),
            null, true), // blue
    CITRINE(Element.AIR, "citrine", 0xFFFFFF00,
            () -> ElementsOfPowerItems.CITRINE, false, () -> CITRINE_BLOCK, List.of(() -> CITRINE_ORE, () -> DEEPSLATE_CITRINE_ORE),
            null, true), // yellow
    AGATE(Element.EARTH, "agate", 0xFF7F3F00,
            () -> ElementsOfPowerItems.AGATE, false, () -> AGATE_BLOCK, List.of(() -> AGATE_ORE, ()-> DEEPSLATE_AGATE_ORE),
            null, true), // brown
    QUARTZ(Element.LIGHT, "quartz", 0xFFFFFFFF,
            () -> ElementsOfPowerItems.QUARTZ, true, () -> () -> Blocks.QUARTZ_BLOCK, List.of(() -> () -> Blocks.NETHER_QUARTZ_ORE),
            () -> () -> Items.QUARTZ, false), // white
    ONYX(Element.TIME, "onyx", 0xFF0F0F0F,
            () -> ElementsOfPowerItems.ONYX, false, () -> ONYX_BLOCK, List.of(() -> ONYX_ORE, () -> DEEPSLATE_ONYX_ORE),
            null, true), // black
    EMERALD(Element.LIFE, "emerald", 0xFF00FF00,
            () -> ElementsOfPowerItems.EMERALD, true, () -> () -> Blocks.EMERALD_BLOCK, List.of(() -> () ->Blocks.EMERALD_ORE, () -> () ->Blocks.DEEPSLATE_EMERALD_ORE),
            () -> () -> Items.EMERALD, false), // green

    RUBELLITE(Element.CHAOS, "rubellite", 0xFFAF00FF,
            () -> ElementsOfPowerItems.RUBELLITE, false, () -> rubellite_BLOCK, List.of(() -> RUBELLITE_ORE, () -> DEEPSLATE_RUBELLITE_ORE),
            null, true), // purple

    DIAMOND(null, "diamond", 0xFF7FFFCF,
            () -> ElementsOfPowerItems.DIAMOND, true, () -> () ->Blocks.DIAMOND_BLOCK, List.of(() -> () ->Blocks.DIAMOND_ORE, () -> () ->Blocks.DEEPSLATE_DIAMOND_ORE),
            () -> () -> Items.DIAMOND, false), // clear

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

    private final boolean generateInWorld;

    Gemstone(@Nullable Element element, String name, int tintColor,
             @Nullable Supplier<Supplier<GemstoneItem>> itemSupplier,
             boolean isVanilla,
             @Nullable Supplier<Supplier<? extends Block>> blockSupplier,
             List<Supplier<Supplier<? extends Block>>> oreSuppliers,
             @Nullable Supplier<Supplier<? extends Item>> vanillaGemstoneSupplier,
             boolean generateInWorld)
    {
        this.element = element;
        this.name = name;
        this.tintColor = tintColor;
        this.isVanilla = isVanilla;
        this.blockSupplier = blockSupplier != null ? () -> blockSupplier.get().get() : null;
        this.oreSuppliers = oreSuppliers.stream().<Supplier<Block>>map(s -> () -> s.get().get()).toList();
        this.itemSupplier = itemSupplier != null ? () -> itemSupplier.get().get() : null;;
        this.vanillaGemstoneSupplier = vanillaGemstoneSupplier != null ? () -> vanillaGemstoneSupplier.get().get() : null;
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
        return "text.elementsofpower.gem_container." + name;
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

    public static void forEach(Consumer<Gemstone> consumer)
    {
        values.forEach(consumer);
    }
}
