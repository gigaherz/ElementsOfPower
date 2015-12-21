package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.GuiOverlayMagicContainer;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.spells.ISpellEffect;
import gigaherz.elementsofpower.spells.ISpellcast;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class ItemWand extends ItemMagicContainer
{
    public static final String SPELL_SEQUENCE_TAG = "SpellSequence";

    private static final String[] subNames = {
            "lapisWand", "emeraldWand", "diamondWand", "creativeWand",
            "lapisStaff", "emeraldStaff", "diamondStaff", "creativeStaff"
    };

    private static final EnumRarity rarities[] = {
            EnumRarity.UNCOMMON, EnumRarity.RARE, EnumRarity.EPIC, EnumRarity.COMMON,
            EnumRarity.UNCOMMON, EnumRarity.RARE, EnumRarity.EPIC, EnumRarity.COMMON
    };

    private static final boolean areCreative[] = {
            false, false, false, true,
            false, false, false, true
    };

    public static boolean isCreative(ItemStack stack)
    {
        int dmg = stack.getItemDamage();
        return dmg <= areCreative.length && areCreative[dmg];
    }

    public ItemWand()
    {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setUnlocalizedName(ElementsOfPower.MODID + ".magicWand");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack)
    {
        return rarities[stack.getItemDamage()];
    }

    @Override
    public int getMetadata(int damageValue)
    {
        return damageValue;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length)
        {
            return getUnlocalizedName();
        }

        return getUnlocalizedName() + "." + subNames[sub];
    }

    @Override
    public ItemStack getStack(int count, int damageValue)
    {
        return new ItemStack(this, count, damageValue);
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int meta = 0; meta < subNames.length; meta++)
        {
            subItems.add(new ItemStack(itemIn, 1, meta));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (player.isSneaking())
        {
            stack.getTagCompound().removeTag(SPELL_SEQUENCE_TAG);
        }
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        if (world.isRemote)
        {
            int slot = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
            GuiOverlayMagicContainer.instance.beginHoldingRightButton(slot, stack);
        }
        return stack;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining)
    {
        if (world.isRemote)
        {
            GuiOverlayMagicContainer.instance.endHoldingRightButton(false);
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.BOW;
    }

    public void onSpellCommit(ItemStack stack, EntityPlayer player)
    {
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

        ISpellcast cast = effect.castSpell(stack, player);
        if (cast != null)
        {
            SpellcastEntityData data = SpellcastEntityData.get(player);
            if(data != null)
            {
                data.begin(cast);
            }
        }

        amounts.subtract(cost);

        MagicDatabase.setContainedMagic(stack, amounts);
    }

    public void processSequenceUpdate(SpellSequenceUpdate message, ItemStack stack)
    {

        if (message.changeMode == SpellSequenceUpdate.ChangeMode.COMMIT)
        {

            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null)
            {
                if (!ItemWand.isCreative(stack))
                    return;

                nbt = new NBTTagCompound();
                stack.setTagCompound(nbt);
            }

            if (message.sequence != null)
                nbt.setString(SPELL_SEQUENCE_TAG, message.sequence);

            onSpellCommit(stack, message.entity);
        }
    }
}
