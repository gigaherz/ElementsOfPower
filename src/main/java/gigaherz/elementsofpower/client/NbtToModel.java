package gigaherz.elementsofpower.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class NbtToModel implements IModelGeometry<NbtToModel>
{
    final String key;
    final Map<String, BlockModel> modelMap;

    public NbtToModel(String key, Map<String, BlockModel> modelMap)
    {
        this.key = key;
        this.modelMap = modelMap;
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation)
    {
        return new BakedModel(
                spriteGetter.apply(owner.resolveTexture("particle")),
                owner.isSideLit(), bakery, owner.getOwnerModel(), bakery::getUnbakedModel, spriteGetter, key,
                Maps.transformEntries(modelMap, (k, v) -> v.bakeModel(bakery, v, spriteGetter, modelTransform, new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath() + "/" + k), owner.isSideLit()))
        );
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
    {
        Set<RenderMaterial> materials = new HashSet<>();
        for (BlockModel model : modelMap.values())
        { materials.addAll(model.getTextures(modelGetter, missingTextureErrors)); }
        return materials;
    }

    public static class BakedModel implements IBakedModel
    {
        private final boolean isSideLit;
        private final TextureAtlasSprite particle;
        private final ItemOverrideList overrides;

        public BakedModel(TextureAtlasSprite particle, boolean isSideLit, ModelBakery bakery, IUnbakedModel ownerModel, Function<ResourceLocation, IUnbakedModel> modelGetter, Function<RenderMaterial, TextureAtlasSprite> textureGetter,
                          String key, Map<String, IBakedModel> modelMap)
        {
            this.isSideLit = isSideLit;
            this.particle = particle;
            this.overrides = new ItemOverrideList(bakery, ownerModel, modelGetter, textureGetter, Collections.emptyList())
            {
                final String nbtKey = key;
                final Map<String, IBakedModel> models = modelMap;

                @Nullable
                @Override
                public IBakedModel func_239290_a_(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity)
                {
                    CompoundNBT tag = stack.getTag();
                    INBT tagValue = (tag != null) ? tag.get(nbtKey) : null;

                    String value = tagValue != null ? tagValue.getString() : null;
                    if (value == null)
                    {
                        value = "";
                    }
                    return models.getOrDefault(value, null);
                }
            };
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)
        {
            return Collections.emptyList();
        }

        @Override
        public boolean isAmbientOcclusion()
        {
            return true;
        }

        @Override
        public boolean isGui3d()
        {
            return true;
        }

        @Override
        public boolean func_230044_c_()
        {
            return isSideLit;
        }

        @Override
        public boolean isBuiltInRenderer()
        {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture()
        {
            return particle;
        }

        @Override
        public ItemOverrideList getOverrides()
        {
            return overrides;
        }
    }

    public static class Loader implements IModelLoader<NbtToModel>
    {
        public static final Loader INSTANCE = new Loader();

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {

        }

        @Override
        public NbtToModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
        {
            String key = JSONUtils.getString(modelContents, "tag");
            JsonObject obj = JSONUtils.getJsonObject(modelContents, "values");
            ImmutableMap.Builder<String, BlockModel> builder = ImmutableMap.<String, BlockModel>builder();
            for (Map.Entry<String, JsonElement> kv : obj.entrySet())
            {
                builder.put(kv.getKey(), deserializationContext.deserialize(kv.getValue(), BlockModel.class));
            }
            return new NbtToModel(key, builder.build());
        }
    }
}
