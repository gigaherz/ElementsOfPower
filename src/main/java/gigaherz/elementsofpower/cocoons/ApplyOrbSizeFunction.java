package gigaherz.elementsofpower.cocoons;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.essentializer.gui.IMagicAmountContainer;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ApplyOrbSizeFunction extends LootFunction
{
    private final float[] factors;

    protected ApplyOrbSizeFunction(ILootCondition[] conditionsIn, float[] factors)
    {
        super(conditionsIn);
        this.factors = factors;
    }

    @Override
    protected ItemStack doApply(ItemStack stack, LootContext context)
    {
        TileEntity te = context.get(LootParameters.BLOCK_ENTITY);

        if (!(te instanceof IMagicAmountContainer))
            return stack;

        MagicAmounts am = ((IMagicAmountContainer) te).getContainedMagic();
        Random rand = context.getRandom();
        ItemStack tool = Objects.requireNonNull(context.get(LootParameters.TOOL));

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
            int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);
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
    public LootFunctionType getFunctionType()
    {
        return ElementsOfPowerMod.APPLY_ORB_SIZE;
    }

    public static class Builder extends LootFunction.Builder<Builder>
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
        protected Builder doCast()
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

    public static class Serializer extends LootFunction.Serializer<ApplyOrbSizeFunction>
    {
        @Override
        public ApplyOrbSizeFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn)
        {
            Builder b = builder();
            JsonObject elements = JSONUtils.getJsonObject(object, "factors");
            for (Map.Entry<String, JsonElement> kv : elements.entrySet())
            {
                String elementName = kv.getKey();
                Element e = Element.byName(elementName);
                if (e == null)
                    throw new RuntimeException("Unknown key for property 'elements': '" + elementName + "'");
                float f = JSONUtils.getFloat(kv.getValue(), elementName);
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
                if (!MathHelper.epsilonEquals(lootFunction.factors[i], 0))
                    factors.addProperty(Element.values[i].getName(), lootFunction.factors[i]);
            }
            json.add("factors", factors);
        }
    }
}
