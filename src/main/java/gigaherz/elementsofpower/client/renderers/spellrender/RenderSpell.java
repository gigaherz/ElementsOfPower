package gigaherz.elementsofpower.client.renderers.spellrender;

import com.google.common.collect.Maps;
import gigaherz.common.client.ModelHandle;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.FlameEffect;
import gigaherz.elementsofpower.spells.effects.WaterEffect;
import gigaherz.elementsofpower.spells.effects.WindEffect;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Map;


public abstract class RenderSpell
{
    Map<String, ModelHandle> cones = Maps.newHashMap();
    Map<String, ModelHandle> spheres = Maps.newHashMap();
    Map<String, ModelHandle> cylinders = Maps.newHashMap();

    protected ModelHandle getCone(String tex)
    {
        return cones.computeIfAbsent(tex, k -> ModelHandle.of("elementsofpower:entity/cone.obj").replace("#Default", tex));
    }

    protected ModelHandle getSphere(String tex)
    {
        return spheres.computeIfAbsent(tex, k -> ModelHandle.of("elementsofpower:entity/sphere.obj").replace("#Default", tex));
    }

    protected ModelHandle getCylinder(String tex)
    {
        return cylinders.computeIfAbsent(tex, k -> ModelHandle.of("elementsofpower:entity/cylinder.obj").replace("#Default", tex));
    }

    public abstract void doRender(Spellcast spellcast, PlayerEntity player, EntityRendererManager renderManager, double x, double y, double z, float partialTicks, Vec3d offset, String tex, int color);

    public void doRender(Spellcast spellcast, PlayerEntity player, EntityRendererManager renderManager, double x, double y, double z, float partialTicks, Vec3d offset)
    {
        int color = spellcast.getColor();

        String tex = "minecraft:white";

        if (spellcast.getEffect() instanceof FlameEffect)
        {
            tex = "minecraft:blocks/lava_still";
            color = 0xFFFFFF;
        }
        else if (spellcast.getEffect() instanceof WaterEffect)
        {
            tex = "minecraft:blocks/water_still";
            color = 0xFFFFFF;
        }
        else if (spellcast.getEffect() instanceof WindEffect)
        {
            tex = "elementsofpower:blocks/cone";
            color = 0xFFFFFF;
        }

        doRender(spellcast, player, renderManager, x, y, z, partialTicks, offset, tex, color);
    }
}
