package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.progression.DiscoveryHandler;
import gigaherz.elementsofpower.spells.Spellcast;
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
            ".lapisWand", ".emeraldWand", ".diamondWand", ".creativeWand",
            ".lapisStaff", ".emeraldStaff", ".diamondStaff", ".creativeStaff"
    };

    private static final EnumRarity rarities[] = {
            EnumRarity.UNCOMMON, EnumRarity.RARE, EnumRarity.EPIC, EnumRarity.COMMON,
            EnumRarity.UNCOMMON, EnumRarity.RARE, EnumRarity.EPIC, EnumRarity.COMMON
    };

    private static final boolean areCreative[] = {
            false, false, false, true,
            false, false, false, true
    };

    @Override
    public boolean isInfinite(ItemStack stack)
    {
        int dmg = stack.getItemDamage();
        return dmg <= areCreative.length && areCreative[dmg];
    }

    public boolean isStaff(ItemStack stack)
    {
        return stack.getMetadata() >= 4;
    }

    public ItemWand()
    {
        super();
        setHasSubtypes(true);
        setUnlocalizedName(ElementsOfPower.MODID + ".magicWand");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || oldStack == null || newStack == null
                || oldStack.getItem() != newStack.getItem() || oldStack.getMetadata() != newStack.getMetadata();
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

        return "item." + ElementsOfPower.MODID + subNames[sub];
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
        if (!world.isRemote)
        {
            if (player.isSneaking())
            {
                stack.getTagCompound().removeTag(SPELL_SEQUENCE_TAG);
            }
        }

        // itemInUse handled by TickEventWandControl

        return stack;
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

    public boolean onSpellCommit(ItemStack stack, EntityPlayer player, String sequence)
    {
        boolean updateSequenceOnWand = true;

        if (sequence == null)
        {
            updateSequenceOnWand = false;
            sequence = stack.getTagCompound().getString(SPELL_SEQUENCE_TAG);
        }

        if (sequence.length() == 0)
            return false;

        Spellcast cast = SpellManager.makeSpell(sequence);

        if (cast == null)
            return false;

        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);
        MagicAmounts cost = cast.getSpellCost();

        if (!MagicDatabase.isInfiniteContainer(stack) && !amounts.hasEnough(cost))
            return false;

        cast = cast.getShape().castSpell(stack, player, cast);
        if (cast != null)
        {
            SpellcastEntityData data = SpellcastEntityData.get(player);
            if (data != null)
            {
                data.begin(cast);
            }
        }

        if (!MagicDatabase.isInfiniteContainer(stack))
            amounts.subtract(cost);

        MagicDatabase.setContainedMagic(stack, amounts);

        DiscoveryHandler.instance.onSpellcast(player, cast);
        return updateSequenceOnWand;
    }

    public void processSequenceUpdate(SpellSequenceUpdate message, ItemStack stack, EntityPlayer player)
    {
        if (message.changeMode == SpellSequenceUpdate.ChangeMode.COMMIT)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null)
            {
                if (!MagicDatabase.isInfiniteContainer(stack))
                    return;

                nbt = new NBTTagCompound();
                stack.setTagCompound(nbt);
            }

            if (onSpellCommit(stack, player, message.sequence))
            {
                nbt.setString(SPELL_SEQUENCE_TAG, message.sequence);
            }
        }
    }
}
