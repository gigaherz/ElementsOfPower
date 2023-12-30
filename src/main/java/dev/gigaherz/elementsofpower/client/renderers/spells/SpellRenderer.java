package dev.gigaherz.elementsofpower.client.renderers.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.client.renderers.ModelHandle;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
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

import org.jetbrains.annotations.Nullable;


public abstract class SpellRenderer
{
    public static final NonNullLazy<ModelHandle> modelCone = NonNullLazy.of(() -> ModelHandle.of("elementsofpower:models/entity/cone.obj"));
    public static final NonNullLazy<ModelHandle> modelSphere = NonNullLazy.of(() -> ModelHandle.of("elementsofpower:models/entity/sphere.obj"));
    public static final NonNullLazy<ModelHandle> modelSphereInside = NonNullLazy.of(() -> ModelHandle.of("elementsofpower:models/entity/sphere_inside.obj"));
    public static final NonNullLazy<ModelHandle> modelCyl = NonNullLazy.of(() -> ModelHandle.of("elementsofpower:models/entity/cylinder.obj"));

    public static int getColor(SpellcastState spellcast)
    {
        int color = spellcast.color();

        if (spellcast.effect() instanceof FlameEffect)
        {
            color = 0xFFFFFF;
        }
        else if (spellcast.effect() instanceof WaterEffect)
        {
            color = 0xFFFFFF;
        }
        else
        {
            if (spellcast.effect() instanceof WindEffect)
            {
                color = 0xFFFFFF;
            }
        }

        return color;
    }

    public static ResourceLocation getTexture(@Nullable SpellcastState spellcast, @Nullable Spellcast entitySpellcast)
    {
        String tex = "neoforge:textures/white.png";

        if (spellcast != null)
        {
            if (spellcast.effect() instanceof FlameEffect)
            {
                tex = "minecraft:textures/block/lava_still.png";
            }
            else if (spellcast.effect() instanceof WaterEffect)
            {
                tex = "minecraft:textures/block/water_still.png";
            }
            else
            {
                if (spellcast.effect() instanceof WindEffect)
                {
                    tex = "elementsofpower:textures/block/cone.png";
                }
            }
        }

        return new ResourceLocation(tex);
    }

    public static RenderType getRenderType(@Nullable SpellcastState state, @Nullable Spellcast spellcast)
    {
        return RenderType.entityTranslucent(getTexture(state, spellcast));
    }

    public abstract void render(SpellcastState state, Player player, EntityRenderDispatcher renderManager, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Vec3 offset);
}
