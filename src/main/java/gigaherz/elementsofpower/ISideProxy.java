package gigaherz.elementsofpower;

import gigaherz.elementsofpower.network.SetSpecialSlot;
import gigaherz.elementsofpower.network.SpellcastSync;

public interface ISideProxy
{
    void preInit();

    void init();

    void handleSpellcastSync(SpellcastSync message);

    void handleSetSpecialSlot(SetSpecialSlot message);
}
