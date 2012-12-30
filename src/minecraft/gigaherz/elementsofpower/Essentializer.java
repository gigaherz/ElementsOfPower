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
        return ElementsOfPower.worker.blockID;
    }

    @Override
    public int getBlockTextureFromSideAndMetadata(int i, int meta)
    {
        int sideTexture1 = (meta & 8) != 0 ? 17 : 1;
        int sideTexture2 = (meta & 8) != 0 ? 48 : 32;
        int sideTexture3 = 18;
        int meta2 = 0;

        switch (meta & 7)
        {
            case 2:
                meta2 = 3;
                break;

            case 3:
                meta2 = 2;
                break;

            case 5:
                meta2 = 4;
                break;

            case 4:
                meta2 = 5;
                break;
        }

        switch (i)
        {
            case 0: // bottom
                return 3;

            case 1: // top
                return 3;

            default: // sides
                if (meta2 == i)
                {
                    return sideTexture3;
                }

                return (meta & 7) == i ? sideTexture2 : sideTexture1;
        }
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
        int metadata = world.getBlockMetadata(x, y, z);
        boolean wasPowered = (metadata & 8) != 0;

        if (wasPowered)
        {
            metadata &= 7;
            float sx = (float)x + 0.5F;
            float sy = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float sz = (float)z + 0.5F;
            float o1 = 0.52F;
            float o2 = random.nextFloat() * 0.6F - 0.3F;
            world.spawnParticle("reddust", (double)(sx - o1), (double)sy, (double)(sz + o2), 0.0D, 0.0D, 0.0D);
            world.spawnParticle("reddust", (double)(sx + o1), (double)sy, (double)(sz + o2), 0.0D, 0.0D, 0.0D);
            world.spawnParticle("reddust", (double)(sx + o2), (double)sy, (double)(sz - o1), 0.0D, 0.0D, 0.0D);
            world.spawnParticle("reddust", (double)(sx + o2), (double)sy, (double)(sz + o1), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entity)
    {
        int angle = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        switch (angle)
        {
            case 0:
                world.setBlockMetadataWithNotify(x, y, z, 2);
                break;

            case 1:
                world.setBlockMetadataWithNotify(x, y, z, 5);
                break;

            case 2:
                world.setBlockMetadataWithNotify(x, y, z, 3);
                break;

            case 3:
                world.setBlockMetadataWithNotify(x, y, z, 4);
                break;
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
