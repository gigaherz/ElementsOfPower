package dev.gigaherz.elementsofpower.items;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.client.WandUseManager;
import dev.gigaherz.elementsofpower.integration.Curios;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.network.UpdateSpellSequence;
import dev.gigaherz.elementsofpower.spells.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

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
                itemStackIn.remove(ElementsOfPowerMod.SPELL_SEQUENCE);
            }
        }

        if (hand == InteractionHand.MAIN_HAND)
        {
            if (worldIn.isClientSide)
            {
                WandUseManager.instance.handInUse = hand;
            }

            playerIn.startUsingItem(hand);
            return InteractionResultHolder.success(itemStackIn);
        }

        return InteractionResultHolder.pass(itemStackIn);
    }

    public boolean onSpellCommit(ItemStack stack, Player player, @Nullable List<Element> sequence, int useTicks)
    {
        final boolean updateSequenceOnWand;
        if (sequence == null || sequence.size() == 0)
        {
            updateSequenceOnWand = false;
            sequence = getSequence(stack);
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
            player.displayClientMessage(Component.translatable("text.elementsofpower.spell.invalid_sequence"), true);
            return false;
        }

        var magic = MagicContainerCapability.getContainer(stack);
        if (magic == null)
            return false;

        MagicAmounts amounts = magic.getContainedMagic().add(getTotalPlayerReservoir(player));
        MagicAmounts cost = SpellManager.computeCost(cast);

        if (!magic.isInfinite() && !amounts.greaterEqual(cost))
        {
            player.displayClientMessage(Component.translatable("text.elementsofpower.spell.cost_too_high"), true);
            return updateSequenceOnWand;
        }

        if (useTicks < SpellManager.getChargeDuration(sequence))
        {
            player.displayClientMessage(Component.translatable("text.elementsofpower.spell.not_enough_time"), true);
            return updateSequenceOnWand;
        }

        Spellcast cast2 = cast.shape().castSpell(stack, player, cast);
        if (cast2 != null)
        {
            SpellcastState.get(player).begin(cast2);
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
    }

    @Nullable
    public List<Element> getSequence(ItemStack stack)
    {
        var seq = stack.get(ElementsOfPowerMod.SPELL_SEQUENCE);
        return seq != null ? SpellManager.sequenceFromList(seq) : null;
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

            if (item instanceof BaubleItem && BaubleItem.getTransferMode(stack) == TransferMode.PASSIVE)
            {
                var magic = MagicContainerCapability.getContainer(stack);
                MagicAmounts contained = magic != null ? magic.getContainedMagic() : MagicAmounts.EMPTY;
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

            if (item instanceof BaubleItem && BaubleItem.getTransferMode(stack) == TransferMode.PASSIVE)
            {
                var magic = MagicContainerCapability.getContainer(stack);
                if (magic != null) {

                    MagicAmounts contained = magic.getContainedMagic();
                    MagicAmounts required = accumulator.toAmounts();
                    MagicAmounts toSubtract = MagicAmounts.min(contained, required);
                    if (!toSubtract.isEmpty())
                    {
                        MagicAmounts remainder = contained.subtract(toSubtract);
                        magic.setContainedMagic(remainder);
                        accumulator.subtract(toSubtract);
                    }
                }
            }
        }
    }

    public int getChargeDuration(ItemStack stack)
    {
        var seq = getSequence(stack);
        if (seq != null)
        {
            return SpellManager.getChargeDuration(seq);
        }
        return 20;
    }

    public void processSequenceUpdate(UpdateSpellSequence message, ItemStack stack, Player player, int useTicks)
    {
        if (message.changeMode() == UpdateSpellSequence.ChangeMode.COMMIT)
        {
            if (onSpellCommit(stack, player, message.sequence(), useTicks))
            {
                stack.set(ElementsOfPowerMod.SPELL_SEQUENCE, SpellManager.sequenceToList(message.sequence()));
            }
        }

    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand,
                                                   float partialTick, float equipProcess, float swingProcess)
            {
                return WandUseManager.instance.applyCustomArmTransforms(WandItem.this, poseStack, player, arm, itemInHand, partialTick, equipProcess, swingProcess);
            }
        });
    }
}