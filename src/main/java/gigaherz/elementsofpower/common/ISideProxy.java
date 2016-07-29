package gigaherz.elementsofpower.common;

import com.google.common.collect.ImmutableMap;
import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;

public interface ISideProxy
{
    void preInit();

    void init();

    void handleSpellcastSync(SpellcastSync message);

    void handleRemainingAmountsUpdate(EssentializerAmountsUpdate message);

    void handleEssentializerTileUpdate(EssentializerTileUpdate message);

    void handleAddVelocity(AddVelocityPlayer message);

    void displayBook();

    void beginTracking(EntityPlayer playerIn, EnumHand hand);

    IAnimationStateMachine load(ResourceLocation resourceLocation, ImmutableMap<String, ITimeValue> values);
}
