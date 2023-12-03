package dev.gigaherz.elementsofpower.client.renderers.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.client.renderers.ModelHandle;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.effects.FlameEffect;
import dev.gigaherz.elementsofpower.spells.effects.WaterEffect;
import dev.gigaherz.elementsofpower.spells.effects.WindEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.NonNullLazy;

import javax.annotation.Nullable;


public abstract class SpellRenderer
{
    public static final NonNullLazy<ModelHandle> modelCone = NonNullLazy.of(() -> ModelHandle.of("elementsofpower:models/entity/cone.obj"));
    public static final NonNullLazy<ModelHandle> modelSphere = NonNullLazy.of(() -> ModelHandle.of("elementsofpower:models/entity/sphere.obj"));
    public static final NonNullLazy<ModelHandle> modelSphereInside = NonNullLazy.of(() -> ModelHandle.of("elementsofpower:models/entity/sphere_inside.obj"));
    public static final NonNullLazy<ModelHandle> modelCyl = NonNullLazy.of(() -> ModelHandle.of("elementsofpower:models/entity/cylinder.obj"));

    public static int getColor(InitializedSpellcast spellcast)
    {
        int color = spellcast.getColor();

        if (spellcast.getEffect() instanceof FlameEffect)
        {
            color = 0xFFFFFF;
        }
        else if (spellcast.getEffect() instanceof WaterEffect)
        {
            color = 0xFFFFFF;
        }
        else if (spellcast.getEffect() instanceof WindEffect)
        {
            color = 0xFFFFFF;
        }

        return color;
    }

    public static ResourceLocation getTexture(@Nullable InitializedSpellcast spellcast)
    {
        String tex = "neoforge:textures/white.png";

        if (spellcast != null)
        {
            if (spellcast.getEffect() instanceof FlameEffect)
            {
                tex = "minecraft:textures/block/lava_still.png";
            }
            else if (spellcast.getEffect() instanceof WaterEffect)
            {
                tex = "minecraft:textures/block/water_still.png";
            }
            else if (spellcast.getEffect() instanceof WindEffect)
            {
                tex = "elementsofpower:textures/block/cone.png";
            }
        }

        return new ResourceLocation(tex);
    }

    public static RenderType getRenderType(InitializedSpellcast spellcast)
    {
        return RenderType.entityTranslucent(getTexture(spellcast));
    }

    public abstract void render(InitializedSpellcast spellcast, Player player, EntityRenderDispatcher renderManager, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Vec3 offset);
}
