package gigaherz.elementsofpower.essentializer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.ParticleSmallCloud;
import gigaherz.elementsofpower.common.BlockRegistered;
import gigaherz.elementsofpower.common.GuiHandler;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockEssentializer
        extends BlockRegistered
{

    public BlockEssentializer(String name)
    {
        super(name, Material.IRON);
        setCreativeTab(ElementsOfPower.tabMagic);
        setHardness(15.0F);
        setSoundType(SoundType.METAL);
        setLightLevel(1);
        setLightOpacity(0);
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity == null || playerIn.isSneaking())
            return false;

        playerIn.openGui(ElementsOfPower.instance, GuiHandler.GUI_ESSENTIALIZER, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEssentializer();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEssentializer)
        {
            TileEssentializer essentializer = (TileEssentializer) te;
            if (essentializer.remainingToConvert != null)
            {
                double x = (double) pos.getX() + 0.5;
                double y = (double) pos.getY() + (8.5 / 16.0);
                double z = (double) pos.getZ() + 0.5;
                double rx = rand.nextDouble() * 0.2D - 0.1D;
                double rz = rand.nextDouble() * 0.2D - 0.1D;

                switch (rand.nextInt(4))
                {
                    case 0:
                        ParticleSmallCloud.spawn(worldIn, x + rx + 0.4, y, z + rz, 0.0D, 0.05D, 0.0D);
                        break;
                    case 1:
                        ParticleSmallCloud.spawn(worldIn, x + rx - 0.4, y, z + rz, 0.0D, 0.05D, 0.0D);
                        break;
                    case 2:
                        ParticleSmallCloud.spawn(worldIn, x + rx, y, z + rz + 0.4, 0.0D, 0.05D, 0.0D);
                        break;
                    case 3:
                        ParticleSmallCloud.spawn(worldIn, x + rx, y, z + rz - 0.4, 0.0D, 0.05D, 0.0D);
                        break;
                }
            }
        }
    }
}
