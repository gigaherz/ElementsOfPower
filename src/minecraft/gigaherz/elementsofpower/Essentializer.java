package gigaherz.elementsofpower;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Essentializer extends Block
{
    public Essentializer(String name, int id, Material mat, CreativeTabs tab)
    {
        super(id, mat);
        setBlockName(name);
        setCreativeTab(tab);
    }

    @Override
    public String getTextureFile()
    {
        return CommonProxy.BLOCK_PNG;
    }

    @Override
    public int idDropped(int par1, Random par2Random, int par3)
    {
        return ElementsOfPower.essentializer.blockID;
    }

    @Override
    public int getBlockTextureFromSide(int i)
    {
        int topBottom = 3;
        int sideTexture = 19;

        switch (i)
        {
            case 0: // bottom
            case 1: // top
                return topBottom;

            default: // sides
                return sideTexture;
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity == null || player.isSneaking())
        {
            return false;
        }

        player.openGui(ElementsOfPower.instance, 0, world, x, y, z);
        return true;
    }

    public static void updateBlockState(boolean powered, World world, int x, int y, int z)
    {
        int metadata = world.getBlockMetadata(x, y, z);

        if (powered)
        {
            metadata |= 8;
        }
        else
        {
            metadata &= 7;
        }

        world.setBlockMetadataWithNotify(x, y, z, metadata);
    }

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        return new EssentializerTile();
    }
}
