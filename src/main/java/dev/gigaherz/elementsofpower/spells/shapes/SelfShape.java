package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.SpellcastState;

public class SelfShape extends SpellShape
{
    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public void spellTick(SpellcastState cast)
    {
        cast.effect().processDirectHit(cast, cast.player(), cast.player().position(), cast.player());
    }
}
