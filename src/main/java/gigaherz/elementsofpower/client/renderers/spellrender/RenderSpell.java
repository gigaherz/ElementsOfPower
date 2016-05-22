package gigaherz.elementsofpower.client.renderers.spellrender;

import com.google.common.collect.Maps;
import gigaherz.elementsofpower.client.renderers.ModelHandle;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.FlameEffect;
import gigaherz.elementsofpower.spells.effects.WaterEffect;
import gigaherz.elementsofpower.spells.effects.WindEffect;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import java.util.Map;


public abstract class RenderSpell
{
    Map<String, ModelHandle> cones = Maps.newHashMap();
    Map<String, ModelHandle> spheres = Maps.newHashMap();
    Map<String, ModelHandle> cylinders = Maps.newHashMap();

    protected IBakedModel getCone(String tex)
    {
        ModelHandle h = cones.get(tex);
        if (h == null)
        {
            h = ModelHandle.of("elementsofpower:entity/cone.obj").replace("#Default", tex);
            cones.put(tex, h);
        }

        return h.get();
    }

    protected IBakedModel getSphere(String tex)
    {
        ModelHandle h = spheres.get(tex);
        if (h == null)
        {
            h = ModelHandle.of("elementsofpower:entity/sphere.obj").replace("#Default", tex);
            spheres.put(tex, h);
        }

        return h.get();
    }

    protected IBakedModel getCylinder(String tex)
    {
        ModelHandle h = cylinders.get(tex);
        if (h == null)
        {
            h = ModelHandle.of("elementsofpower:entity/cylinder.obj").replace("#Default", tex);
            cylinders.put(tex, h);
        }

        return h.get();
    }

    public abstract void doRender(Spellcast spellcast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3d offset, String tex, int color);

    public void doRender(Spellcast spellcast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3d offset)
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
