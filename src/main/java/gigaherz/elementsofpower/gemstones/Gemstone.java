package gigaherz.elementsofpower.gemstones;

import com.google.common.collect.ImmutableList;
import gigaherz.elementsofpower.ElementsOfPowerBlocks;
import gigaherz.elementsofpower.ElementsOfPowerItems;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum Gemstone implements IStringSerializable, IItemProvider
{
    RUBY(Element.FIRE, "ruby", 0xFFFF0000,
            () -> ElementsOfPowerItems.RUBY, false, () -> ElementsOfPowerBlocks.RUBY_BLOCK, () -> ElementsOfPowerBlocks.RUBY_ORE,
            true, () -> ElementsOfPowerItems.RUBY_SPELLDUST, null), // red
    SAPPHIRE(Element.WATER, "sapphire", 0xFF0000FF,
            () -> ElementsOfPowerItems.SAPPHIRE, false, () -> ElementsOfPowerBlocks.SAPPHIRE_BLOCK, () -> ElementsOfPowerBlocks.SAPPHIRE_ORE,
            true, () -> ElementsOfPowerItems.SAPPHIRE_SPELLDUST, null), // blue
    CITRINE(Element.AIR, "citrine", 0xFFFFFF00,
            () -> ElementsOfPowerItems.CITRINE, false, () -> ElementsOfPowerBlocks.CITRINE_BLOCK, () -> ElementsOfPowerBlocks.CITRINE_ORE,
            true, () -> ElementsOfPowerItems.CITRINE_SPELLDUST, null), // yellow
    AGATE(Element.EARTH, "agate", 0xFF7F3F00,
            () -> ElementsOfPowerItems.AGATE, false, () -> ElementsOfPowerBlocks.AGATE_BLOCK, () -> ElementsOfPowerBlocks.AGATE_ORE,
            true, () -> ElementsOfPowerItems.AGATE_SPELLDUST, null), // brown
    QUARTZ(Element.LIGHT, "quartz", 0xFFFFFFFF,
            () -> ElementsOfPowerItems.QUARTZ, true, () -> Blocks.QUARTZ_BLOCK, () -> Blocks.NETHER_QUARTZ_ORE,
            true, () -> ElementsOfPowerItems.QUARTZ_SPELLDUST, () -> Items.QUARTZ), // white
    SERENDIBITE(Element.DARKNESS, "serendibite", 0xFF0F0F0F,
            () -> ElementsOfPowerItems.SERENDIBITE, false, () -> ElementsOfPowerBlocks.SERENDIBITE_BLOCK, () -> ElementsOfPowerBlocks.SERENDIBITE_ORE,
            true, () -> ElementsOfPowerItems.SERENDIBITE_SPELLDUST, null), // black
    EMERALD(Element.LIFE, "emerald", 0xFF00FF00,
            () -> ElementsOfPowerItems.EMERALD, true, () -> Blocks.EMERALD_BLOCK, () -> Blocks.EMERALD_ORE,
            true, () -> ElementsOfPowerItems.EMERALD_SPELLDUST, () -> Items.EMERALD), // green
    AMETHYST(Element.DEATH, "amethyst", 0xFFAF00FF,
            () -> ElementsOfPowerItems.AMETHYST, false, () -> ElementsOfPowerBlocks.AMETHYST_BLOCK, () -> ElementsOfPowerBlocks.AMETHYST_ORE,
            true, () -> ElementsOfPowerItems.AMETHYST_SPELLDUST, null), // purple

    DIAMOND(null, "diamond", 0xFF7FFFCF,
            () -> ElementsOfPowerItems.DIAMOND, true, () -> Blocks.DIAMOND_BLOCK, () -> Blocks.DIAMOND_ORE,
            true, () -> ElementsOfPowerItems.DIAMOND_SPELLDUST, () -> Items.DIAMOND), // clear

    CREATIVITE(null, "creativite", 0xFF000000,
            () -> ElementsOfPowerItems.CREATIVITE, false, null, null,
            false, null, null);

    private final Element element;
    private final String name;
    private final int tintColor;
    private final boolean isVanilla;
    private final Supplier<GemstoneItem> itemSupplier;
    @Nullable
    private final Supplier<Block> blockSupplier;
    @Nullable
    private final Supplier<Block> oreSupplier;
    @Nullable
    private final Supplier<Item> spelldustItemSupplier;
    @Nullable
    private final Supplier<Item> vanillaGemstoneSupplier;

    private final boolean generateSpelldust;

    Gemstone(@Nullable Element element, String name, int tintColor,
             @Nullable Supplier<GemstoneItem> itemSupplier,
             boolean isVanilla, @Nullable Supplier<Block> blockSupplier, @Nullable Supplier<Block> oreSupplier, boolean generateSpelldust, @Nullable Supplier<Item> spelldustItemSupplier, @Nullable Supplier<Item> vanillaGemstoneSupplier)
    {
        this.element = element;
        this.name = name;
        this.tintColor = tintColor;
        this.isVanilla = isVanilla;
        this.blockSupplier = blockSupplier;
        this.oreSupplier = oreSupplier;
        this.itemSupplier = itemSupplier;
        this.generateSpelldust = generateSpelldust;
        this.spelldustItemSupplier = spelldustItemSupplier;
        this.vanillaGemstoneSupplier = vanillaGemstoneSupplier;
    }

    @Nullable
    public Element getElement()
    {
        return element;
    }

    @Override
    public String getName()
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

    public static final ImmutableList<Gemstone> values = ImmutableList.copyOf(values());

    public boolean generateCustomBlock()
    {
        return !isVanilla && blockSupplier != null;
    }

    public boolean generateCustomOre()
    {
        return !isVanilla && oreSupplier != null;
    }

    public boolean generateSpelldust()
    {
        return spelldustItemSupplier != null;
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

    public Block getOre()
    {
        return oreSupplier != null ? oreSupplier.get() : Blocks.AIR;
    }

    public GemstoneItem getItem()
    {
        return itemSupplier.get();
    }

    public Item getSpelldust()
    {
        return spelldustItemSupplier != null ? spelldustItemSupplier.get() : Items.AIR;
    }

    @Nullable
    public static Gemstone byName(String name)
    {
        for (Gemstone g : values())
        {
            if (g.getName().equals(name))
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
}
