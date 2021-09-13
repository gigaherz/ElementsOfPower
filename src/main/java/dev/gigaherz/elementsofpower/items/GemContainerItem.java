package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
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
        return getGemstone(stack) == Gemstone.CREATIVITE;
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
        CompoundTag tag = stack.getTag();
        if (tag == null)
            return null;

        if (tag.contains("gemstone", Constants.NBT.TAG_INT))
        {
            int g = tag.getInt("gemstone");
            if (g < 0 || g > Gemstone.values.size())
                return null;

            Gemstone gem = Gemstone.values.get(g);

            tag.remove("gemstone");
            tag.putString("gemstone", gem.getSerializedName());

            return gem;
        }

        if (tag.contains("gemstone", Constants.NBT.TAG_STRING))
        {
            String g = tag.getString("gemstone");
            return Gemstone.byName(g);
        }

        return null;
    }

    public ItemStack setGemstone(ItemStack stack, @Nullable Gemstone gemstone)
    {
        CompoundTag tag = stack.getTag();
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
            tag = new CompoundTag();
            stack.setTag(tag);
        }

        tag.putString("gemstone", gemstone.getSerializedName());

        return stack;
    }

    @Nullable
    public Quality getQuality(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null)
            return null;

        if (!tag.contains("quality", Constants.NBT.TAG_INT))
            return null;

        int q = tag.getInt("quality");
        return Quality.byIndex(q);
    }

    public ItemStack setQuality(ItemStack stack, @Nullable Quality q)
    {
        CompoundTag tag = stack.getTag();

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
            tag = new CompoundTag();
            stack.setTag(tag);
        }

        tag.putInt("quality", q.getIndex());

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

    public Component getGemstoneName(ItemStack stack)
    {
        Component baseName = super.getName(stack);

        Gemstone g = getGemstone(stack);
        if (g == null)
            return new TranslatableComponent("elementsofpower.gem_container.unbound", baseName);

        return new TranslatableComponent(g.getContainerTranslationKey(), baseName);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        Component gemstoneName = getGemstoneName(stack);

        Quality q = getQuality(stack);
        if (q == null)
            return gemstoneName;

        return new TranslatableComponent(q.getContainerTranslationKey(), gemstoneName);
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.BOW;
    }
}
