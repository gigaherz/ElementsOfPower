package gigaherz.elementsofpower.renders.spellrender;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public abstract class RenderSpell
{
    public abstract void doRender(Spellcast spellcast, EntityPlayer player, RenderManager renderManager, double x, double y, double z, float partialTicks, Vec3 offset);
}
