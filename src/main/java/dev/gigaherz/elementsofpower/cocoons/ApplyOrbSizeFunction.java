package dev.gigaherz.elementsofpower.cocoons;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.essentializer.menu.IMagicAmountContainer;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplyOrbSizeFunction extends LootItemConditionalFunction
{
    private final MagicAmounts factors;

    protected ApplyOrbSizeFunction(List<LootItemCondition> conditionsIn, MagicAmounts factors)
    {
        super(conditionsIn);
        this.factors = factors;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context)
    {
        BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);

        if (!(te instanceof IMagicAmountContainer))
            return stack;

        MagicAmounts am = ((IMagicAmountContainer) te).getContainedMagic();
        RandomSource rand = context.getRandom();
        ItemStack tool = Objects.requireNonNull(context.getParamOrNull(LootContextParams.TOOL));

        float a = 0;
        for (int i = 0; i < 8; i++)
        {
            a += am.get(i) * factors.get(i);
        }

        int whole = (int) Math.floor(a);
        if (rand.nextFloat() < (a - whole))
            whole++;

        if (whole > 0)
        {
            int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
            whole = Math.round((float) Math.pow(rand.nextFloat(), 1 / (fortune + 1.0f)) * whole);
        }

        stack.setCount(whole);

        return stack;
    }

    public static dev.gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction.Builder builder()
    {
        return new dev.gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction.Builder();
    }

    @Override
    public LootItemFunctionType getType()
    {
        return ElementsOfPowerMod.APPLY_ORB_SIZE.get();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<dev.gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction.Builder>
    {
        private final MagicAmounts.Accumulator factors = MagicAmounts.builder();

        public Builder()
        {
        }

        public dev.gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction.Builder with(Element e)
        {
            return with(e, 1);
        }

        public dev.gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction.Builder with(Element e, float factor)
        {
            factors.add(e.ordinal(), factor);
            return this;
        }

        @Override
        protected dev.gigaherz.elementsofpower.cocoons.ApplyOrbSizeFunction.Builder getThis()
        {
            return this;
        }

        @Override
        public ApplyOrbSizeFunction build()
        {
            return new ApplyOrbSizeFunction(this.getConditions(), factors.toAmounts());
        }
    }

    public static final Codec<ApplyOrbSizeFunction> CODEC = RecordCodecBuilder.create((instance) ->
            commonFields(instance)
            .and(MagicAmounts.CODEC.fieldOf("factors").forGetter((o) -> o.factors))
            .apply(instance, ApplyOrbSizeFunction::new));
}
