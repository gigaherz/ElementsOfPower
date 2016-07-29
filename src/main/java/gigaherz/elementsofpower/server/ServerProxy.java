package gigaherz.elementsofpower.server;

import com.google.common.collect.ImmutableMap;
import gigaherz.elementsofpower.common.ISideProxy;
import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;

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
    public void handleAddVelocity(AddVelocityPlayer message)
    {

    }

    @Override
    public void displayBook()
    {

    }

    @Override
    public void beginTracking(EntityPlayer playerIn, EnumHand hand)
    {

    }

    @Override
    public IAnimationStateMachine load(ResourceLocation resourceLocation, ImmutableMap<String, ITimeValue> values)
    {
        return null;
    }
}
