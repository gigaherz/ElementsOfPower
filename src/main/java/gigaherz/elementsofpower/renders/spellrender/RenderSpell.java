package gigaherz.elementsofpower.renders.spellrender;

import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.FlameEffect;
import gigaherz.elementsofpower.spells.effects.WaterEffect;
import gigaherz.elementsofpower.spells.effects.WindEffect;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public abstract class RenderSpell
{
    public abstract void doRender(Spellcast spellcast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset, String tex);

    public void doRender(Spellcast spellcast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset)
    {
        String tex = "minecraft:white";

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

        doRender(spellcast, player, renderManager, x, y, z, partialTicks, offset, tex);
    }
}
