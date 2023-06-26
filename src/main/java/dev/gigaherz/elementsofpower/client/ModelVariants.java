package dev.gigaherz.elementsofpower.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.SeparateTransformsModel;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class ModelVariants implements IUnbakedGeometry<ModelVariants>
{
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

    public static class Loader implements IGeometryLoader<ModelVariants>
    {
        public static final Loader INSTANCE = new Loader();

        @Override
        public ModelVariants read(JsonObject modelContents, JsonDeserializationContext deserializationContext)
        {
            JsonObject defaultValues = GsonHelper.getAsJsonObject(modelContents, "default", new JsonObject());

            JsonObject perspectives = GsonHelper.getAsJsonObject(modelContents, "perspectives", new JsonObject());

            JsonArray nbtConditions = GsonHelper.getAsJsonArray(modelContents, "nbt", new JsonArray());

            List<Pair<String, Map<String, JsonObject>>> nbtVariants = new ArrayList<>();
            for(var e : nbtConditions)
            {
                var entry = e.getAsJsonObject();
                var path = GsonHelper.getAsString(entry,"tag");
                var values = GsonHelper.getAsJsonObject(entry,"values");

                var variantMap = new HashMap<String, JsonObject>();

                for(var key : values.keySet())
                {
                    var value = GsonHelper.getAsJsonObject(values, key);

                    variantMap.put(key, value);
                }

                nbtVariants.add(Pair.of(path, variantMap));
            }

            var defaultModel = (BlockModel)deserializationContext.deserialize(defaultValues, BlockModel.class);

            var variantsMap = makeVariantsMap(defaultValues, nbtVariants, deserializationContext, perspectives);

            return new NbtVariants(defaultModel, variantsMap);
        }

        private List<Pair<Predicate<CompoundTag>, PerspectiveVariants>> makeVariantsMap(JsonObject defaultJson, List<Pair<String, Map<String, JsonObject>>> nbtVariants, JsonDeserializationContext deserializationContext, JsonObject perspectives)
        {
            List<Pair<Predicate<CompoundTag>, PerspectiveVariants>> list = new ArrayList<>();
            makeRecursive(defaultJson, nbtVariants, 0, deserializationContext, perspectives, tag -> true, list);
            return list;
        }

        private void makeRecursive(JsonObject defaultJson, List<Pair<String, Map<String, JsonObject>>> nbtVariants, int index, JsonDeserializationContext deserializationContext,
                                   JsonObject perspectives,
                                   Predicate<CompoundTag> predicate, List<Pair<Predicate<CompoundTag>, PerspectiveVariants>> list)
        {
            if (index == nbtVariants.size())
            {
                Map<ItemDisplayContext, BlockModel> perspectiveVariants = new HashMap<>();
                for (ItemDisplayContext transform : ItemDisplayContext.values())
                {
                    if (perspectives.has(transform.getSerializedName()))
                    {
                        var perspectiveDefaults = GsonHelper.getAsJsonObject(perspectives, transform.getSerializedName());

                        var combinedJson = combineModel(defaultJson, perspectiveDefaults);
                        var combinedModel = (BlockModel)deserializationContext.deserialize(combinedJson, BlockModel.class);

                        perspectiveVariants.put(transform, combinedModel);
                    }
                }

                var baseModel = (BlockModel)deserializationContext.deserialize(defaultJson, BlockModel.class);

                list.add(Pair.of(predicate, new PerspectiveVariants(baseModel, perspectiveVariants)));
                return;
            }

            var entry =nbtVariants.get(index);

            var tagName = entry.getFirst();
            var entryMap = entry.getSecond();

            for(var e : entryMap.entrySet())
            {
                var key = e.getKey();
                var value = e.getValue();
                var predicate1 = predicate.and(tag -> (key.equals("") && (tag == null ||tag.size() == 0) || (tag != null && Objects.equals(tag.getString(tagName), key))));
                var combinedValue = combineModel(defaultJson, value);
                makeRecursive(combinedValue, nbtVariants, index+1, deserializationContext, perspectives, predicate1, list);
            }
        }

        private static JsonObject combineModel(JsonObject base, JsonObject layer)
        {
            if (layer.size() == 0)
                return base;

            var combined = base.deepCopy();

            for (var key : layer.keySet())
            {
                var layered = layer.get(key);
                combined.add(key, switch (key)
                {
                    case "display", "textures" -> {
                        var original = combined.get(key);
                        yield original != null && original.isJsonObject() && layered.isJsonObject()
                                ? combineObject(original.getAsJsonObject(), GsonHelper.getAsJsonObject(layer, key))
                                : layered;
                    }
                    default -> layered;
                });
            }

            return combined;
        }

        private static JsonObject combineObject(JsonObject base, JsonObject layer)
        {
            if (layer.size() == 0)
                return base;

            var combined = base.deepCopy();

            for (var key : layer.keySet())
            {
                combined.add(key, layer.get(key));
            }

            return combined;
        }

        private static class NbtVariants extends ModelVariants
        {
            private final BlockModel defaultModel;
            private final List<Pair<Predicate<CompoundTag>, PerspectiveVariants>> variantsMap;

            public NbtVariants(BlockModel defaultModel, List<Pair<Predicate<CompoundTag>, PerspectiveVariants>> variantsMap)
            {
                super();
                this.defaultModel = defaultModel;
                this.variantsMap = variantsMap;
            }

            @Override
            public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation)
            {
                var particle = spriteGetter.apply(context.getMaterial("particle"));
                final List<Pair<Predicate<CompoundTag>, BakedModel>> models =
                        variantsMap.stream().map(a -> a.mapSecond(model -> model.bake(context, baker, spriteGetter, modelState, ItemOverrides.EMPTY, modelLocation))).toList();

                var overrides1 = new ItemOverrides()
                {
                    @Nullable
                    @Override
                    public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed)
                    {
                        for(var entry : models)
                        {
                            if (entry.getFirst().test(pStack.getTag()))
                                return entry.getSecond().getOverrides().resolve(entry.getSecond(), pStack, pLevel, pEntity, pSeed);
                        }

                        return overrides.resolve(pModel, pStack, pLevel, pEntity, pSeed);
                    }
                };

                return new Baked(particle, context.useBlockLight(), overrides1);
            }

            @Override
            public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context)
            {
                variantsMap.forEach(e -> e.getSecond().resolveParents(modelGetter, context));
            }
        }

        private static class PerspectiveVariants extends ModelVariants
        {
            private final BlockModel defaults;
            private final Map<ItemDisplayContext, BlockModel> perspectiveVariants;

            public PerspectiveVariants(BlockModel defaults, Map<ItemDisplayContext, BlockModel> perspectiveVariants)
            {
                super();
                this.defaults = defaults;
                this.perspectiveVariants = perspectiveVariants;
            }

            @Override
            public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation)
            {
                if (perspectiveVariants.size() == 0)
                    return defaults.bake(baker, defaults, spriteGetter, modelState, modelLocation, context.useBlockLight());

                return new SeparateTransformsModel.Baked(
                        context.useAmbientOcclusion(), context.isGui3d(), context.useBlockLight(),
                        spriteGetter.apply(context.getMaterial("particle")), overrides,
                        defaults.bake(baker, defaults, spriteGetter, modelState, modelLocation, context.useBlockLight()),
                        ImmutableMap.copyOf(Maps.transformValues(perspectiveVariants, value ->
                                value.bake(baker, value, spriteGetter, modelState, modelLocation, context.useBlockLight()))));
            }

            @Override
            public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context)
            {
                perspectiveVariants.values().forEach(e -> e.resolveParents(modelGetter));
            }
        }
    }
}
