package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.blocks.BlockDust;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSnow;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;

public class EntityEarthball extends EntityBallBase {

    public EntityEarthball(World worldIn)
    {
        super(worldIn);
    }
    public EntityEarthball(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
    }
    public EntityEarthball(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }
    public EntityEarthball(World worldIn, int force, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, force, p_i1774_2_);
    }

    @Override
    protected void spawnBallParticles()
    {
        for (int i = 0; i < 8; ++i)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY, this.posZ,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }
    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.air) {
            worldObj.setBlockState(blockPos, ElementsOfPower.dust.getDefaultState().withProperty(BlockDust.DENSITY, 16));
        }
    }
}
