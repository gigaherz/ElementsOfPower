package gigaherz.elementsofpower.server;

import gigaherz.elementsofpower.ISideProxy;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;

public class ServerProxy implements ISideProxy
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
    public void displayBook()
    {

    }
}
