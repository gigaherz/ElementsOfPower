package gigaherz.elementsofpower.spelldust;

import gigaherz.common.state.IItemState;
import gigaherz.common.state.IItemStateManager;
import gigaherz.common.state.ItemStateful;
import gigaherz.common.state.implementation.ItemStateManager;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemSpelldust extends ItemStateful
{
    public static final PropertyEnum<Gemstone> GEM = PropertyEnum.create("gem", Gemstone.class, Gemstone.values.stream().filter(m -> m != Gemstone.Creativite).toArray(Gemstone[]::new));

    public ItemSpelldust(String name)
    {
        super(name);
        setCreativeTab(ElementsOfPower.tabMagic);
        setHasSubtypes(true);
    }

    @Override
    public IItemStateManager createStateManager()
    {
        return new ItemStateManager(this, GEM);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (this.isInCreativeTab(tab))
        {
            for (Gemstone type : GEM.getAllowedValues())
            {
                IItemState state = getDefaultState().withProperty(GEM, type);
                subItems.add(state.getStack());
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        int sub = stack.getItemDamage();

        if (sub >= Gemstone.values.size())
            return getTranslationKey();

        return getTranslationKey() + Gemstone.values.get(sub).getUnlocalizedName();
    }
}
