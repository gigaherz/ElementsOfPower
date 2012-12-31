package gigaherz.elementsofpower;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MagicOrb extends Item
{
    private final static String[] subNames =
    {
        "fire", "water",
        "air", "earth",
        "light", "darkness",
        "life", "death",
    };

    public MagicOrb(int id)
    {
        super(id);
        // Constructor Configuration
        setMaxStackSize(1000);
        setIconIndex(0);
        setItemName("magicOrb");
        setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    public int getIconFromDamage(int par1)
    {
        if (par1 < 8)
        {
            return par1;
        }

        return this.iconIndex;
    }

    @Override
    public int getMetadata(int damageValue)
    {
        return damageValue;
    }

    public String getTextureFile()
    {
        return CommonProxy.ITEMS_PNG;
    }

    @Override
    public String getItemNameIS(ItemStack stack)
    {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length)
        {
            sub = 0;
        }

        return getItemName() + "." + subNames[sub];
    }

    public ItemStack getStack(int count, int damageValue)
    {
        ItemStack stack = new ItemStack(this, count);
        stack.setItemDamage(damageValue);
        return stack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int unknown, CreativeTabs tab, List subItems)
    {
        for (int meta = 1; meta < subNames.length; meta++)
        {
            subItems.add(new ItemStack(this, 1, meta));
        }
    }
}
