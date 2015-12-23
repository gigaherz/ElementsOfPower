package gigaherz.elementsofpower.spells.cast;

import gigaherz.elementsofpower.spells.SpellBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class Spellcast<T extends SpellBase> implements ISpellcast<T>
{
    protected World world;
    protected EntityPlayer player;
    protected T spell;

    public Spellcast(T parent)
    {
        spell = parent;
    }


    @Override
    public float getRemainingCastTime()
    {
        return 0;
    }

    @Override
    public void init(World world, EntityPlayer player)
    {
        this.world = world;
        this.player = player;
    }

    @Override
    public T getEffect() { return spell; }

    public int getDamageForce() { return spell.getPower(); }

    @Override
    public void update()
    {
    }

    @Override
    public void readFromNBT(NBTTagCompound tagData)
    {
    }

    @Override
    public void writeToNBT(NBTTagCompound tagData)
    {
    }
}