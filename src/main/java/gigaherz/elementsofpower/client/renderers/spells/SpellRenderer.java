package gigaherz.elementsofpower.client.renderers.spells;

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
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;


public abstract class SpellRenderer
{
    public static final Lazy<ModelHandle> modelCone = Lazy.of(() -> ModelHandle.of("elementsofpower:models/entity/cone.obj"));
    public static final Lazy<ModelHandle> modelSphere = Lazy.of(() -> ModelHandle.of("elementsofpower:models/entity/sphere.obj"));
    public static final Lazy<ModelHandle> modelSphereInside = Lazy.of(() -> ModelHandle.of("elementsofpower:models/entity/sphere_inside.obj"));
    public static final Lazy<ModelHandle> modelCyl = Lazy.of(() -> ModelHandle.of("elementsofpower:models/entity/cylinder.obj"));

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
        String tex = "forge:textures/white.png";

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

    public static RenderType getRenderType(Spellcast spellcast)
    {
        return RenderType.entityTranslucent(getTexture(spellcast));
    }

    public abstract void render(Spellcast spellcast, PlayerEntity player, EntityRendererManager renderManager, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, Vec3d offset);
}
