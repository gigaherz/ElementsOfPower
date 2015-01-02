package gigaherz.elementsofpower;

import gigaherz.elementsofpower.client.GuiEssentializer;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Random;

public class BlockEssentializer
        extends Block
        implements ITileEntityProvider {
    public BlockEssentializer() {
        super(Material.iron);
        setUnlocalizedName("essentializer");
        setCreativeTab(ElementsOfPower.tabMagic);
        setHardness(15.0F);
        setStepSound(Block.soundTypeMetal);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity == null || playerIn.isSneaking()) {
            return false;
        }

        playerIn.openGui(ElementsOfPower.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEssentializer();
    }

    // TODO: OLD STUFF THAT NEEDS REPLACING
    public static void updateBlockState(boolean powered, World world, int x, int y, int z) {
        int metadata = 0; // world.getBlockMetadata(x, y, z);

        if (powered) {
            metadata |= 8;
        } else {
            metadata &= 7;
        }

        //world.setBlockMetadataWithNotify(x, y, z, metadata);
    }
}
