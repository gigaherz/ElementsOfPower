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
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class NbtToModel implements IUnbakedGeometry<NbtToModel>
{
    final String key;
    final Map<String, BlockModel> modelMap;

    public NbtToModel(String key, Map<String, BlockModel> modelMap)
    {
        this.key = key;
        this.modelMap = modelMap;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
    {
        var particle = spriteGetter.apply(owner.getMaterial("particle"));
        var bakedMap = new HashMap<>(Maps.transformEntries(modelMap, (k, v) ->
                v.bake(baker, v, spriteGetter, modelTransform, new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath() + "/" + k), owner.useBlockLight())));

        var overrides1 = new ItemOverrides(baker, null, Collections.emptyList(), spriteGetter)
        {
            final String nbtKey = key;
            final Map<String, BakedModel> models = bakedMap;

            @Nullable
            @Override
            public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int p_173469_)
            {
                var result = overrides.resolve(originalModel, stack, world, entity, p_173469_);
                if (result != originalModel)
                    return result;

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

        return new Baked(particle, owner.useBlockLight(), overrides1);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context)
    {
        for (BlockModel model : modelMap.values())
            model.resolveParents(modelGetter);
    }

    public static class Baked implements BakedModel
    {
        private final boolean isSideLit;
        private final TextureAtlasSprite particle;
        private final ItemOverrides overrides;

        public Baked(TextureAtlasSprite particle, boolean isSideLit, ItemOverrides overrides)
        {
            this.isSideLit = isSideLit;
            this.particle = particle;
            this.overrides = overrides;
        }

        @Override
        public List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState p_235039_, @org.jetbrains.annotations.Nullable Direction p_235040_, RandomSource p_235041_)
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

    public static class Loader implements IGeometryLoader<NbtToModel>
    {
        public static final Loader INSTANCE = new Loader();

        @Override
        public NbtToModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext)
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
