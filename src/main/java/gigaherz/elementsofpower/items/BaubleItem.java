package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import gigaherz.elementsofpower.integration.Curios;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
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
    private static Supplier<ItemStack> findInInventory(ItemStack thisStack, @Nullable final IInventory b)
    {
        if (b == null)
            return null;

        for (int i = 0; i < b.getSizeInventory(); i++)
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
    private static Supplier<ItemStack> findInCurios(ItemStack stack, PlayerEntity player)
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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        if (worldIn.isRemote || !playerIn.isSneaking())
            return super.onItemRightClick(worldIn, playerIn, handIn);

        ItemStack stack = playerIn.getHeldItem(handIn);

        CompoundNBT tag = stack.getTag();
        if (tag == null)
        {
            tag = new CompoundNBT();
            stack.setTag(tag);
        }

        TransferMode oldValue = getTransferMode(stack);

        TransferMode newValue = TransferMode.values[(oldValue.ordinal() + 1) % TransferMode.values.length];

        tag.putByte("Active", (byte) newValue.ordinal());

        switch (getTransferMode(stack))
        {
            case ACTIVE:
                playerIn.sendStatusMessage(new TranslationTextComponent("text.elementsofpower.bauble.active"), true);
                break;
            case PASSIVE:
                playerIn.sendStatusMessage(new TranslationTextComponent("text.elementsofpower.bauble.passive"), true);
                break;
            case DISABLED:
                playerIn.sendStatusMessage(new TranslationTextComponent("text.elementsofpower.bauble.disabled"), true);
                break;
        }

        return ActionResult.resultSuccess(stack);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        switch (getTransferMode(stack))
        {
            case ACTIVE:
                tooltip.add(new TranslationTextComponent("text.elementsofpower.bauble.active").func_240701_a_(TextFormatting.BOLD, TextFormatting.WHITE));
                break;
            case PASSIVE:
                tooltip.add(new TranslationTextComponent("text.elementsofpower.bauble.passive").func_240701_a_(TextFormatting.ITALIC, TextFormatting.GRAY));
                break;
            case DISABLED:
                tooltip.add(new TranslationTextComponent("text.elementsofpower.bauble.disabled").func_240701_a_(TextFormatting.ITALIC, TextFormatting.DARK_RED));
                break;
        }
        tooltip.add(new TranslationTextComponent("text.elementsofpower.bauble.toggle").func_240701_a_(TextFormatting.ITALIC, TextFormatting.DARK_GRAY));
    }

    public static TransferMode getTransferMode(ItemStack stack)
    {
        if (!(stack.getItem() instanceof BaubleItem))
            return TransferMode.PASSIVE;

        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("Active", Constants.NBT.TAG_BYTE))
            return TransferMode.values[tag.getByte("Active") % TransferMode.values.length];
        return TransferMode.PASSIVE;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

        if (getTransferMode(stack) != TransferMode.ACTIVE) return;

        if (worldIn.isRemote)
            return;

        if (!(entityIn instanceof PlayerEntity))
            return;

        tryTransferToWands(stack, (PlayerEntity) entityIn);
    }

    protected void tryTransferToWands(ItemStack thisStack, PlayerEntity player)
    {
        MagicContainerCapability.getContainer(thisStack).ifPresent(magic -> {
            MagicAmounts available = magic.getContainedMagic();

            if (available.isEmpty())
                return;

            Supplier<ItemStack> slotReference = findInInventory(thisStack, player.inventory);

            if (slotReference == null)
            {
                slotReference = findInCurios(thisStack, player);
            }

            if (slotReference == null)
                return;

            doTransfer(thisStack, magic, available, slotReference);
        });
    }

    private void doTransfer(ItemStack thisStack, IMagicContainer thisMagic,
                            MagicAmounts available, Supplier<ItemStack> slotReference)
    {
        ItemStack stack = slotReference.get();
        MagicContainerCapability.getContainer(stack).ifPresent(magic -> {

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

            if (magic.getContainedMagic().lessThan(amounts))
                magic.setContainedMagic(amounts);

            if (remaining.lessThan(available))
            {
                if (!thisMagic.isInfinite())
                    thisMagic.setContainedMagic(remaining);
            }
        });
    }
}
