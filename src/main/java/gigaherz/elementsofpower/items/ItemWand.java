package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.MagicAmounts;
import gigaherz.elementsofpower.MagicDatabase;
import gigaherz.elementsofpower.SpellManager;
import gigaherz.elementsofpower.client.GuiOverlayMagicContainer;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.spells.ISpellEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemWand extends ItemMagicContainer {
    public static final String SPELL_SEQUENCE_TAG = "SpellSequence";

    private static final String[] subNames = {
            "lapisWand", "emeraldWand", "diamondWand", "creativeWand",
            "lapisStaff", "emeraldStaff", "diamondStaff", "creativeStaff"
    };

    private static final EnumRarity rarities[] = {
            EnumRarity.UNCOMMON, EnumRarity.RARE, EnumRarity.EPIC, EnumRarity.COMMON,
            EnumRarity.UNCOMMON, EnumRarity.RARE, EnumRarity.EPIC, EnumRarity.COMMON
    };

    //private final static Hashtable<ItemStack, byte[]> spellTemp = new Hashtable<ItemStack, byte[]>();

    public ItemWand() {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setUnlocalizedName("magicWand");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return rarities[stack.getItemDamage()];
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

    public ItemStack getStack(int count, int damageValue) {
        return new ItemStack(this, count, damageValue);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
        for (int meta = 0; meta < subNames.length; meta++) {
            subItems.add(new ItemStack(itemIn, 1, meta));
        }
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        if (world.isRemote) {
            int slot = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
            GuiOverlayMagicContainer.instance.beginHoldingRightButton(slot, stack);
        }
        return stack;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining) {
        if (!world.isRemote) {
            //onMagicItemReleased(stack, world, player, remaining);
        } else {
            GuiOverlayMagicContainer.instance.endHoldingRightButton(false);
        }
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.BOW;
    }

    public void onMagicItemReleased(ItemStack stack, EntityPlayer player) {

        String sequence = stack.getTagCompound().getString(SPELL_SEQUENCE_TAG);

        if (sequence.length() == 0)
            return;

        ISpellEffect effect = SpellManager.spellRegistration.get(sequence);

        if (effect == null)
            return;

        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);
        MagicAmounts cost = effect.getSpellCost();

        if (!amounts.hasEnough(cost))
            return;

        effect.castSpell(stack, player);

        amounts.subtract(cost);

        MagicDatabase.setContainedMagic(stack, amounts);
    }

    public void processSequenceUpdate(SpellSequenceUpdate message, ItemStack stack) {

        if (message.changeMode == SpellSequenceUpdate.ChangeMode.COMMIT) {

            NBTTagCompound nbt = stack.getTagCompound();
            if(nbt == null)
            {
                if (!((ItemWand) stack.getItem()).isCreative(stack))
                    return;

                nbt = new NBTTagCompound();
                stack.setTagCompound(nbt);
            }

            if (message.sequence != null)
                nbt.setString(SPELL_SEQUENCE_TAG, message.sequence);

            onMagicItemReleased(stack, message.entity);
        }
    }

    private boolean isCreative(ItemStack stack) {
        return stack.getItemDamage() % 4 == 3;
    }
}
