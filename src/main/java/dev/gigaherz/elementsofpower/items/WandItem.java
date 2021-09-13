package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.client.WandUseManager;
import dev.gigaherz.elementsofpower.integration.Curios;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.network.UpdateSpellSequence;
import dev.gigaherz.elementsofpower.spells.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class WandItem extends GemContainerItem
{
    public static final String SPELL_SEQUENCE_TAG = "SpellSequence";

    public WandItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand)
    {
        ItemStack itemStackIn = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide)
        {
            if (playerIn.isShiftKeyDown())
            {
                CompoundTag tag = itemStackIn.getTag();
                if (tag != null)
                    tag.remove(WandItem.SPELL_SEQUENCE_TAG);
            }
        }

        if (hand == InteractionHand.MAIN_HAND)
        {
            if (worldIn.isClientSide)
            {
                beginTracking(playerIn, hand);
            }

            playerIn.startUsingItem(hand);
            return InteractionResultHolder.success(itemStackIn);
        }

        return InteractionResultHolder.pass(itemStackIn);
    }

    public static void beginTracking(Player playerIn, InteractionHand hand)
    {
        WandUseManager.instance.handInUse = hand;
    }

    public boolean onSpellCommit(ItemStack stack, Player player, @Nullable List<Element> sequence)
    {
        final boolean updateSequenceOnWand;
        if (sequence == null)
        {
            updateSequenceOnWand = false;
            CompoundTag tag = stack.getTag();
            if (tag != null)
            {
                ListTag seq = tag.getList(WandItem.SPELL_SEQUENCE_TAG, Constants.NBT.TAG_STRING);
                sequence = SpellManager.sequenceFromList(seq);
            }
        }
        else
        {
            updateSequenceOnWand = true;
        }

        if (sequence == null || sequence.size() == 0)
            return false;

        final Spellcast cast = SpellManager.makeSpell(sequence);
        if (cast == null)
        {
            player.displayClientMessage(new TranslatableComponent("text.elementsofpower.spell.invalid_sequence"), true);
            return false;
        }

        return MagicContainerCapability.getContainer(stack).map(magic -> {

            MagicAmounts amounts = magic.getContainedMagic().add(getTotalPlayerReservoir(player));
            MagicAmounts cost = cast.getSpellCost();

            if (!magic.isInfinite() && !amounts.hasEnough(cost))
            {
                player.displayClientMessage(new TranslatableComponent("text.elementsofpower.spell.cost_too_high"), true);
                return updateSequenceOnWand;
            }

            InitializedSpellcast cast2 = cast.getShape().castSpell(stack, player, cast);
            if (cast2 != null)
            {
                SpellcastEntityData.get(player).ifPresent(data -> data.begin(cast2));
            }

            // TODO: Subtract from reservoir if needed
            if (!magic.isInfinite())
            {
                cost = subtractFromReservoir(player, cost);
                if (!cost.isEmpty())
                {
                    amounts = amounts.subtract(cost);
                    magic.setContainedMagic(amounts);
                }
            }

            //DiscoveryHandler.instance.onSpellcast(player, cast);
            return updateSequenceOnWand;
        }).orElse(false);
    }

    public static MagicAmounts getTotalPlayerReservoir(Player player)
    {
        MagicAmounts.Accumulator accumulator = MagicAmounts.builder();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
        {
            accumulateReservoir(accumulator, player.getInventory().getItem(i));
        }

        Curios.getCurios(player).forEach(value -> {
            for (int i = 0; i < value.getSlots(); i++)
            {
                accumulateReservoir(accumulator, value.getStackInSlot(i));
            }
        });

        return accumulator.toAmounts();
    }

    private static void accumulateReservoir(MagicAmounts.Accumulator accumulator, ItemStack stack)
    {
        if (stack.getCount() > 0)
        {
            Item item = stack.getItem();

            if (item instanceof BaubleItem && ((BaubleItem) item).getTransferMode(stack) == BaubleItem.TransferMode.PASSIVE)
            {
                MagicAmounts contained = MagicContainerCapability.getContainer(stack).map(IMagicContainer::getContainedMagic).orElse(MagicAmounts.EMPTY);
                accumulator.add(contained);
            }
        }
    }

    public static MagicAmounts subtractFromReservoir(Player player, MagicAmounts cost)
    {
        MagicAmounts.Accumulator accumulator = MagicAmounts.builder();

        accumulator.add(cost);

        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
        {
            subtractFromReservoir(accumulator, player.getInventory().getItem(i));
        }

        Curios.getCurios(player).forEach(value -> {
            for (int i = 0; i < value.getSlots(); i++)
            {
                subtractFromReservoir(accumulator, value.getStackInSlot(i));
            }
        });

        return accumulator.toAmounts();
    }

    private static void subtractFromReservoir(MagicAmounts.Accumulator accumulator, ItemStack stack)
    {
        if (stack.getCount() > 0)
        {
            Item item = stack.getItem();

            if (item instanceof BaubleItem && ((BaubleItem) item).getTransferMode(stack) == BaubleItem.TransferMode.PASSIVE)
            {
                MagicContainerCapability.getContainer(stack).ifPresent(magic -> {

                    MagicAmounts contained = magic.getContainedMagic();
                    MagicAmounts required = accumulator.toAmounts();
                    MagicAmounts toSubtract = MagicAmounts.min(contained, required);
                    if (!toSubtract.isEmpty())
                    {
                        MagicAmounts remainder = contained.subtract(toSubtract);
                        magic.setContainedMagic(remainder);
                        accumulator.subtract(toSubtract);
                    }
                });
            }
        }
    }

    public void processSequenceUpdate(UpdateSpellSequence message, ItemStack stack, Player player)
    {
        if (message.changeMode == UpdateSpellSequence.ChangeMode.COMMIT)
        {
            CompoundTag nbt = stack.getOrCreateTag();

            if (onSpellCommit(stack, player, message.sequence))
            {
                nbt.put(WandItem.SPELL_SEQUENCE_TAG, SpellManager.sequenceToList(message.sequence));
            }
        }
    }
}