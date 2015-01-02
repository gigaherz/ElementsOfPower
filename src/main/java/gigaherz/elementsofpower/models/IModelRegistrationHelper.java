package gigaherz.elementsofpower.models;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.ResourceLocation;

public interface IModelRegistrationHelper {
    void registerCustomModel(ResourceLocation location, IBakedModel bakedModel);
}
