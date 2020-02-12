package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.items.ItemMagicOrb;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.function.Supplier;

public enum Element
{
    Fire("fire", 1, Shape.Sphere, () -> ElementsOfPowerMod.fireCocoon, () -> ElementsOfPowerMod.fireCocoonItem, () -> ElementsOfPowerMod.fire_orb),
    Water("water", 0, Shape.Ball, () -> ElementsOfPowerMod.waterCocoon, () -> ElementsOfPowerMod.waterCocoonItem, () -> ElementsOfPowerMod.water_orb),
    Air("air", 3, Shape.Cone, () -> ElementsOfPowerMod.airCocoon, () -> ElementsOfPowerMod.airCocoonItem, () -> ElementsOfPowerMod.air_orb),
    Earth("earth", 2, Shape.Ball, () -> ElementsOfPowerMod.earthCocoon, () -> ElementsOfPowerMod.earthCocoonItem, () -> ElementsOfPowerMod.earth_orb),
    Light("light", 5, Shape.Beam, () -> ElementsOfPowerMod.lightCocoon, () -> ElementsOfPowerMod.lightCocoonItem, () -> ElementsOfPowerMod.light_orb),
    Darkness("darkness", 4, Shape.Beam, () -> ElementsOfPowerMod.darknessCocoon, () -> ElementsOfPowerMod.darknessCocoonItem, () -> ElementsOfPowerMod.darkness_orb),
    Life("life", 7, Shape.Self, () -> ElementsOfPowerMod.lifeCocoon, () -> ElementsOfPowerMod.lifeCocoonItem, () -> ElementsOfPowerMod.life_orb),
    Death("death", 6, Shape.Single, () -> ElementsOfPowerMod.deathCocoon, () -> ElementsOfPowerMod.deathCocoonItem, () -> ElementsOfPowerMod.death_orb);

    private final int opposite;
    private final Shape shape;
    private final String name;
    private final Supplier<Block> blockSupplier;
    private final Supplier<Item> itemSupplier;
    private final Supplier<ItemMagicOrb> orbSupplier;

    public Element getOpposite()
    {
        return values[opposite];
    }

    public String getName()
    {
        return name;
    }

    Element(String name, int opposite, Shape shape, Supplier<Block> blockSupplier, Supplier<Item> itemSupplier, Supplier<ItemMagicOrb> orbSupplier)
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

    public ItemMagicOrb getOrb()
    {
        return orbSupplier.get();
    }
}
