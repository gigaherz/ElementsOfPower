package dev.gigaherz.elementsofpower.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
    {
        return new Baked(
                spriteGetter.apply(owner.resolveTexture("particle")),
                owner.isSideLit(), bakery, owner.getOwnerModel(), bakery::getModel, spriteGetter, key,
                Maps.transformEntries(modelMap, (k, v) -> v.bake(bakery, v, spriteGetter, modelTransform, new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath() + "/" + k), owner.isSideLit()))
        );
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
    {
        Set<Material> materials = new HashSet<>();
        for (BlockModel model : modelMap.values())
        {materials.addAll(model.getMaterials(modelGetter, missingTextureErrors));}
        return materials;
    }

    public static class Baked implements BakedModel
    {
        private final boolean isSideLit;
        private final TextureAtlasSprite particle;
        private final ItemOverrides overrides;

        public Baked(TextureAtlasSprite particle, boolean isSideLit, ModelBakery bakery, UnbakedModel ownerModel, Function<ResourceLocation, UnbakedModel> modelGetter, Function<Material, TextureAtlasSprite> textureGetter,
                     String key, Map<String, BakedModel> modelMap)
        {
            this.isSideLit = isSideLit;
            this.particle = particle;
            this.overrides = new ItemOverrides(bakery, ownerModel, modelGetter, textureGetter, Collections.emptyList())
            {
                final String nbtKey = key;
                final Map<String, BakedModel> models = modelMap;

                @Nullable
                @Override
                public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int p_173469_)
                {
                    CompoundTag tag = stack.getTag();
                    Tag tagValue = (tag != null) ? tag.get(nbtKey) : null;

                    String value = tagValue != null ? tagValue.getAsString() : null;
                    if (value == null)
                    {
                        value = "";
                    }
                    return models.getOrDefault(value, null);
                }
            };
        }

        @Deprecated
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)
        {
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion()
        {
            return true;
        }

        @Override
        public boolean isGui3d()
        {
            return true;
        }

        @Override
        public boolean usesBlockLight()
        {
            return isSideLit;
        }

        @Override
        public boolean isCustomRenderer()
        {
            return false;
        }

        @Deprecated
        @Override
        public TextureAtlasSprite getParticleIcon()
        {
            return particle;
        }

        @Override
        public ItemOverrides getOverrides()
        {
            return overrides;
        }
    }

    public static class Loader implements IModelLoader<NbtToModel>
    {
        public static final Loader INSTANCE = new Loader();

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager)
        {

        }

        @Override
        public NbtToModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
        {
            String key = GsonHelper.getAsString(modelContents, "tag");
            JsonObject obj = GsonHelper.getAsJsonObject(modelContents, "values");
            ImmutableMap.Builder<String, BlockModel> builder = ImmutableMap.<String, BlockModel>builder();
            for (Map.Entry<String, JsonElement> kv : obj.entrySet())
            {
                builder.put(kv.getKey(), deserializationContext.deserialize(kv.getValue(), BlockModel.class));
            }
            return new NbtToModel(key, builder.build());
        }
    }
}