package gigaherz.elementsofpower.gemstones;

import com.google.common.collect.ImmutableList;
import gigaherz.elementsofpower.ElementsOfPowerMod;
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
    Ruby(Element.Fire, "ruby", 0xFFFF0000,
            () -> ElementsOfPowerMod.ruby, false, () -> ElementsOfPowerMod.rubyBlock, () -> ElementsOfPowerMod.rubyOre,
            () -> ElementsOfPowerMod.unknown_ruby, true, () -> ElementsOfPowerMod.ruby_spelldust), // red
    Sapphire(Element.Water, "sapphire", 0xFF0000FF,
            () -> ElementsOfPowerMod.sapphire, false, () -> ElementsOfPowerMod.sapphireBlock, () -> ElementsOfPowerMod.sapphireOre,
            () -> ElementsOfPowerMod.unknown_sapphire, true, () -> ElementsOfPowerMod.sapphire_spelldust), // blue
    Citrine(Element.Air, "citrine", 0xFFFFFF00,
            () -> ElementsOfPowerMod.citrine, false, () -> ElementsOfPowerMod.citrineBlock, () -> ElementsOfPowerMod.citrineOre,
            () -> ElementsOfPowerMod.unknown_citrine, true, () -> ElementsOfPowerMod.citrine_spelldust), // yellow
    Agate(Element.Earth, "agate", 0xFF7F3F00,
            () -> ElementsOfPowerMod.agate, false, () -> ElementsOfPowerMod.agateBlock, () -> ElementsOfPowerMod.agateOre,
            () -> ElementsOfPowerMod.unknown_agate, true, () -> ElementsOfPowerMod.agate_spelldust), // brown
    Quartz(Element.Light, "quartz", 0xFFFFFFFF,
            () -> ElementsOfPowerMod.quartz, true, () -> Blocks.QUARTZ_BLOCK, () -> Blocks.NETHER_QUARTZ_ORE,
            () -> Items.QUARTZ, true, () -> ElementsOfPowerMod.quartz_spelldust), // white
    Serendibite(Element.Darkness, "serendibite", 0xFF0F0F0F,
            () -> ElementsOfPowerMod.serendibite, false, () -> ElementsOfPowerMod.serendibiteBlock, () -> ElementsOfPowerMod.serendibiteOre,
            () -> ElementsOfPowerMod.unknown_serendibite, true, () -> ElementsOfPowerMod.serendibite_spelldust), // black
    Emerald(Element.Life, "emerald", 0xFF00FF00,
            () -> ElementsOfPowerMod.emerald, true, ()->Blocks.EMERALD_BLOCK, ()->Blocks.EMERALD_ORE,
            () -> Items.EMERALD, true, () -> ElementsOfPowerMod.emerald_spelldust), // green
    Amethyst(Element.Death, "amethyst", 0xFFAF00FF,
            () -> ElementsOfPowerMod.amethyst, false, () -> ElementsOfPowerMod.amethystBlock, () -> ElementsOfPowerMod.amethystOre,
            () -> ElementsOfPowerMod.unknown_amethyst, true, () -> ElementsOfPowerMod.amethyst_spelldust), // purple

    Diamond(null, "diamond", 0xFF7FFFCF,
            () -> ElementsOfPowerMod.diamond, true, ()->Blocks.DIAMOND_BLOCK, ()->Blocks.DIAMOND_ORE,
            () -> Items.DIAMOND, true, () -> ElementsOfPowerMod.diamond_spelldust), // clear

    Creativite(null, "creativite", 0xFF000000,
            () -> ElementsOfPowerMod.creativite, false, null, null,
            () -> ElementsOfPowerMod.unknown_creativite, false, null);

    private final Element element;
    private final String name;
    private final int tintColor;
    private final boolean isVanilla;
    private final Supplier<ItemGemstone> itemSupplier;
    @Nullable
    private final Supplier<Block> blockSupplier;
    @Nullable
    private final Supplier<Block> oreSupplier;
    @Nullable
    private final Supplier<Item> unknownItemSupplier;
    @Nullable
    private final Supplier<Item> spelldustItemSupplier;
    private final boolean generateSpelldust;

    Gemstone(@Nullable Element element, String name, int tintColor,
             @Nullable Supplier<ItemGemstone> itemSupplier,
             boolean isVanilla, @Nullable Supplier<Block> blockSupplier, @Nullable Supplier<Block> oreSupplier, @Nullable Supplier<Item> unknownItemSupplier, boolean generateSpelldust, @Nullable Supplier<Item> spelldustItemSupplier)
    {
        this.element = element;
        this.name = name;
        this.tintColor = tintColor;
        this.isVanilla = isVanilla;
        this.blockSupplier = blockSupplier;
        this.oreSupplier = oreSupplier;
        this.itemSupplier = itemSupplier;
        this.unknownItemSupplier = unknownItemSupplier;
        this.generateSpelldust = generateSpelldust;
        this.spelldustItemSupplier = spelldustItemSupplier;
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

    public boolean generateCustomUnexamined()
    {
        return !isVanilla && unknownItemSupplier != null;
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

    public ItemGemstone getItem()
    {
        return itemSupplier.get();
    }

    public Item getUnexamined()
    {
        return unknownItemSupplier != null ? unknownItemSupplier.get() : Items.AIR;
    }

    public Item getSpelldust()
    {
        return spelldustItemSupplier != null ? spelldustItemSupplier.get() : Items.AIR;
    }
}
