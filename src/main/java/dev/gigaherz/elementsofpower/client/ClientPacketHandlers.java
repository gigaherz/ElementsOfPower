package dev.gigaherz.elementsofpower.client;

import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerMenu;
import dev.gigaherz.elementsofpower.network.*;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public class ClientPacketHandlers
{
    public static void handleSpellcastSync(SynchronizeSpellcastState message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            Player player = (Player) mc.level.getEntity(message.casterID());
            Spellcast spellcast = message.spellcast().isEmpty() ? null : Spellcast.read(message.spellcast());
            SpellcastState.get(player)
                    .onSync(message.changeMode(), spellcast, message.remainingCastTime(), message.remainingInterval(), message.totalCastTime());
        });
    }

    public static void handleRemainingAmountsUpdate(UpdateEssentializerAmounts message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            Player player = mc.player;
            if (message.windowId() != -1)
            {
                if (message.windowId() == player.containerMenu.containerId)
                {
                    if ((player.containerMenu instanceof EssentializerMenu))
                    {
                        ((EssentializerMenu) player.containerMenu).updateAmounts(message.contained(), message.remaining());
                    }
                }
            }
        });
    }

    public static void handleEssentializerTileUpdate(UpdateEssentializerTile message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() ->
        {
            if (mc.level == null)
                return;
            if (mc.level.getBlockEntity(message.pos()) instanceof EssentializerBlockEntity essentializer)
            {
                essentializer.getInventory().setStackInSlot(0, message.activeItem());
                essentializer.remainingToConvert = message.remaining();
            }
        });
    }

    public static void handleAddVelocityPlayer(AddVelocityToPlayer message)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player == null)
                return;
            mc.player.push(message.vx(), message.vy(), message.vz());
        });
    }

    public static void handleParticlesInShape(ParticlesInShape packet)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            var random = mc.level.random;
            switch (Objects.requireNonNull(packet.areaShape()))
            {
                case BOX ->
                {
                    for (int i = 0; i < packet.count(); i++)
                    {
                        var options = packet.options();
                        double posX = packet.centerX() + (random.nextDouble()-0.5f) * 2.0f * packet.spreadX();
                        double posY = packet.centerY() + (random.nextDouble()-0.5f) * 2.0f * packet.spreadY();
                        double posZ = packet.centerZ() + (random.nextDouble()-0.5f) * 2.0f * packet.spreadZ();
                        double velX = Mth.lerp(packet.minVelocityX(), packet.maxVelocityX(), random.nextDouble());
                        double velY = Mth.lerp(packet.minVelocityY(), packet.maxVelocityY(), random.nextDouble());
                        double velZ = Mth.lerp(packet.minVelocityZ(), packet.maxVelocityZ(), random.nextDouble());
                        mc.level.addParticle(options, posX, posY, posZ, velX, velY, velZ);
                    }
                }
                case BOX_UNIFORM ->
                {
                    for (int i = 0; i < packet.count(); i++)
                    {
                        var options = packet.options();
                        double posX = packet.centerX() + signedSqrt((random.nextDouble()-0.5f) * 2.0f) * packet.spreadX();
                        double posY = packet.centerY() + signedSqrt((random.nextDouble()-0.5f) * 2.0f) * packet.spreadY();
                        double posZ = packet.centerZ() + signedSqrt((random.nextDouble()-0.5f) * 2.0f) * packet.spreadZ();
                        double velX = Mth.lerp(packet.minVelocityX(), packet.maxVelocityX(), random.nextDouble());
                        double velY = Mth.lerp(packet.minVelocityY(), packet.maxVelocityY(), random.nextDouble());
                        double velZ = Mth.lerp(packet.minVelocityZ(), packet.maxVelocityZ(), random.nextDouble());
                        mc.level.addParticle(options, posX, posY, posZ, velX, velY, velZ);
                    }
                }
            }
        });
    }

    private static double signedSqrt(double v)
    {
        return Math.signum(v) * Math.sqrt(Math.abs(v));
    }
}
