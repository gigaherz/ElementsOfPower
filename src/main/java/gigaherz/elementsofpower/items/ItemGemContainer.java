package gigaherz.elementsofpower.items;

import gigaherz.common.state.IItemStateManager;
import gigaherz.common.state.implementation.ItemStateManager;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.capabilities.AbstractMagicContainer;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Element;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.ItemGemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//import gigaherz.elementsofpower.progression.DiscoveryHandler;

public abstract class ItemGemContainer extends ItemMagicContainer
{
    public static final PropertyInteger NORMAL = PropertyInteger.create("meta", 0, 1);

    @Override
    public boolean canContainMagic(ItemStack stack)
    {
        return getGemstone(stack) != null;
    }

    @Override
    public boolean isInfinite(ItemStack stack)
    {
        return getGemstone(stack) == Gemstone.Creativite;
    }

    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        Gemstone g = getGemstone(stack);
        if (g == null)
            return MagicAmounts.EMPTY;

        Quality q = getQuality(stack);
        if (q == null)
            return MagicAmounts.EMPTY;

        MagicAmounts magic = ItemGemstone.capacities[q.ordinal()];

        Element e = g.getElement();
        if (e == null)
            magic = magic.all(magic.get(0) * 0.1f);
        else
            magic = magic.add(g.getElement(), magic.get(g.getElement()) * 0.25f);

        return magic;
    }

    @Override
    public IItemStateManager createStateManager()
    {
        return new ItemStateManager(this, NORMAL);
    }

    public ItemGemContainer(String name)
    {
        super(name);
    }

    public ItemStack getStack(Gemstone gemstone, Quality quality)
    {
        return setQuality(getStack(1, gemstone), quality);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack)
    {
        Quality q = getQuality(stack);
        if (q == null)
            return EnumRarity.COMMON;
        return q.getRarity();
    }

    @Nullable
    public Gemstone getGemstone(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return null;

        if (!tag.hasKey("gemstone", Constants.NBT.TAG_INT))
            return null;

        int g = tag.getInteger("gemstone");
        if (g < 0 || g > Gemstone.values.size())
            return null;

        return Gemstone.values.get(g);
    }

    public ItemStack setGemstone(ItemStack stack, @Nullable Gemstone gemstone)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (gemstone == null)
        {
            if (tag != null)
            {
                tag.removeTag("gemstone");
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

    @Nullable
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

    public ItemStack setQuality(ItemStack stack, @Nullable Quality q)
    {
        NBTTagCompound tag = stack.getTagCompound();

        if (q == null)
        {
            if (tag != null)
            {
                tag.removeTag("quality");
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
            return ItemStack.EMPTY;

        ItemStack t = ElementsOfPower.gemstone.getStack(gem);

        if (q != null)
        {
            t = ElementsOfPower.gemstone.setQuality(t, q);
        }

        IMagicContainer magic = ContainerInformation.getMagic(stack);
        if (magic == null)
            return ItemStack.EMPTY;

        MagicAmounts am = magic.getContainedMagic();

        if (!am.isEmpty())
        {
            am = adjustRemovedMagic(am);

            IMagicContainer magic2 = ContainerInformation.getMagic(t);
            if (magic2 == null)
                return ItemStack.EMPTY;
            magic2.setContainedMagic(am);
        }

        return t;
    }

    public ItemStack setContainedGemstone(ItemStack stack, ItemStack gemstone)
    {
        ItemStack result;
        MagicAmounts am = MagicAmounts.EMPTY;

        if (!(gemstone.getItem() instanceof ItemGemstone))
            return ItemStack.EMPTY;

        if (gemstone.getCount() <= 0)
        {
            result = setQuality(setGemstone(stack, null), null);
        }
        else
        {
            ItemGemstone g = ((ItemGemstone) gemstone.getItem());
            Gemstone gem = g.getGemstone(gemstone);
            Quality q = g.getQuality(gemstone);
            result = setQuality(setGemstone(stack, gem), q);

            IMagicContainer magic3 = ContainerInformation.getMagic(gemstone);

            am = (magic3 == null) ? MagicAmounts.EMPTY : magic3.getContainedMagic();
            am = adjustInsertedMagic(am);
        }

        IMagicContainer magic4 = ContainerInformation.getMagic(result);
        if (magic4 != null)
            magic4.setContainedMagic(am);

        return result;
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
        return slotChanged || oldStack.getItem() != newStack.getItem() || oldStack.getMetadata() != newStack.getMetadata();
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

        @SuppressWarnings("deprecation")
        String namePart = net.minecraft.util.text.translation.I18n.translateToLocal(getUnlocalizedName(stack) + ".name");

        if (q == null)
            return namePart;

        @SuppressWarnings("deprecation")
        String quality = net.minecraft.util.text.translation.I18n.translateToLocal(ElementsOfPower.MODID + ".gemContainer.quality" + q.getUnlocalizedName());

        return quality + " " + namePart;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote)
        {
            if (playerIn.isSneaking())
            {
                NBTTagCompound tag = itemStackIn.getTagCompound();
                if (tag != null)
                    tag.removeTag(ItemWand.SPELL_SEQUENCE_TAG);
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

    public boolean onSpellCommit(ItemStack stack, EntityPlayer player, @Nullable String sequence)
    {
        boolean updateSequenceOnWand = true;

        if (sequence == null)
        {
            updateSequenceOnWand = false;
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null)
            {
                sequence = tag.getString(ItemWand.SPELL_SEQUENCE_TAG);
            }
        }

        if (sequence == null || sequence.length() == 0)
            return false;

        Spellcast cast = SpellManager.makeSpell(sequence);

        if (cast == null)
            return false;

        IMagicContainer magic = ContainerInformation.getMagic(stack);
        if (magic == null)
            return false;

        MagicAmounts amounts = magic.getContainedMagic();
        MagicAmounts cost = cast.getSpellCost();

        if (!magic.isInfinite() && !amounts.hasEnough(cost))
            return false;

        cast = cast.getShape().castSpell(stack, player, cast);
        if (cast != null)
        {
            SpellcastEntityData data = SpellcastEntityData.get(player);
            data.begin(cast);
        }

        if (!magic.isInfinite())
            amounts = amounts.subtract(cost);

        magic.setContainedMagic(amounts);

        //DiscoveryHandler.instance.onSpellcast(player, cast);
        return updateSequenceOnWand;
    }

    public void processSequenceUpdate(SpellSequenceUpdate message, ItemStack stack, EntityPlayer player)
    {
        if (message.changeMode == SpellSequenceUpdate.ChangeMode.COMMIT)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null)
            {
                IMagicContainer magic = ContainerInformation.getMagic(stack);
                if (magic == null)
                    return;

                if (!magic.isInfinite())
                    return;

                nbt = new NBTTagCompound();
                stack.setTagCompound(nbt);
            }

            if (onSpellCommit(stack, player, message.sequence))
            {
                nbt.setString(ItemWand.SPELL_SEQUENCE_TAG, message.sequence);
            }
        }
    }
}
