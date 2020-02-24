package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.ElementsOfPowerBlocks;
import gigaherz.elementsofpower.ElementsOfPowerItems;
import gigaherz.elementsofpower.cocoons.CocoonBlock;
import gigaherz.elementsofpower.items.MagicOrbItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum Element
{
    FIRE("fire", 1, Shape.SPHERE, () -> ElementsOfPowerBlocks.FIRE_COCOON, () -> ElementsOfPowerItems.FIRE_COCOON, () -> ElementsOfPowerItems.FIRE_ORB),
    WATER("water", 0, Shape.BALL, () -> ElementsOfPowerBlocks.WATER_COCOON, () -> ElementsOfPowerItems.WATER_COCOON, () -> ElementsOfPowerItems.WATER_ORB),
    AIR("air", 3, Shape.CONE, () -> ElementsOfPowerBlocks.AIR_COCOON, () -> ElementsOfPowerItems.AIR_COCOON, () -> ElementsOfPowerItems.AIR_ORB),
    EARTH("earth", 2, Shape.BALL, () -> ElementsOfPowerBlocks.EARTH_COCOON, () -> ElementsOfPowerItems.EARTH_COCOON, () -> ElementsOfPowerItems.EARTH_ORB),
    LIGHT("light", 5, Shape.BEAM, () -> ElementsOfPowerBlocks.LIGHT_COCOON, () -> ElementsOfPowerItems.LIGHT_COCOON, () -> ElementsOfPowerItems.LIGHT_ORB),
    DARKNESS("darkness", 4, Shape.BEAM, () -> ElementsOfPowerBlocks.DARKNESS_COCOON, () -> ElementsOfPowerItems.DARKNESS_COCOON, () -> ElementsOfPowerItems.DARKNESS_ORB),
    LIFE("life", 7, Shape.SELF, () -> ElementsOfPowerBlocks.LIFE_COCOON, () -> ElementsOfPowerItems.LIFE_COCOON, () -> ElementsOfPowerItems.LIFE_ORB),
    DEATH("death", 6, Shape.SINGLE, () -> ElementsOfPowerBlocks.DEATH_COCOON, () -> ElementsOfPowerItems.DEATH_COCOON, () -> ElementsOfPowerItems.DEATH_ORB);

    private final int opposite;
    private final Shape shape;
    private final String name;
    private final Supplier<CocoonBlock> blockSupplier;
    private final Supplier<Item> itemSupplier;
    private final Supplier<MagicOrbItem> orbSupplier;

    @Nullable
    public static Element byName(String name)
    {
        for (Element e : values())
            if (e.getName().equals(name))
                return e;
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

    Element(String name, int opposite, Shape shape, Supplier<CocoonBlock> blockSupplier, Supplier<Item> itemSupplier, Supplier<MagicOrbItem> orbSupplier)
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
}
