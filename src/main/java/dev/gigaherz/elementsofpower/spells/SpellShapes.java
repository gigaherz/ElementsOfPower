package dev.gigaherz.elementsofpower.spells;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.gigaherz.elementsofpower.spells.shapes.*;

public class SpellShapes
{
    private static final BiMap<String, SpellShape> shapeRegistry = HashBiMap.create();

    public static SpellShape register(String name, SpellShape shape)
    {
        shapeRegistry.put(name, shape);
        return shape;
    }

    public static SpellShape getShape(String name)
    {
        return shapeRegistry.get(name);
    }

    public static String getName(SpellShape shape)
    {
        return shapeRegistry.inverse().get(shape);
    }

    public static final SpellShape SPHERE = register("sphere", new SphereShape());
    public static final SpellShape BALL = register("ball", new BallShape());
    public static final SpellShape BEAM = register("beam", new LaserShape());
    public static final SpellShape LASER = register("laser", new LaserShape());
    public static final SpellShape CONE = register("cone", new ConeShape());
    public static final SpellShape SELF = register("self", new SelfShape());
    public static final SpellShape WALL = register("wall", new WallShape());
    public static final SpellShape SINGLE = register("single", new SingleShape());

}
