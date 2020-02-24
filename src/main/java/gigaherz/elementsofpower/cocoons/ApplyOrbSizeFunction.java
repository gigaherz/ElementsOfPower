package gigaherz.elementsofpower.cocoons;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.LootParameter;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.functions.ILootFunction;

import java.util.Objects;
import java.util.Random;

public class ApplyOrbSizeFunction extends LootFunction
{
    public static final LootParameter<MagicAmounts> CONTAINED_MAGIC = new LootParameter<>(ElementsOfPowerMod.location("contained_magic"));

    private final Element element;

    protected ApplyOrbSizeFunction(ILootCondition[] conditionsIn, Element element)
    {
        super(conditionsIn);
        this.element = element;
    }

    @Override
    protected ItemStack doApply(ItemStack stack, LootContext context)
    {
        MagicAmounts am = Objects.requireNonNull(context.get(CONTAINED_MAGIC));
        Random rand = context.getRandom();
        ItemStack tool = Objects.requireNonNull(context.get(LootParameters.TOOL));

        int i = element.ordinal();

        float a = am.get(i);
        int whole = (int) Math.floor(a);
        if (rand.nextFloat() < (a - whole))
            whole++;

        if (whole > 0)
        {
            int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);
            if (fortune >= 1)
                whole = (int) (Math.pow(rand.nextFloat(), 1 / (fortune - 1.0)) * whole);
            else
                whole = (int) (Math.pow(rand.nextFloat(), 3 - fortune) * whole);

            stack.setCount(whole);
        }

        return stack;
    }

    public static IBuilder builder(Element e)
    {
        return new Builder(e);
    }

    public static class Builder extends LootFunction.Builder<Builder>
    {
        private final Element element;

        public Builder(Element e)
        {
            this.element = e;
        }

        @Override
        protected Builder doCast()
        {
            return this;
        }

        @Override
        public ILootFunction build()
        {
            return new ApplyOrbSizeFunction(this.getConditions(), element);
        }
    }

    public static class Serializer extends LootFunction.Serializer<ApplyOrbSizeFunction>
    {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer()
        {
            super(ElementsOfPowerMod.location("apply_orb_size"), ApplyOrbSizeFunction.class);
        }

        @Override
        public ApplyOrbSizeFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn)
        {
            String elementName = JSONUtils.getString(object, "element");
            Element e = Element.byName(elementName);
            if (e == null)
                throw new RuntimeException("Unknown value for property 'element': '" + elementName + "'");
            return new ApplyOrbSizeFunction(conditionsIn, e);
        }

        @Override
        public void serialize(JsonObject object, ApplyOrbSizeFunction lootFunction, JsonSerializationContext serializationContext)
        {
            super.serialize(object, lootFunction, serializationContext);
            object.addProperty("element", lootFunction.element.getName());
        }
    }
}
