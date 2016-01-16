package gigaherz.elementsofpower.spells.cast.shapes;

import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import gigaherz.elementsofpower.spells.SpellBeam;
import gigaherz.elementsofpower.spells.cast.Spellcast;
import gigaherz.elementsofpower.spells.cast.effects.SpellEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SpellcastBeam extends Spellcast<SpellBeam>
{
    protected int remainingCastTime;
    protected int remainingInterval;

    public SpellcastBeam(SpellBeam parent, SpellEffect effect)
    {
        super(parent, effect);
    }

    @Override
    public float getRemainingCastTime()
    {
        return remainingCastTime;
    }

    @Override
    public void init(World world, EntityPlayer player)
    {
        super.init(world, player);
        remainingCastTime = effect.getBeamDuration(this);
        remainingInterval = effect.getBeamInterval(this);
    }

    @Override
    public SpellBeam getEffect()
    {
        return spell;
    }

    public void onImpact(MovingObjectPosition mop)
    {
        BlockPos bp = mop.getBlockPos();

        if (bp != null)
        {
            bp = bp.offset(mop.sideHit);
        }
        else
        {
            bp = new BlockPos(mop.hitVec);
        }

        int px = bp.getX();
        int py = bp.getY();
        int pz = bp.getZ();
        for (int z = pz - 1; z <= pz + 1; z++)
        {
            for (int x = px - 1; x <= px + 1; x++)
            {
                for (int y = py - 1; y <= py + 1; y++)
                {
                    float dx = Math.abs(px - x);
                    float dy = Math.abs(py - y);
                    float dz = Math.abs(pz - z);
                    float r2 = (dx * dx + dy * dy + dz * dz);
                    boolean in_sphere = r2 <= 1;
                    if (!in_sphere)
                        continue;

                    float r = (float) Math.sqrt(r2);

                    int layers = (int) Math.min(1 - r, 7);

                    BlockPos np = new BlockPos(x, y, z);

                    IBlockState currentState = world.getBlockState(np);

                    effect.processBlockWithinRadius(this, np, currentState, layers);
                }
            }
        }
    }

    @Override
    public void update()
    {
        remainingCastTime--;
        remainingInterval--;

        if (remainingInterval <= 0)
        {
            remainingInterval = effect.getBeamInterval(this);

            if (!world.isRemote)
            {
                float maxDistance = 10;
                Vec3 start = new Vec3(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ);
                Vec3 look = player.getLook(1);
                Vec3 end = start.addVector(look.xCoord * maxDistance, look.yCoord * maxDistance, look.zCoord * maxDistance);
                MovingObjectPosition mop = player.worldObj.rayTraceBlocks(start, end, false, true, false);

                // TODO: Detect entities

                onImpact(mop);
                /*if(mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                {
                    BlockPos pos = mop.getBlockPos();
                    pos = pos.offset(mop.sideHit);
                    IBlockState state = world.getBlockState(pos);
                    effect.processBlockWithinRadius(this, pos, state, 0);
                }*/
            }

            // TODO: visual effects?
        }

        if (remainingCastTime <= 0)
        {
            SpellcastEntityData data = SpellcastEntityData.get(player);
            if (data != null)
            {
                data.end();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagData)
    {
        remainingCastTime = tagData.getInteger("remainingCastTime");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagData)
    {
        tagData.setInteger("remainingCastTime", remainingCastTime);
    }
}