package dev.gigaherz.elementsofpower.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
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

public class StaffModel implements IUnbakedGeometry<StaffModel>
{
    private record Variant(String gemstone, String augment) {}

    final Map<Variant, SeparateTransformsModel> modelMap;

    public StaffModel(Map<Variant, SeparateTransformsModel> modelMap)
    {
        this.modelMap = modelMap;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation)
    {
        var particle = spriteGetter.apply(context.getMaterial("particle"));
        var bakedMap = Map.copyOf(Maps.transformValues(modelMap, model -> model.bake(context, baker, spriteGetter, modelState, overrides, modelLocation)));
        var overrides1 = new ItemOverrides()
        {
            @Nullable
            @Override
            public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed)
            {
                var tag = pStack.getTag();
                String main;
                String augment;

                if (tag == null)
                {
                    main = "";
                    augment = "";
                }
                else
                {
                    main = tag.getString("gemstone");
                    augment = tag.getString("augment");
                }

                var variant = new Variant(main, augment);

                var model = bakedMap.get(variant);
                if (model == null)
                {
                    model = bakedMap.get(new Variant("",""));
                    if (model == null)
                        return overrides.resolve(pModel, pStack, pLevel, pEntity, pSeed);
                }

                return model.getOverrides().resolve(model, pStack, pLevel, pEntity, pSeed);
            }
        };

        return new Baked(particle, context.useBlockLight(), overrides1);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context)
    {
        modelMap.values().forEach(model -> model.resolveParents(modelGetter, context));
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
        public List<BakedQuad> getQuads(@Nullable BlockState p_235039_, @Nullable Direction p_235040_, RandomSource p_235041_)
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

    public static class Loader implements IGeometryLoader<StaffModel>
    {
        public static final Loader INSTANCE = new Loader();

        @Override
        public StaffModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext)
        {
            JsonObject gui = GsonHelper.getAsJsonObject(modelContents, "gui", new JsonObject());
            var guiParent = GsonHelper.getAsString(gui, "base");
            var guiGemstones = GsonHelper.getAsJsonObject(gui, "gemstones");
            var guiAugments = GsonHelper.getAsJsonObject(gui, "augments");

            JsonObject hand = GsonHelper.getAsJsonObject(modelContents, "hand", new JsonObject());
            var handParent  = GsonHelper.getAsString(hand, "base");
            var handGemstones = GsonHelper.getAsJsonObject(hand, "gemstones");
            var handAugments = GsonHelper.getAsJsonObject(hand, "augments");

            var gems = new ArrayList<String>();
            gems.add("");
            for(var e : Gemstone.values)
                gems.add(e.getSerializedName());

            Map<Variant, SeparateTransformsModel> variantMap = new HashMap<>();
            for(var main : gems)
            {
                for(var augment : gems)
                {
                    var variant = new Variant(main, augment);

                    var guiJson = new JsonObject();
                    guiJson.addProperty("parent", guiParent);
                    var guiTextures = new JsonObject();
                    guiJson.add("textures", guiTextures);

                    var handJson = new JsonObject();
                    handJson.addProperty("parent", handParent);
                    var handTextures = new JsonObject();
                    handJson.add("textures", handTextures);

                    int i = 1;

                    if (main.length() > 0)
                    {
                        guiTextures.addProperty("layer" + i, GsonHelper.getAsString(guiGemstones, main));
                        handTextures.addProperty("layer" + i, GsonHelper.getAsString(handGemstones, main));
                        i++;
                    }

                    if (augment.length() > 0)
                    {
                        guiTextures.addProperty("layer" + i, GsonHelper.getAsString(guiAugments, augment));
                        handTextures.addProperty("layer" + i, GsonHelper.getAsString(handAugments, augment));
                    }

                    var guiModel = (BlockModel)deserializationContext.deserialize(guiJson, BlockModel.class);
                    var handModel = (BlockModel)deserializationContext.deserialize(handJson, BlockModel.class);

                    var transformsMap = ImmutableMap.of(ItemDisplayContext.GUI, guiModel);

                    var separateModel = new SeparateTransformsModel(handModel, transformsMap);

                    variantMap.put(variant, separateModel);
                }
            }

            return new StaffModel(variantMap);
        }
    }
}
