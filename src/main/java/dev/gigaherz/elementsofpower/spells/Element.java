package dev.gigaherz.elementsofpower.spells;

import dev.gigaherz.elementsofpower.ElementsOfPowerBlocks;
import dev.gigaherz.elementsofpower.ElementsOfPowerItems;
import dev.gigaherz.elementsofpower.cocoons.CocoonBlock;
import dev.gigaherz.elementsofpower.items.MagicOrbItem;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum Element
{
    FIRE("fire", 1, Shape.SPHERE, () -> ElementsOfPowerBlocks.FIRE_COCOON, () -> ElementsOfPowerItems.FIRE_COCOON, () -> ElementsOfPowerItems.FIRE_ORB, 0xFFff3e00),
    WATER("water", 0, Shape.BALL, () -> ElementsOfPowerBlocks.WATER_COCOON, () -> ElementsOfPowerItems.WATER_COCOON, () -> ElementsOfPowerItems.WATER_ORB, 0xFF005dff),
    AIR("air", 3, Shape.CONE, () -> ElementsOfPowerBlocks.AIR_COCOON, () -> ElementsOfPowerItems.AIR_COCOON, () -> ElementsOfPowerItems.AIR_ORB, 0xFFffed96),
    EARTH("earth", 2, Shape.BALL, () -> ElementsOfPowerBlocks.EARTH_COCOON, () -> ElementsOfPowerItems.EARTH_COCOON, () -> ElementsOfPowerItems.EARTH_ORB, 0xFF7f3300),
    LIGHT("light", 5, Shape.BEAM, () -> ElementsOfPowerBlocks.LIGHT_COCOON, () -> ElementsOfPowerItems.LIGHT_COCOON, () -> ElementsOfPowerItems.LIGHT_ORB, 0xFFffffff),
    DARKNESS("darkness", 4, Shape.BEAM, () -> ElementsOfPowerBlocks.DARKNESS_COCOON, () -> ElementsOfPowerItems.DARKNESS_COCOON, () -> ElementsOfPowerItems.DARKNESS_ORB, 0xFF242424),
    LIFE("life", 7, Shape.SELF, () -> ElementsOfPowerBlocks.LIFE_COCOON, () -> ElementsOfPowerItems.LIFE_COCOON, () -> ElementsOfPowerItems.LIFE_ORB, 0xFF1acd7f),
    DEATH("death", 6, Shape.SINGLE, () -> ElementsOfPowerBlocks.DEATH_COCOON, () -> ElementsOfPowerItems.DEATH_COCOON, () -> ElementsOfPowerItems.DEATH_ORB, 0xFF8c1acd);

    private final int opposite;
    private final Shape shape;
    private final String name;
    private final Supplier<CocoonBlock> blockSupplier;
    private final Supplier<Item> itemSupplier;
    private final Supplier<MagicOrbItem> orbSupplier;
    private final int color;

    @Nullable
    public static Element byName(String name)
    {
        for (Element e : values())
        {
            if (e.getName().equals(name))
                return e;
        }
        return null;
    }

    public Element getOpposite()
    {
        return values[opposite];
    }

    public String getName()
    {
        return name;
    }

    Element(String name, int opposite, Shape shape, Supplier<CocoonBlock> blockSupplier, Supplier<Item> itemSupplier, Supplier<MagicOrbItem> orbSupplier, int color)
    {
        this.opposite = opposite;
        this.shape = shape;
        this.name = name;
        this.blockSupplier = blockSupplier;
        this.itemSupplier = itemSupplier;
        this.orbSupplier = orbSupplier;
        this.color = color;
    }

    public Shape getShape()
    {
        return shape;
    }

    public CocoonBlock getCocoon()
    {
        return blockSupplier.get();
    }

    public Item getItem()
    {
        return itemSupplier.get();
    }

    public MagicOrbItem getOrb()
    {
        return orbSupplier.get();
    }

    public int getColor()
    {
        return color;
    }

    public static final Element[] values = values();

    public static Stream<Element> stream()
    {
        return Arrays.stream(values);
    }
}
