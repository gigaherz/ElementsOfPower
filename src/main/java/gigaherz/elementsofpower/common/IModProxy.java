package gigaherz.elementsofpower.common;

import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public interface IModProxy
{
    default void init() {}

    void handleSpellcastSync(SpellcastSync message);

    void handleRemainingAmountsUpdate(EssentializerAmountsUpdate message);

    void handleEssentializerTileUpdate(EssentializerTileUpdate message);

    void handleAddVelocity(AddVelocityPlayer message);

    void beginTracking(EntityPlayer playerIn, EnumHand hand);
}
