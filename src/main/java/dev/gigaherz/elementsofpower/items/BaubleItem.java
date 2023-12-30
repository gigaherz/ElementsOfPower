package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.integration.Curios;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class BaubleItem extends GemContainerItem
{
    private static final float MAX_TRANSFER_TICK = 1 / 20.0f;
    private static final float[] TRANSFER_RATES = {
            MAX_TRANSFER_TICK,
            MAX_TRANSFER_TICK * 2,
            MAX_TRANSFER_TICK * 5,
            MAX_TRANSFER_TICK * 10,
            MAX_TRANSFER_TICK * 25
    };

    static
    {
        assert TRANSFER_RATES.length == Quality.values().length;
    }

    enum TransferMode
    {
        PASSIVE,
        ACTIVE,
        DISABLED;

        static TransferMode[] values = values();
    }

    public BaubleItem(Properties properties)
    {
        super(properties);
    }

    @Nullable
    private static Supplier<ItemStack> findInInventory(ItemStack thisStack, @Nullable final NonNullList<ItemStack> b)
    {
        if (b == null)
            return null;

        for (int i = 0; i < b.size(); i++)
        {
            ItemStack s = b.get(i);
            if (canReceiveMagic(thisStack, s))
            {
                final int slot = i;
                return () -> b.get(slot);
            }
        }

        return null;
    }

    @Nullable
    private static Supplier<ItemStack> findInInventory(ItemStack thisStack, @Nullable final Container b)
    {
        if (b == null)
            return null;

        for (int i = 0; i < b.getContainerSize(); i++)
        {
            ItemStack s = b.getItem(i);
            if (canReceiveMagic(thisStack, s))
            {
                final int slot = i;
                return () -> b.getItem(slot);
            }
        }

        return null;
    }

    @Nullable
    private static Supplier<ItemStack> findInInventory(ItemStack thisStack, @Nullable IItemHandler b)
    {
        if (b == null)
            return null;

        for (int i = 0; i < b.getSlots(); i++)
        {
            ItemStack s = b.getStackInSlot(i);
            if (canReceiveMagic(thisStack, s))
            {
                final int slot = i;
                return () -> b.getStackInSlot(slot);
            }
        }

        return null;
    }

    @Nullable
    private static Supplier<ItemStack> findInCurios(ItemStack stack, Player player)
    {
        return Curios.getCurios(player).map(b -> findInInventory(stack, b)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static boolean canReceiveMagic(ItemStack thisStack, ItemStack s)
    {
        return s != thisStack
                && MagicContainerCapability.isNotFull(s) && getTransferMode(s) == TransferMode.PASSIVE;
    }


    protected MagicAmounts adjustInsertedMagic(MagicAmounts am)
    {
        return am.multiply(1.5f);
    }

    protected MagicAmounts adjustRemovedMagic(MagicAmounts am)
    {
        return am.multiply(1 / 1.5f);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
    {
        if (worldIn.isClientSide || !playerIn.isShiftKeyDown())
            return super.use(worldIn, playerIn, handIn);

        ItemStack stack = playerIn.getItemInHand(handIn);

        CompoundTag tag = stack.getTag();
        if (tag == null)
        {
            tag = new CompoundTag();
            stack.setTag(tag);
        }

        TransferMode oldValue = getTransferMode(stack);

        TransferMode newValue = TransferMode.values[(oldValue.ordinal() + 1) % TransferMode.values.length];

        tag.putByte("Active", (byte) newValue.ordinal());

        switch (getTransferMode(stack))
        {
            case ACTIVE:
                playerIn.displayClientMessage(Component.translatable("text.elementsofpower.bauble.active"), true);
                break;
            case PASSIVE:
                playerIn.displayClientMessage(Component.translatable("text.elementsofpower.bauble.passive"), true);
                break;
            case DISABLED:
                playerIn.displayClientMessage(Component.translatable("text.elementsofpower.bauble.disabled"), true);
                break;
        }

        return InteractionResultHolder.success(stack);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        switch (getTransferMode(stack))
        {
            case ACTIVE:
                tooltip.add(Component.translatable("text.elementsofpower.bauble.active").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE));
                break;
            case PASSIVE:
                tooltip.add(Component.translatable("text.elementsofpower.bauble.passive").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
                break;
            case DISABLED:
                tooltip.add(Component.translatable("text.elementsofpower.bauble.disabled").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_RED));
                break;
        }
        tooltip.add(Component.translatable("text.elementsofpower.bauble.toggle").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
    }

    public static TransferMode getTransferMode(ItemStack stack)
    {
        if (!(stack.getItem() instanceof BaubleItem))
            return TransferMode.PASSIVE;

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Active", Tag.TAG_BYTE))
            return TransferMode.values[tag.getByte("Active") % TransferMode.values.length];
        return TransferMode.PASSIVE;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

        if (getTransferMode(stack) != TransferMode.ACTIVE) return;

        if (worldIn.isClientSide)
            return;

        if (!(entityIn instanceof Player))
            return;

        tryTransferToWands(stack, (Player) entityIn);
    }

    protected void tryTransferToWands(ItemStack thisStack, Player player)
    {
        var magic = MagicContainerCapability.getContainer(thisStack);
        if (magic != null) {
            MagicAmounts available = magic.getContainedMagic();

            if (available.isEmpty())
                return;

            Supplier<ItemStack> slotReference = findInInventory(thisStack, player.getInventory());

            if (slotReference == null)
            {
                slotReference = findInCurios(thisStack, player);
            }

            if (slotReference == null)
                return;

            doTransfer(thisStack, magic, available, slotReference);
        }
    }

    private void doTransfer(ItemStack thisStack, IMagicContainer thisMagic,
                            MagicAmounts available, Supplier<ItemStack> slotReference)
    {
        var stack = slotReference.get();
        var magic = MagicContainerCapability.getContainer(stack);
        if (magic != null) {

            MagicAmounts limits = magic.getCapacity();
            MagicAmounts amounts = magic.getContainedMagic();

            if (limits.isEmpty())
                return;

            MagicAmounts remaining = available;

            Gemstone g = getGemstone(thisStack);
            Quality q = getQuality(thisStack);
            if (g != null && q != null)
            {
                float maxTransferFrom = TRANSFER_RATES[q.ordinal()];
                float boost = q.getTransferSpeed();

                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    float maxTransfer = maxTransferFrom;

                    if ((g == Gemstone.DIAMOND || g == Gemstone.CREATIVITE) || g.ordinal() == i)
                        maxTransfer *= boost;

                    float transfer = Math.min(maxTransfer, limits.get(i) - amounts.get(i));
                    if (!thisMagic.isInfinite())
                        transfer = Math.min(remaining.get(i), transfer);
                    if (transfer > 0)
                    {
                        amounts = amounts.add(i, transfer);
                        if (!thisMagic.isInfinite())
                            remaining = remaining.add(i, -transfer);
                    }
                }
            }

            if (magic.getContainedMagic().anyLessThan(amounts))
                magic.setContainedMagic(amounts);

            if (remaining.anyLessThan(available))
            {
                if (!thisMagic.isInfinite())
                    thisMagic.setContainedMagic(remaining);
            }
        }
    }
}
