package gigaherz.elementsofpower.integration.aequivaleo;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.mediation.IMediationCandidate;
import com.ldtteam.aequivaleo.api.mediation.IMediationEngine;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class EssenceGroupType extends ForgeRegistryEntry<ICompoundTypeGroup> implements ICompoundTypeGroup
{
    @Override
    public IMediationEngine getMediationEngine()
    {
        return context -> {
            return context
                    .getCandidates()
                    .stream()
                    .min((o1, o2) -> {
                        if (o1.isSourceIncomplete() && !o2.isSourceIncomplete())
                            return 1;

                        if (!o1.isSourceIncomplete() && o2.isSourceIncomplete())
                            return -1;

                        if (o1.getValues().isEmpty() && !o2.getValues().isEmpty())
                            return 1;

                        if (!o1.getValues().isEmpty() && o2.getValues().isEmpty())
                            return -1;

                        return Double.compare(o1.getValues().stream().mapToDouble(CompoundInstance::getAmount).sum(),
                                o2.getValues().stream().mapToDouble(CompoundInstance::getAmount).sum());
                    })
                    .map(IMediationCandidate::getValues);
        };
    }

    @Override
    public boolean shouldIncompleteRecipeBeProcessed(IEquivalencyRecipe iEquivalencyRecipe)
    {
        return false;
    }

    @Override
    public boolean canContributeToRecipeAsInput(IEquivalencyRecipe iEquivalencyRecipe, CompoundInstance compoundInstance)
    {
        return true; // !(iEquivalencyRecipe instanceof ITagEquivalencyRecipe);
    }

    @Override
    public boolean canContributeToRecipeAsOutput(IEquivalencyRecipe iEquivalencyRecipe, CompoundInstance compoundInstance)
    {
        return true; // !(iEquivalencyRecipe instanceof ITagEquivalencyRecipe);
    }

    @Override
    public boolean isValidFor(ICompoundContainer<?> iCompoundContainer, CompoundInstance compoundInstance)
    {
        return true;
    }

    @Override
    public Optional<?> mapEntry(Set<CompoundInstance> instances)
    {
        return AequivaleoPlugin.getMagicAmounts(instances);
    }
}
