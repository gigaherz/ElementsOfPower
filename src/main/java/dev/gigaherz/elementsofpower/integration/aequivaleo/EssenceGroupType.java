package dev.gigaherz.elementsofpower.integration.aequivaleo;
/*
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.mediation.IMediationCandidate;
import com.ldtteam.aequivaleo.api.mediation.IMediationEngine;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Set;

public class EssenceGroupType implements ICompoundTypeGroup
{
    private final ResourceLocation registryName;

    public EssenceGroupType(ResourceLocation registryName)
    {
        this.registryName = registryName;
    }

    @Override
    public IMediationEngine getMediationEngine()
    {
        return context -> context
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

                    return Double.compare(
                            o1.getValues().stream().mapToDouble(CompoundInstance::getAmount).sum(),
                            o2.getValues().stream().mapToDouble(CompoundInstance::getAmount).sum());
                })
                .map(IMediationCandidate::getValues);
    }

    @Override
    public boolean shouldIncompleteRecipeBeProcessed(IEquivalencyRecipe iEquivalencyRecipe)
    {
        return true;
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
    public Optional<?> mapEntry(ICompoundContainer<?> container, Set<CompoundInstance> instances)
    {
        return mapEntry(instances);
    }

    @Override
    public Optional<?> mapEntry(Set<CompoundInstance> instances)
    {
        return AequivaleoPlugin.getMagicAmounts(instances);
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    @Override
    public Codec<? extends ICompoundType> getEntryCodec()
    {
        return EssenceType.CODEC;
    }
}
*/