package dev.gigaherz.purrticles.features;

import dev.gigaherz.purrticles.values.VaryingNumber;

/**
 * Defines the position of the particle as a 3-component vector
 */
public class PositionModule extends Vector3Module
{
    public PositionModule(VaryingNumber x, VaryingNumber y, VaryingNumber z)
    {
        super("position", x, y, z);
    }
}
