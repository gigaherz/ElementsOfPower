package dev.gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Function;

public class ModelHandle
{
    private final Random rand = new Random();
    private final BakedModel model;

    public static ModelHandle of(String modelLocation)
    {
        return new ModelHandle(new ResourceLocation(modelLocation));
    }

    public ModelHandle(ResourceLocation modelLocation)
    {
        OBJModel model = OBJLoader.INSTANCE.loadModel(new OBJModel.ModelSettings(modelLocation, false, true, true, true, null));
        this.model = model.bake(new FakeModelConfiguration(modelLocation), ForgeModelBakery.instance(), FakeSprite.GETTER, BlockModelRotation.X0_Y0, null, modelLocation);
    }

    public void render(MultiBufferSource bufferIn, RenderType rt, PoseStack matrixStackIn, int packedLightIn, int color)
    {
        render(bufferIn, rt, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY, color);
    }

    public void render(MultiBufferSource bufferIn, RenderType rt, PoseStack matrixStackIn, int packedLightIn, int overlay, int color)
    {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = ((color >> 0) & 0xFF) / 255.0f;

        VertexConsumer bb = bufferIn.getBuffer(rt);
        for (BakedQuad quad : model.getQuads(null, null, rand, EmptyModelData.INSTANCE))
        {
            bb.putBulkData(matrixStackIn.last(), quad, r, g, b, a, packedLightIn, overlay, true);
        }
    }

    private static class FakeSprite extends TextureAtlasSprite
    {
        public static final ResourceLocation LOCATION = new ResourceLocation("elementsofpower", "fake");
        public static final FakeSprite INSTANCE = new FakeSprite();
        public static final Function<Material, TextureAtlasSprite> GETTER = (x) -> INSTANCE;

        protected FakeSprite()
        {
            super(new TextureAtlas(LOCATION),
                    new Info(LOCATION, 1, 1, AnimationMetadataSection.EMPTY),
                    0, 1, 1,
                    0, 0, new NativeImage(1, 1, false));
        }

        @Override
        public float getU(double u)
        {
            return (float) u / 16;
        }

        @Override
        public float getV(double v)
        {
            return (float) v / 16;
        }
    }

    private static class FakeModelConfiguration implements IModelConfiguration
    {
        private final ResourceLocation modelLocation;

        public FakeModelConfiguration(ResourceLocation modelLocation)
        {
            this.modelLocation = modelLocation;
        }

        @Nullable
        @Override
        public UnbakedModel getOwnerModel()
        {
            return null;
        }

        @Override
        public String getModelName()
        {
            return modelLocation.toString();
        }

        @Override
        public boolean isTexturePresent(String name)
        {
            return false;
        }

        @Override
        public Material resolveTexture(String name)
        {
            return new Material(FakeSprite.LOCATION, FakeSprite.LOCATION);
        }

        @Override
        public boolean isShadedInGui()
        {
            return true;
        }

        @Override
        public boolean isSideLit()
        {
            return true;
        }

        @Override
        public boolean useSmoothLighting()
        {
            return true;
        }

        @Override
        public ItemTransforms getCameraTransforms()
        {
            return ItemTransforms.NO_TRANSFORMS;
        }

        @Override
        public ModelState getCombinedTransform()
        {
            return BlockModelRotation.X0_Y0;
        }
    }
}
