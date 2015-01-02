package gigaherz.elementsofpower.models;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartItemModel;

import java.util.List;

public class CustomMeshModel
        implements ISmartItemModel {

    String model;
    String variant;

    public CustomMeshModel(String modelType, String variantType) {
        this.model = modelType;
        this.variant = variantType;
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack)
    {
        return this;
    }

    @Override
    public List getFaceQuads(EnumFacing face)
    {
        return null;
    }

    @Override
    public List getGeneralQuads()
    {
        return null;
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return false;
    }

    @Override
    public boolean isGui3d()
    {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getTexture()
    {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return null;
    }
}
