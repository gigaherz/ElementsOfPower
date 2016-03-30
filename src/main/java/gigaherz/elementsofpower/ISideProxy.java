package gigaherz.elementsofpower;

import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public interface ISideProxy
{
    void preInit();

    void init();

    void handleSpellcastSync(SpellcastSync message);

    void handleRemainingAmountsUpdate(EssentializerAmountsUpdate message);

    void handleEssentializerTileUpdate(EssentializerTileUpdate message);

    void displayBook();

    void beginTracking(EntityPlayer playerIn, EnumHand hand);
}
