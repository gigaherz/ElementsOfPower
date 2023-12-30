package dev.gigaherz.elementsofpower.spells;

import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import dev.gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record Spellcast(
    List<Element> sequence,
    SpellShape shape,
    SpellEffect effect,
    int power,
    int empowering,
    int radiating,
    int timing)
{
    public static Spellcast read(CompoundTag compound)
    {
        var sequence = readSequenceNBT(compound.getList("sequence", Tag.TAG_STRING));

        SpellShape shape = SpellShapes.getShape(compound.getString("shape"));
        SpellEffect effect = SpellEffects.getEffect(compound.getString("effect"));
        int power = compound.getInt("power");
        int empowering = compound.getInt("empowering");
        int radiating = compound.getInt("radiating");
        int timing = compound.getInt("timing");
        return new Spellcast(sequence, shape, effect, power, empowering, radiating, timing);
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        compound.put("sequence", getSequenceNBT());
        compound.putString("shape", SpellShapes.getName(shape));
        compound.putString("effect", SpellEffects.getName(effect));
        compound.putInt("power", power);
        compound.putInt("empowering", empowering);
        compound.putInt("radiating", radiating);
        return compound;
    }

    public static List<Element> readSequenceNBT(ListTag sequenceTag)
    {
        var list = new ArrayList<Element>();
        for (int i = 0; i < sequenceTag.size(); i++)
        {
            String element = sequenceTag.getString(i);
            list.add(Element.byName(element));
        }
        return list;
    }

    public ListTag getSequenceNBT()
    {
        ListTag list = new ListTag();
        for (Element e : sequence)
        {
            list.add(StringTag.valueOf(e.getName()));
        }
        return list;
    }
}