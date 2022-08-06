package dev.gigaherz.elementsofpower.spells;

import dev.gigaherz.elementsofpower.spells.shapes.*;

public class SpellShapes
{
    public static final SpellShape SPHERE = new SphereShape();
    public static final SpellShape BALL = new BallShape();
    public static final SpellShape BEAM = new LaserShape(); // FIXME: new BeamShape();
    public static final SpellShape CONE = new ConeShape();
    public static final SpellShape SELF = new SelfShape();
    public static final SpellShape PILLAR = new PillarShape();
    //public static final SpellShape WALL = new WallShape();
    //public static final SpellShape SHIELD = new ShieldShape();
    public static final SpellShape SINGLE = new SingleShape();

}
