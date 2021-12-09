package dev.gigaherz.elementsofpower.cocoons;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;

public class CocoonEventHandling
{
    public static void enable(RegisterCapabilitiesEvent event)
    {
        CocoonTracker.registerCap(event);
        PendingTracker.registerCap(event);
        MinecraftForge.EVENT_BUS.addListener(CocoonEventHandling::playerTick);
    }

    private static void playerTick(TickEvent.PlayerTickEvent ev)
    {
        Player player = ev.player;
        if (player.level.isClientSide || !EntitySelector.NO_SPECTATORS.test(player))
            return;

        player.getCapability(PlayerCombinedMagicContainers.CAPABILITY).ifPresent(combine -> {
            ServerLevel sw = (ServerLevel) player.level;

            Vec3 p = player.position();
            Vec3 min = p.subtract(8, 8, 8);
            Vec3 max = p.add(9, 9, 9);
            ChunkPos mn = new ChunkPos(new BlockPos(min));
            ChunkPos mx = new ChunkPos(new BlockPos(max));
            int minX = mn.x;
            int minZ = mn.z;
            int maxX = mx.x;
            int maxZ = mx.z;
            for (int z = minZ; z <= maxZ; z++)
            {
                for (int x = minX; x <= maxX; x++)
                {
                    if (sw.hasChunk(x, z))
                    {
                        LevelChunk c = sw.getChunk(x, z);
                        c.getCapability(COCOON_TRACKER).ifPresent(tracker -> {
                            tracker.getCocoons().forEach(te -> {
                                BlockPos pos = te.getBlockPos();
                                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < (8.5 * 8.5)
                                        && sw.random.nextInt(100) == 0)
                                {
                                    te.transferToPlayer(sw.random, combine);
                                }
                            });
                        });
                    }
                }
            }
        });
    }

    public static void track(CocoonTileEntity te)
    {
        Level world = te.getLevel();
        if (world == null || world.isClientSide)
            return;
        world.getCapability(PENDING_TRACKER).ifPresent(tracker -> {
            tracker.addCocoon(te);
        });
    }

    public static void untrack(CocoonTileEntity te)
    {
        Level world = te.getLevel();
        if (world == null || world.isClientSide)
            return;
        world.getCapability(PENDING_TRACKER).ifPresent(tracker -> {
            tracker.removeCocoon(te);
        });
    }

    public static Capability<PendingTracker> PENDING_TRACKER = CapabilityManager.get(new CapabilityToken<>() {});

    public static class PendingTracker
    {
        private final Level world;
        private final Queue<CocoonTileEntity> pending_additions = Queues.newConcurrentLinkedQueue();
        private final Queue<CocoonTileEntity> pending_removals = Queues.newConcurrentLinkedQueue();

        public PendingTracker(Level world)
        {
            this.world = world;
        }

        public static void registerCap(RegisterCapabilitiesEvent event)
        {
            event.register(PendingTracker.class);
            MinecraftForge.EVENT_BUS.addListener(PendingTracker::worldTick);
            MinecraftForge.EVENT_BUS.addGenericListener(Level.class, PendingTracker::attachCapabilities);
        }

        private static void attachCapabilities(AttachCapabilitiesEvent<Level> event)
        {
            Level w = event.getObject();
            if (w.isClientSide)
                return;
            event.addCapability(ElementsOfPowerMod.location("pending_cocoons"), new ICapabilityProvider()
            {
                private final LazyOptional<PendingTracker> lo = LazyOptional.of(() -> new PendingTracker(w));

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
                {
                    if (cap == PENDING_TRACKER)
                        return lo.cast();
                    return LazyOptional.empty();
                }
            });
        }

        private static void worldTick(TickEvent.WorldTickEvent event)
        {
            if (event.phase == TickEvent.Phase.END)
                event.world.getCapability(PENDING_TRACKER).ifPresent(PendingTracker::processCocoons);
        }

        public void addCocoon(CocoonTileEntity te)
        {
            pending_additions.add(te);
        }

        public void removeCocoon(CocoonTileEntity te)
        {
            pending_removals.add(te);
        }

        public void processCocoons()
        {
            processQueue(pending_additions, CocoonTracker::addCocoon);
            processQueue(pending_removals, CocoonTracker::removeCocoon);
        }

        private void processQueue(Queue<CocoonTileEntity> queue, BiConsumer<CocoonTracker, CocoonTileEntity> action)
        {
            while (!queue.isEmpty())
            {
                CocoonTileEntity te = queue.remove();
                BlockPos pos = te.getBlockPos();
                ChunkPos cpos = new ChunkPos(pos);
                if (world.hasChunk(cpos.x, cpos.z))
                {
                    ChunkAccess chunk = world.getChunk(cpos.x, cpos.z, ChunkStatus.FULL, false);
                    if (chunk instanceof LevelChunk)
                    {
                        ((LevelChunk) chunk).getCapability(COCOON_TRACKER).ifPresent(tracker -> {
                            action.accept(tracker, te);
                        });
                    }
                }
            }
        }
    }

    public static Capability<CocoonTracker> COCOON_TRACKER = CapabilityManager.get(new CapabilityToken<>() {});

    public static class CocoonTracker
    {
        private final List<CocoonTileEntity> cocoons = Lists.newArrayList();

        public static void registerCap(RegisterCapabilitiesEvent event)
        {
            event.register(CocoonTracker.class);
            MinecraftForge.EVENT_BUS.addGenericListener(LevelChunk.class, CocoonTracker::attachCapabilities);
        }

        private static void attachCapabilities(AttachCapabilitiesEvent<LevelChunk> event)
        {
            LevelChunk c = event.getObject();
            if (c.getLevel().isClientSide)
                return;
            event.addCapability(ElementsOfPowerMod.location("cocoon_tracker"), new ICapabilityProvider()
            {
                private final LazyOptional<CocoonTracker> lo = LazyOptional.of(CocoonTracker::new);

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
                {
                    if (cap == COCOON_TRACKER)
                        return lo.cast();
                    return LazyOptional.empty();
                }
            });
        }


        public void addCocoon(CocoonTileEntity te)
        {
            cocoons.add(te);
        }

        public void removeCocoon(CocoonTileEntity te)
        {
            cocoons.remove(te);
        }

        public List<CocoonTileEntity> getCocoons()
        {
            return Collections.unmodifiableList(cocoons);
        }
    }
}
