package gigaherz.elementsofpower;

import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;

public interface ISideProxy
{
    void preInit();

    void init();

    void handleSpellcastSync(SpellcastSync message);

    void handleRemainingAmountsUpdate(EssentializerAmountsUpdate message);
}
