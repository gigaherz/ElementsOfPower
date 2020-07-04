package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Spellcast
{
    public final List<Element> sequence;

    // Rendering data;
    public Vector3d start;
    public Vector3d end;

    protected SpellShape shape;
    protected SpellEffect effect;

    public Entity projectile;

    public int power;

    public Random rand;
    protected int empowering;
    protected int radiating;
    protected MagicAmounts spellCost;

    public Spellcast(SpellShape shape, SpellEffect effect, int power, List<Element> sequence)
    {
        this.shape = shape;
        this.effect = effect;
        this.power = power;
        this.sequence = sequence;
    }

    protected Spellcast(List<Element> sequence, Vector3d start, Vector3d end, SpellShape shape, SpellEffect effect, Entity projectile, int power, Random rand, int empowering, int radiating, MagicAmounts spellCost)
    {
        this.sequence = sequence;
        this.start = start;
        this.end = end;
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

    public InitializedSpellcast init(World world, PlayerEntity player)
    {
        return new InitializedSpellcast(sequence, start, end, shape, effect, projectile, power, rand, empowering, radiating, spellCost, world, player);
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

    public ListNBT getSequenceNBT()
    {
        ListNBT list = new ListNBT();
        for (Element e : sequence)
        {
            list.add(StringNBT.valueOf(e.getName()));
        }
        return list;
    }
}