package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.GemstoneItem;
import gigaherz.elementsofpower.gemstones.Quality;
import gigaherz.elementsofpower.network.UpdateSpellSequence;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public abstract class GemContainerItem extends MagicContainerItem
{
    public GemContainerItem(Properties properties)
    {
        super(properties);
    }

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

        MagicAmounts magic = GemstoneItem.capacities[q.ordinal()];

        Element e = g.getElement();
        if (e == null)
            magic = magic.all(magic.get(0) * 0.1f);
        else
            magic = magic.add(g.getElement(), magic.get(g.getElement()) * 0.25f);

        return magic;
    }

    public ItemStack getStack(Gemstone gemstone)
    {
        return setGemstone(new ItemStack(this, 1), gemstone);
    }

    public ItemStack getStack(Gemstone gemstone, Quality quality)
    {
        return setQuality(setGemstone(new ItemStack(this, 1), gemstone), quality);
    }

    @Override
    public Rarity getRarity(ItemStack stack)
    {
        Quality q = getQuality(stack);
        if (q == null)
            return Rarity.COMMON;
        return q.getRarity();
    }

    @Nullable
    public Gemstone getGemstone(ItemStack stack)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return null;

        if (!tag.contains("gemstone", Constants.NBT.TAG_INT))
            return null;

        int g = tag.getInt("gemstone");
        if (g < 0 || g > Gemstone.values.size())
            return null;

        return Gemstone.values.get(g);
    }

    public ItemStack setGemstone(ItemStack stack, @Nullable Gemstone gemstone)
    {
        CompoundNBT tag = stack.getTag();
        if (gemstone == null)
        {
            if (tag != null)
            {
                tag.remove("gemstone");
            }
            return stack;
        }

        if (tag == null)
        {
            tag = new CompoundNBT();
            stack.setTag(tag);
        }

        tag.putInt("gemstone", gemstone.ordinal());

        return stack;
    }

    @Nullable
    public Quality getQuality(ItemStack stack)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return null;

        if (!tag.contains("quality", Constants.NBT.TAG_INT))
            return null;

        int q = tag.getInt("quality");
        if (q < 0 || q > Quality.values.length)
            return null;

        return Quality.values[q];
    }

    public ItemStack setQuality(ItemStack stack, @Nullable Quality q)
    {
        CompoundNBT tag = stack.getTag();

        if (q == null)
        {
            if (tag != null)
            {
                tag.remove("quality");
            }
            return stack;
        }

        if (tag == null)
        {
            tag = new CompoundNBT();
            stack.setTag(tag);
        }

        tag.putInt("quality", q.ordinal());

        return stack;
    }

    public ItemStack getContainedGemstone(ItemStack stack)
    {
        Gemstone gem = getGemstone(stack);
        Quality q = getQuality(stack);

        if (gem == null)
            return ItemStack.EMPTY;

        final ItemStack t = q != null ? gem.getItem().setQuality(new ItemStack(gem), q) : new ItemStack(gem);

        return MagicContainerCapability.getContainer(stack).map(magic -> {
            MagicAmounts am = magic.getContainedMagic();

            if (am.isEmpty())
                return t;

            MagicAmounts am2 = adjustRemovedMagic(am);

            return MagicContainerCapability.getContainer(t).map(magic2 -> {
                magic2.setContainedMagic(am2);
                return t;
            }).orElse(ItemStack.EMPTY);
        }).orElse(ItemStack.EMPTY);
    }

    public ItemStack setContainedGemstone(ItemStack stack, ItemStack gemStack)
    {

        if (gemStack.getCount() <= 0 || !(gemStack.getItem() instanceof GemstoneItem))
        {
            return setQuality(setGemstone(stack, null), null);
        }

        GemstoneItem g = ((GemstoneItem) gemStack.getItem());
        Gemstone gem = g.getGemstone();
        Quality q = g.getQuality(gemStack);
        ItemStack result = setQuality(setGemstone(stack, gem), q);

        MagicContainerCapability.getContainer(gemStack).ifPresent(magic3 -> {
            MagicAmounts am2 = magic3.getContainedMagic();
            MagicAmounts am3 = adjustInsertedMagic(am2);

            MagicContainerCapability.getContainer(result).ifPresent(magic4 -> {
                magic4.setContainedMagic(am3);
            });
        });

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
        return slotChanged || oldStack.getItem() != newStack.getItem()
                || getGemstone(oldStack) != getGemstone(newStack)
                || getQuality(oldStack) != getQuality(newStack);
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        Gemstone g = getGemstone(stack);

        if (g == null)
            return getTranslationKey();

        return getTranslationKey() + "." + g.getName();
    }

    // FIXME: Make this not suck.
    @Override
    public ITextComponent getDisplayName(ItemStack stack)
    {
        Quality q = getQuality(stack);

        ITextComponent namePart = new TranslationTextComponent(getTranslationKey(stack) + ".name");

        if (q == null)
            return namePart;

        @SuppressWarnings("deprecation")
        ITextComponent quality = new TranslationTextComponent("elementsofpower.gemContainer.quality" + q.getUnlocalizedName());

        return new StringTextComponent(quality.getFormattedText() + " " + namePart.getFormattedText());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote)
        {
            if (playerIn.isShiftKeyDown())
            {
                CompoundNBT tag = itemStackIn.getTag();
                if (tag != null)
                    tag.remove(WandItem.SPELL_SEQUENCE_TAG);
            }
        }

        // itemInUse handled by TickEventWandControl

        return ActionResult.func_226248_a_(itemStackIn);
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack)
    {
        return UseAction.BOW;
    }

    public boolean onSpellCommit(ItemStack stack, PlayerEntity player, @Nullable String sequence)
    {
        final boolean updateSequenceOnWand;
        if (sequence == null)
        {
            updateSequenceOnWand = false;
            CompoundNBT tag = stack.getTag();
            if (tag != null)
            {
                sequence = tag.getString(WandItem.SPELL_SEQUENCE_TAG);
            }
        }
        else
        {
             updateSequenceOnWand = true;
        }

        if (sequence == null || sequence.length() == 0)
            return false;

        final Spellcast cast = SpellManager.makeSpell(sequence);

        if (cast == null)
            return false;

        return MagicContainerCapability.getContainer(stack).map(magic -> {

            MagicAmounts amounts = magic.getContainedMagic();
            MagicAmounts cost = cast.getSpellCost();

            if (!magic.isInfinite() && !amounts.hasEnough(cost))
                return false;

            Spellcast cast2 = cast.getShape().castSpell(stack, player, cast);
            if (cast2 != null)
            {
                SpellcastEntityData.get(player).ifPresent(data -> data.begin(cast2));
            }

            if (!magic.isInfinite())
                amounts = amounts.subtract(cost);

            magic.setContainedMagic(amounts);

            //DiscoveryHandler.instance.onSpellcast(player, cast);
            return updateSequenceOnWand;
        }).orElse(false);
    }

    public void processSequenceUpdate(UpdateSpellSequence message, ItemStack stack, PlayerEntity player)
    {
        if (message.changeMode == UpdateSpellSequence.ChangeMode.COMMIT)
        {
            CompoundNBT nbt = stack.getTag();
            if (nbt == null)
            {
                if (!MagicContainerCapability.getContainer(stack).filter(magic -> !magic.isInfinite()).isPresent())
                    return;

                nbt = new CompoundNBT();
                stack.setTag(nbt);
            }

            if (onSpellCommit(stack, player, message.sequence))
            {
                nbt.putString(WandItem.SPELL_SEQUENCE_TAG, message.sequence);
            }
        }
    }
}
