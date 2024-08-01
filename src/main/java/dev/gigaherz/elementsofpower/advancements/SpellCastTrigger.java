package dev.gigaherz.elementsofpower.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public class SpellCastTrigger extends SimpleCriterionTrigger<SpellCastTrigger.TriggerInstance>
{
    @Override
    public Codec<SpellCastTrigger.TriggerInstance> codec()
    {
        return SpellCastTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer pPlayer)
    {
        this.trigger(pPlayer, trigger -> true);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player)
            implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<SpellCastTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SpellCastTrigger.TriggerInstance::player)
                        )
                        .apply(instance, SpellCastTrigger.TriggerInstance::new)
        );

        public static Criterion<SpellCastTrigger.TriggerInstance> playerCastsSpell(EntityPredicate.Builder pPlayer)
        {
            return ElementsOfPowerMod.SPELLCAST_TRIGGER.get().createCriterion(new SpellCastTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(pPlayer))));
        }

        public static Criterion<SpellCastTrigger.TriggerInstance> playerCastsSpell()
        {
            return playerCastsSpell(EntityPredicate.Builder.entity());
        }
    }
}
