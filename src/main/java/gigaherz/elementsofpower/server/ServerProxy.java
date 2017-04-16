package gigaherz.elementsofpower.server;

import gigaherz.elementsofpower.common.IModProxy;
import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public class ServerProxy implements IModProxy
{
    public void preInit()
    {
        // Nothing here
    }

    public void init()
    {
        // Nothing here
    }

    @Override
    public void handleSpellcastSync(SpellcastSync message)
    {
        // Nothing here
    }

    @Override
    public void handleRemainingAmountsUpdate(EssentializerAmountsUpdate message)
    {
        // Nothing here
    }

    @Override
    public void handleEssentializerTileUpdate(EssentializerTileUpdate message)
    {
        // Nothing here
    }

    @Override
    public void handleAddVelocity(AddVelocityPlayer message)
    {

    }

    @Override
    public void beginTracking(EntityPlayer playerIn, EnumHand hand)
    {

    }
}
