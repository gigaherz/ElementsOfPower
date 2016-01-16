package gigaherz.elementsofpower.spells.cast;

import gigaherz.elementsofpower.spells.Spell;
import gigaherz.elementsofpower.spells.cast.effects.SpellEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class Spellcast<T extends Spell>
{
    protected SpellEffect effect;

    public World world;
    public EntityPlayer player;
    protected T spell;

    public Entity projectile;

    public Spellcast(T parent, SpellEffect effect)
    {
        spell = parent;
        this.effect = effect;
    }

    public void setProjectile(Entity entity)
    {
        projectile = entity;
    }

    public float getRemainingCastTime()
    {
        return 0;
    }

    public void init(World world, EntityPlayer player)
    {
        this.world = world;
        this.player = player;
    }

    public T getEffect()
    {
        return spell;
    }

    public int getDamageForce()
    {
        return Math.max(0, spell.getPower() - effect.getForceModifier(this));
    }

    public void update()
    {
    }

    public void readFromNBT(NBTTagCompound tagData)
    {
    }

    public void writeToNBT(NBTTagCompound tagData)
    {
    }

    public EntityPlayer getCastingPlayer()
    {
        return player;
    }

    public int getColor()
    {
        return effect.getColor(this);
    }

    public float getRandomForParticle()
    {
        return 0;
    }

    public void spawnRandomParticle(EnumParticleTypes type, double x, double y, double z)
    {
        world.spawnParticle(type, x, y, z, getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
    }
}