package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import gigaherz.elementsofpower.gemstones.Element;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.progression.DiscoveryHandler;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ItemGemContainer extends ItemMagicContainer
{
    public static final String SPELL_SEQUENCE_TAG = "SpellSequence";

    @Override
    public boolean isInfinite(ItemStack stack)
    {
        return stack.getMetadata() == Gemstone.values.length;
    }

    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        Gemstone g = getGemstone(stack);
        Quality q = getQuality(stack);
        if (q == null)
            return null;

        MagicAmounts magic = ItemGemstone.capacities[q.ordinal()].copy();

        Element e = g.getElement();
        if (e == null)
            magic.all(magic.amounts[0] * 0.1f);
        else
            magic.element(g.getElement(), magic.amount(g.getElement()) * 0.25f);

        return magic;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack)
    {
        Quality q = getQuality(stack);
        if (q == null)
            return EnumRarity.COMMON;
        return q.getRarity();
    }

    public Gemstone getGemstone(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return null;

        if (!tag.hasKey("gemstone", Constants.NBT.TAG_INT))
            return null;

        int g = tag.getInteger("gemstone");
        if (g < 0 || g > Gemstone.values.length)
            return null;

        return Gemstone.values[g];
    }

    public ItemStack setGemstone(ItemStack stack, Gemstone gemstone)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (gemstone == null)
        {
            if (tag != null)
            {
                tag.removeTag("gemstone");
                if (tag.getKeySet().size() == 0)
                {
                    stack.setTagCompound(null);
                }
            }
            return stack;
        }

        if (tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        tag.setInteger("gemstone", gemstone.ordinal());

        return stack;
    }

    public Quality getQuality(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return null;

        if (!tag.hasKey("quality", Constants.NBT.TAG_INT))
            return null;

        int q = tag.getInteger("quality");
        if (q < 0 || q > Quality.values.length)
            return null;

        return Quality.values[q];
    }

    public ItemStack setQuality(ItemStack stack, Quality q)
    {
        NBTTagCompound tag = stack.getTagCompound();

        if (q == null)
        {
            if (tag != null)
            {
                tag.removeTag("quality");
                if (tag.getKeySet().size() == 0)
                {
                    stack.setTagCompound(null);
                }
            }
            return stack;
        }

        if (tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        tag.setInteger("quality", q.ordinal());

        return stack;
    }

    public ItemStack getContainedGemstone(ItemStack stack)
    {
        Gemstone gem = getGemstone(stack);
        Quality q = getQuality(stack);

        if (gem == null)
            return null;

        ItemStack t = ElementsOfPower.gemstone.getStack(1, gem.ordinal());

        if (q != null)
        {
            t = ElementsOfPower.gemstone.setQuality(t, q);
        }

        MagicAmounts am = ContainerInformation.getContainedMagic(stack);

        if (am != null)
        {
            am = adjustRemovedMagic(am);

            t = ContainerInformation.setContainedMagic(t, am);
        }

        return t;
    }

    public ItemStack setContainedGemstone(ItemStack stack, ItemStack gemstone)
    {
        if (gemstone == null)
        {
            return ContainerInformation.setContainedMagic(setQuality(setGemstone(stack, null), null), null);
        }

        if (!(gemstone.getItem() instanceof ItemGemstone))
            return null;

        ItemGemstone g = ((ItemGemstone) gemstone.getItem());
        Gemstone gem = g.getGemstone(gemstone);
        Quality q = g.getQuality(gemstone);

        MagicAmounts am = ContainerInformation.getContainedMagic(gemstone);

        am = adjustInsertedMagic(am);

        return ContainerInformation.setContainedMagic(setQuality(setGemstone(stack, gem), q), am);
    }

    protected MagicAmounts adjustInsertedMagic(MagicAmounts am)
    {
        return am;
    }

    protected MagicAmounts adjustRemovedMagic(MagicAmounts am)
    {
        return am;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || oldStack == null || newStack == null
                || oldStack.getItem() != newStack.getItem() || oldStack.getMetadata() != newStack.getMetadata();
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        Gemstone g = getGemstone(stack);

        if (g == null)
            return getUnlocalizedName();

        return getUnlocalizedName() + g.getUnlocalizedName();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        Quality q = getQuality(stack);

        String namePart = I18n.translateToLocal(getUnlocalizedName(stack) + ".name");

        if (q == null)
            return namePart;

        String quality = I18n.translateToLocal(ElementsOfPower.MODID + ".gemContainer.quality" + q.getUnlocalizedName());

        return quality + " " + namePart;
    }

    @Override
    public ItemStack getStack(int count, int damageValue)
    {
        return new ItemStack(this, count, damageValue);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (!worldIn.isRemote)
        {
            if (playerIn.isSneaking())
            {
                itemStackIn.getTagCompound().removeTag(SPELL_SEQUENCE_TAG);
            }
        }

        // itemInUse handled by TickEventWandControl

        return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
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

        MagicAmounts amounts = ContainerInformation.getContainedMagic(stack);
        MagicAmounts cost = cast.getSpellCost();

        if (!ContainerInformation.isInfiniteContainer(stack) && !amounts.hasEnough(cost))
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

        if (!ContainerInformation.isInfiniteContainer(stack))
            amounts.subtract(cost);

        ContainerInformation.setContainedMagic(stack, amounts);

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
                if (!ContainerInformation.isInfiniteContainer(stack))
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
