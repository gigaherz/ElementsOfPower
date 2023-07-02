package dev.gigaherz.elementsofpower.spells;

import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import dev.gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Spellcast
{
    private List<Element> sequence;

    protected SpellShape shape;
    protected SpellEffect effect;

    protected int power;
    protected int empowering;
    protected int radiating;
    protected int timing;

    public Spellcast(SpellShape shape, SpellEffect effect, int power, List<Element> sequence)
    {
        this.shape = shape;
        this.effect = effect;
        this.power = power;
        this.sequence = sequence;
    }

    protected Spellcast(List<Element> sequence, SpellShape shape, SpellEffect effect, int power, int empowering, int radiating)
    {
        this.sequence = sequence;
        this.shape = shape;
        this.effect = effect;
        this.power = power;
        this.empowering = empowering;
        this.radiating = radiating;
    }

    public void write(CompoundTag compound)
    {
        compound.put("sequence", getSequenceNBT());
        compound.putString("shape", SpellShapes.getName(shape));
        compound.putString("effect", SpellEffects.getName(effect));
        compound.putInt("power", power);
        compound.putInt("empowering", empowering);
        compound.putInt("radiating", radiating);
    }

    public static Spellcast read(CompoundTag compound)
    {
        var sequence = readSequenceNBT(compound.getList("sequence", Tag.TAG_STRING));

        SpellShape shape = SpellShapes.getShape(compound.getString("shape"));
        SpellEffect effect = SpellEffects.getEffect(compound.getString("effect"));
        int power = compound.getInt("power");
        int empowering = compound.getInt("empowering");
        int radiating = compound.getInt("radiating");
        return new Spellcast(sequence, shape, effect, power, empowering, radiating);
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

    public InitializedSpellcast init(Level level, Player player)
    {
        return new InitializedSpellcast(sequence, shape, effect, power, empowering, radiating, level, player);
    }

    public List<Element> getSequence()
    {
        return sequence;
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

    public SpellShape getShape()
    {
        return shape;
    }

    public SpellEffect getEffect()
    {
        return effect;
    }

    public void setEmpowering(int empowering)
    {
        this.empowering = empowering;
    }

    public int getEmpowering()
    {
        return empowering;
    }

    public void setRadiating(int radiating)
    {
        this.radiating = radiating;
    }

    public int getRadiating()
    {
        return radiating;
    }

    public int getPower()
    {
        return power;
    }

    public int getTiming()
    {
        return timing;
    }

    public void setTiming(int timing)
    {
        this.timing = timing;
    }
}