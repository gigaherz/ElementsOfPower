package gigaherz.elementsofpower.models;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.TRSRTransformation;

public interface IInitializeBakedModel
{
    void initialize(
            TRSRTransformation thirdPerson,
            TRSRTransformation firstPerson,
            TRSRTransformation head,
            TRSRTransformation gui,
            ResourceLocation icon, ModelManager modelManager);
}
