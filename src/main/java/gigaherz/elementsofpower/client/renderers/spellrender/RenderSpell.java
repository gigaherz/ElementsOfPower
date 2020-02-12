package gigaherz.elementsofpower.client.renderers.spellrender;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import gigaherz.elementsofpower.client.renderers.ModelHandle;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.FlameEffect;
import gigaherz.elementsofpower.spells.effects.WaterEffect;
import gigaherz.elementsofpower.spells.effects.WindEffect;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Map;


public abstract class RenderSpell
{
    final ModelHandle modelCone = getCone();
    final ModelHandle modelSphere = getSphere();
    final ModelHandle modelCyl = getCylinder();

    protected ModelHandle getCone()
    {
        return ModelHandle.of("elementsofpower:entity/cone.obj");
    }

    protected ModelHandle getSphere()
    {
        return ModelHandle.of("elementsofpower:entity/sphere.obj");
    }

    protected ModelHandle getCylinder()
    {
        return ModelHandle.of("elementsofpower:entity/cylinder.obj");
    }

    public static int getColor(Spellcast spellcast)
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

    public static ResourceLocation getTexture(@Nullable Spellcast spellcast)
    {
        String tex = "minecraft:white";

        if (spellcast != null)
        {
            if (spellcast.getEffect() instanceof FlameEffect)
            {
                tex = "minecraft:blocks/lava_still";
            }
            else if (spellcast.getEffect() instanceof WaterEffect)
            {
                tex = "minecraft:blocks/water_still";
            }
            else if (spellcast.getEffect() instanceof WindEffect)
            {
                tex = "elementsofpower:blocks/cone";
            }
        }

        return new ResourceLocation(tex);
    }

    public static RenderType getRenderType(Spellcast spellcast)
    {
        return RenderType.entityTranslucent(getTexture(spellcast));
    }

    public abstract void render(Spellcast spellcast, PlayerEntity player, EntityRendererManager renderManager, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, Vec3d offset);
}
