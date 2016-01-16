package gigaherz.elementsofpower.renders.spellrender;

import gigaherz.elementsofpower.spells.cast.Spellcast;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public abstract class RenderSpell<T extends Spellcast>
{
    public abstract void doRender(T spellcast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset);
}
