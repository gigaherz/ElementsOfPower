package dev.gigaherz.elementsofpower.cocoons;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.essentializer.menu.IMagicAmountContainer;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Map;
import java.util.Objects;

public class ApplyOrbSizeFunction extends LootItemConditionalFunction
{
    private final float[] factors;

    protected ApplyOrbSizeFunction(LootItemCondition[] conditionsIn, float[] factors)
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
            a += am.get(i) * factors[i];
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

    public static Builder builder()
    {
        return new Builder();
    }

    @Override
    public LootItemFunctionType getType()
    {
        return ElementsOfPowerMod.APPLY_ORB_SIZE.get();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder>
    {
        private final Map<Element, Float> factors = Maps.newHashMap();

        public Builder()
        {
        }

        public Builder with(Element e)
        {
            return with(e, 1);
        }

        public Builder with(Element e, float factor)
        {
            factors.put(e, factor);
            return this;
        }

        @Override
        protected Builder getThis()
        {
            return this;
        }

        @Override
        public ApplyOrbSizeFunction build()
        {
            float[] values = new float[8];
            for (Map.Entry<Element, Float> kv : factors.entrySet())
            {
                values[kv.getKey().ordinal()] = kv.getValue();
            }
            return new ApplyOrbSizeFunction(this.getConditions(), values);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyOrbSizeFunction>
    {
        @Override
        public ApplyOrbSizeFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootItemCondition[] conditionsIn)
        {
            Builder b = builder();
            JsonObject elements = GsonHelper.getAsJsonObject(object, "factors");
            for (Map.Entry<String, JsonElement> kv : elements.entrySet())
            {
                String elementName = kv.getKey();
                Element e = Element.byName(elementName);
                if (e == null)
                    throw new RuntimeException("Unknown key for property 'elements': '" + elementName + "'");
                float f = GsonHelper.convertToFloat(kv.getValue(), elementName);
                b.with(e, f);
            }
            return b.build();
        }

        @Override
        public void serialize(JsonObject json, ApplyOrbSizeFunction lootFunction, JsonSerializationContext ctx)
        {
            super.serialize(json, lootFunction, ctx);

            JsonObject factors = new JsonObject();
            for (int i = 0; i < 8; i++)
            {
                if (!Mth.equal(lootFunction.factors[i], 0))
                    factors.addProperty(Element.values[i].getName(), lootFunction.factors[i]);
            }
            json.add("factors", factors);
        }
    }
}
