package dev.gigaherz.elementsofpower.integration.aequivaleo;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class EssenceType implements ICompoundType
{
    public final Element element;
    public final Supplier<EssenceGroupType> groupSupplier;
    private ResourceLocation registryName;

    public EssenceType(Element element, Supplier<EssenceGroupType> groupSupplier, ResourceLocation registryName)
    {
        this.element = element;
        this.groupSupplier = groupSupplier;
        this.registryName = registryName;
    }

    @Override
    public ICompoundTypeGroup getGroup()
    {
        return groupSupplier.get();
    }

    public Element getElement()
    {
        return element;
    }

    @Override
    public ICompoundType setRegistryName(ResourceLocation registryName)
    {
        this.registryName = registryName;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }
}
