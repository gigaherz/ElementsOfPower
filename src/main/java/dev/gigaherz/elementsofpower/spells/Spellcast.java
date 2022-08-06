package dev.gigaherz.elementsofpower.spells;

import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import dev.gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class Spellcast
{
    private final List<Element> sequence;

    private SpellShape shape;
    private SpellEffect effect;

    private int power;
    private int empowering;
    private int radiating;
    private int timing;
    private MagicAmounts spellCost;

    @Nullable
    private Entity projectile;

    private Random rand;

    public Spellcast(SpellShape shape, SpellEffect effect, int power, List<Element> sequence)
    {
        this.shape = shape;
        this.effect = effect;
        this.power = power;
        this.sequence = sequence;
    }

    protected Spellcast(List<Element> sequence, SpellShape shape, SpellEffect effect, Entity projectile, int power, Random rand, int empowering, int radiating, MagicAmounts spellCost)
    {
        this.sequence = sequence;
        this.shape = shape;
        this.effect = effect;
        this.projectile = projectile;
        this.power = power;
        this.rand = rand;
        this.empowering = empowering;
        this.radiating = radiating;
        this.spellCost = spellCost;
    }

    public List<Element> getSequence()
    {
        return sequence;
    }

    public void setProjectile(Entity entity)
    {
        projectile = entity;
    }

    public InitializedSpellcast init(Level world, Player player)
    {
        return new InitializedSpellcast(sequence, shape, effect, projectile, power, rand, empowering, radiating, spellCost, world, player);
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

    public MagicAmounts getSpellCost()
    {
        return spellCost;
    }

    public void setSpellCost(MagicAmounts spellCost)
    {
        this.spellCost = spellCost;
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

    public int getPower()
    {
        return power;
    }

    public Entity getProjectile()
    {
        return projectile;
    }

    protected Random getRandom()
    {
        return rand;
    }

    protected void setRandom(Random rand)
    {
        this.rand = rand;
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