package gigaherz.elementsofpower.integration.aequivaleo;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Set;

public class EssenceGroupType extends ForgeRegistryEntry<ICompoundTypeGroup> implements ICompoundTypeGroup
{
    @Override
    public Set<CompoundInstance> determineResult(Set<Set<CompoundInstance>> set, Boolean aBoolean)
    {
        return set
            .stream().min((compoundInstances, t1) -> (int) (compoundInstances
                    .stream()
                    .mapToDouble(CompoundInstance::getAmount)
                    .sum() - t1
                    .stream()
                    .mapToDouble(CompoundInstance::getAmount)
                    .sum()))
            .orElse(Sets.newHashSet());
    }

    @Override
    public boolean shouldIncompleteRecipeBeProcessed(IEquivalencyRecipe iEquivalencyRecipe)
    {
        return true;
    }

    @Override
    public boolean canContributeToRecipeAsInput(IEquivalencyRecipe iEquivalencyRecipe, CompoundInstance compoundInstance)
    {
        return true;
    }

    @Override
    public boolean canContributeToRecipeAsOutput(IEquivalencyRecipe iEquivalencyRecipe, CompoundInstance compoundInstance)
    {
        return true;
    }

    @Override
    public boolean isValidFor(ICompoundContainer<?> iCompoundContainer, CompoundInstance compoundInstance)
    {
        return true;
    }
}
