package gigaherz.elementsofpower.integration.aequivaleo;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import gigaherz.elementsofpower.spells.Element;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Supplier;

public class EssenceType extends ForgeRegistryEntry<ICompoundType> implements ICompoundType
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
}
