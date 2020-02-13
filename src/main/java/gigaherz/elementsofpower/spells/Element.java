package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.ElementsOfPowerBlocks;
import gigaherz.elementsofpower.ElementsOfPowerItems;
import gigaherz.elementsofpower.items.MagicOrbItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.function.Supplier;

public enum Element
{
    Fire("fire", 1, Shape.Sphere, () -> ElementsOfPowerBlocks.FIRE_COCOON, () -> ElementsOfPowerItems.FIRE_COCOON, () -> ElementsOfPowerItems.FIRE_ORB),
    Water("water", 0, Shape.Ball, () -> ElementsOfPowerBlocks.WATER_COCOON, () -> ElementsOfPowerItems.WATER_COCOON, () -> ElementsOfPowerItems.WATER_ORB),
    Air("air", 3, Shape.Cone, () -> ElementsOfPowerBlocks.AIR_COCOON, () -> ElementsOfPowerItems.AIR_COCOON, () -> ElementsOfPowerItems.AIR_ORB),
    Earth("earth", 2, Shape.Ball, () -> ElementsOfPowerBlocks.EARTH_COCOON, () -> ElementsOfPowerItems.EARTH_COCOON, () -> ElementsOfPowerItems.EARTH_ORB),
    Light("light", 5, Shape.Beam, () -> ElementsOfPowerBlocks.LIGHT_COCOON, () -> ElementsOfPowerItems.LIGHT_COCOON, () -> ElementsOfPowerItems.LIGHT_ORB),
    Darkness("darkness", 4, Shape.Beam, () -> ElementsOfPowerBlocks.DARKNESS_COCOON, () -> ElementsOfPowerItems.DARKNESS_COCOON, () -> ElementsOfPowerItems.DARKNESS_ORB),
    Life("life", 7, Shape.Self, () -> ElementsOfPowerBlocks.LIFE_COCOON, () -> ElementsOfPowerItems.LIFE_COCOON, () -> ElementsOfPowerItems.LIFE_ORB),
    Death("death", 6, Shape.Single, () -> ElementsOfPowerBlocks.DEATH_COCOON, () -> ElementsOfPowerItems.DEATH_COCOON, () -> ElementsOfPowerItems.DEATH_ORB);

    private final int opposite;
    private final Shape shape;
    private final String name;
    private final Supplier<Block> blockSupplier;
    private final Supplier<Item> itemSupplier;
    private final Supplier<MagicOrbItem> orbSupplier;

    public Element getOpposite()
    {
        return values[opposite];
    }

    public String getName()
    {
        return name;
    }

    Element(String name, int opposite, Shape shape, Supplier<Block> blockSupplier, Supplier<Item> itemSupplier, Supplier<MagicOrbItem> orbSupplier)
    {
        this.opposite = opposite;
        this.shape = shape;
        this.name = name;
        this.blockSupplier = blockSupplier;
        this.itemSupplier = itemSupplier;
        this.orbSupplier = orbSupplier;
    }

    public Shape getShape()
    {
        return shape;
    }

    public static final Element[] values = values();

    public Block getBlock()
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
}
