package gigaherz.elementsofpower;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemMagicOrb extends Item {
    private final static String[] subNames =
            {
                    "fire", "water",
                    "air", "earth",
                    "light", "darkness",
                    "life", "death",
            };

    public ItemMagicOrb() {
        // Constructor Configuration
        setMaxStackSize(1000);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damageValue) {
        return damageValue;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length) {
            sub = 0;
        }

        return getUnlocalizedName() + "." + subNames[sub];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
        for (int meta = 1; meta < subNames.length; meta++) {
            subItems.add(new ItemStack(itemIn, 1, meta));
        }
    }

    // CUSTOM STUFF
    public ItemStack getStack(int count, int damageValue) {
        ItemStack stack = new ItemStack(this, count);
        stack.setItemDamage(damageValue);
        return stack;
    }

    // TODO: OLD STUFF THAT NEEDS REPLACING
    @SideOnly(Side.CLIENT)
    public int getIconFromDamage(int par1) {
        if (par1 < 8) {
            return par1;
        }

        return 0; //this.iconIndex;
    }
}
