package dev.gigaherz.elementsofpower.integration.aequivaleo;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.mojang.serialization.Codec;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.function.Supplier;

public class EssenceType implements ICompoundType
{
    public final Element element;
    public final Supplier<EssenceGroupType> groupSupplier;

    public EssenceType(Element element, Supplier<EssenceGroupType> groupSupplier)
    {
        this.element = element;
        this.groupSupplier = groupSupplier;
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
    public String toString()
    {
        return "{" + element.getName() + " essence type}";
    }

    public static final Codec<EssenceType> CODEC = StringRepresentable.fromEnum(Element::values).xmap(
            e -> new EssenceType(e, AequivaleoPlugin.ESSENCE),
            EssenceType::getElement
    );
}
