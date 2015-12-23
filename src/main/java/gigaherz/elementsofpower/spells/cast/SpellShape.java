package gigaherz.elementsofpower.spells.cast;

public enum SpellShape
{
    Thrown, // Does not need rendering, the entity will take care of it
    Beam, // Renders as a beam coming from the player
    Sphere, // Renders as an expanding sphere around the player
    Cylinder, // Renders as an expanding cylinder (uncapped) around the player

}
