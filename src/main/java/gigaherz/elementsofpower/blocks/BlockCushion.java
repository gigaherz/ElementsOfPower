package gigaherz.elementsofpower.blocks;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockCushion extends BlockDust
{
    public BlockCushion()
    {
        super(ElementsOfPower.materialCushion);
        setUnlocalizedName(ElementsOfPower.MODID + ".cushion");
        setCreativeTab(CreativeTabs.tabMisc);
        setHardness(0.1F);
        setBlockUnbreakable();
        setStepSound(Block.soundTypeCloth);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(DENSITY, 16));
    }

    @Override
    public boolean isVisuallyOpaque()
    {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockAccess world, BlockPos pos)
    {
        return 0;
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        double maxV = 0.1;
        double maxVSq = maxV*maxV;
        double factor = 0.2;

        double velocitySq = entityIn.motionX * entityIn.motionX + entityIn.motionY * entityIn.motionY + entityIn.motionZ * entityIn.motionZ;
        if(velocitySq > maxVSq)
        {
            double velocity = Math.sqrt(velocitySq);
            double newVel = velocity + factor * (maxV - velocity);

            entityIn.motionX = entityIn.motionX * newVel / velocity;
            entityIn.motionY = entityIn.motionY * newVel / velocity;
            entityIn.motionZ = entityIn.motionZ * newVel / velocity;

            entityIn.fallDistance = (float) (entityIn.fallDistance * newVel / velocity);
        }
    }
}
