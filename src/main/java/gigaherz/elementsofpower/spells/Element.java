package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum Element
{
    Fire("fire", 1, Shape.Sphere, ()-> ElementsOfPowerMod.fireCocoon, ()-> ElementsOfPowerMod.fireCocoonItem),
    Water("water", 0, Shape.Ball, ()-> ElementsOfPowerMod.waterCocoon, ()-> ElementsOfPowerMod.waterCocoonItem),
    Air("air", 3, Shape.Cone, ()-> ElementsOfPowerMod.airCocoon, ()-> ElementsOfPowerMod.airCocoonItem),
    Earth("earth", 2, Shape.Ball, ()-> ElementsOfPowerMod.earthCocoon, ()-> ElementsOfPowerMod.earthCocoonItem),
    Light("light", 5, Shape.Beam, ()-> ElementsOfPowerMod.lightCocoon, ()-> ElementsOfPowerMod.lightCocoonItem),
    Darkness("darkness", 4, Shape.Beam, ()-> ElementsOfPowerMod.darknessCocoon, ()-> ElementsOfPowerMod.darknessCocoonItem),
    Life("life", 7, Shape.Self, ()-> ElementsOfPowerMod.lifeCocoon, ()-> ElementsOfPowerMod.lifeCocoonItem),
    Death("death", 6, Shape.Single, ()-> ElementsOfPowerMod.deathCocoon, ()-> ElementsOfPowerMod.deathCocoonItem);

    private final int opposite;
    private final Shape shape;
    private final String name;
    @Nullable
    private final Supplier<Block> blockSupplier;
    @Nullable
    private final Supplier<Item> itemSupplier;

    public Element getOpposite()
    {
        return values[opposite];
    }

    public String getName()
    {
        return name;
    }

    Element(String name, int opposite, Shape shape, @Nullable Supplier<Block> blockSupplier, @Nullable Supplier<Item> itemSupplier)
    {
        this.opposite = opposite;
        this.shape = shape;
        this.name = name;
        this.blockSupplier = blockSupplier;
        this.itemSupplier = itemSupplier;
    }

    public Shape getShape()
    {
        return shape;
    }

    public static final Element[] values = values();

    public Block getBlock()
    {
        return blockSupplier != null ? blockSupplier.get() : Blocks.AIR;
    }

    public Item getItem()
    {
        return itemSupplier != null ? itemSupplier.get() : Items.AIR;
    }
}
