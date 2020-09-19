package gigaherz.elementsofpower.cocoons;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
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
    public static void enable()
    {
        MinecraftForge.EVENT_BUS.addListener(CocoonEventHandling::playerTick);
        CocoonTracker.registerCap();
        PendingTracker.registerCap();
    }

    private static void playerTick(TickEvent.PlayerTickEvent ev)
    {
        PlayerEntity player = ev.player;
        if (player.world.isRemote || !EntityPredicates.NOT_SPECTATING.test(player))
            return;

        player.getCapability(PlayerCombinedMagicContainers.CAPABILITY).ifPresent(combine -> {
            ServerWorld sw = (ServerWorld) player.world;

            Vector3d p = player.getPositionVec();
            Vector3d min = p.subtract(8,8,8);
            Vector3d max = p.add(9,9,9);
            ChunkPos mn = new ChunkPos(new BlockPos(min));
            ChunkPos mx = new ChunkPos(new BlockPos(max));
            int minX = mn.x;
            int minZ = mn.z;
            int maxX = mx.x;
            int maxZ = mx.z;
            for(int z = minZ; z <= maxZ; z++)
            {
                for(int x = minX; x <= maxX; x++)
                {
                    if (sw.chunkExists(x,z))
                    {
                        Chunk c = sw.getChunk(x, z);
                        c.getCapability(COCOON_TRACKER).ifPresent(tracker -> {
                            tracker.getCocoons().forEach(te -> {
                                BlockPos pos = te.getPos();
                                if (player.getDistanceSq(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5) < (8.5 * 8.5)
                                        && sw.rand.nextInt(100) == 0)
                                {
                                    te.transferToPlayer(sw.rand, combine);
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
        World world = te.getWorld();
            if (world == null || world.isRemote)
            return;
        world.getCapability(PENDING_TRACKER).ifPresent(tracker -> {
            tracker.addCocoon(te);
        });
    }

    public static void untrack(CocoonTileEntity te)
    {
        World world = te.getWorld();
        if (world == null || world.isRemote)
            return;
        world.getCapability(PENDING_TRACKER).ifPresent(tracker -> {
            tracker.removeCocoon(te);
        });
    }

    @CapabilityInject(PendingTracker.class)
    public static Capability<PendingTracker> PENDING_TRACKER = null;

    public static class PendingTracker
    {
        private final World world;
        private final Queue<CocoonTileEntity> pending_additions = Queues.newConcurrentLinkedQueue();
        private final Queue<CocoonTileEntity> pending_removals = Queues.newConcurrentLinkedQueue();

        public PendingTracker(World world)
        {
            this.world = world;
        }

        public static void registerCap()
        {
            MinecraftForge.EVENT_BUS.addListener(PendingTracker::worldTick);
            MinecraftForge.EVENT_BUS.addGenericListener(World.class, PendingTracker::attachCapabilities);
            CapabilityManager.INSTANCE.register(PendingTracker.class, new Capability.IStorage<PendingTracker>()
            {
                @Nullable
                @Override
                public INBT writeNBT(Capability<PendingTracker> capability, PendingTracker instance, Direction side)
                {
                    throw new IllegalStateException("This capability is not serializable");
                }

                @Override
                public void readNBT(Capability<PendingTracker> capability, PendingTracker instance, Direction side, INBT nbt)
                {
                    throw new IllegalStateException("This capability is not serializable");
                }
            }, () -> {
                throw new IllegalStateException("This capability is not instantiable");
            });
        }

        private static void attachCapabilities(AttachCapabilitiesEvent<World> event)
        {
            World w = event.getObject();
            if (w.isRemote)
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
            while(!queue.isEmpty())
            {
                CocoonTileEntity te = queue.remove();
                BlockPos pos = te.getPos();
                ChunkPos cpos = new ChunkPos(pos);
                if (world.chunkExists(cpos.x, cpos.z))
                {
                    IChunk chunk = world.getChunk(cpos.x, cpos.z, ChunkStatus.FULL, false);
                    if (chunk instanceof Chunk)
                    {
                        ((Chunk) chunk).getCapability(COCOON_TRACKER).ifPresent(tracker -> {
                            action.accept(tracker, te);
                        });
                    }
                }
            }
        }
    }

    @CapabilityInject(CocoonTracker.class)
    public static Capability<CocoonTracker> COCOON_TRACKER = null;

    public static class CocoonTracker
    {
        private final List<CocoonTileEntity> cocoons = Lists.newArrayList();

        public static void registerCap()
        {
            MinecraftForge.EVENT_BUS.addGenericListener(Chunk.class, CocoonTracker::attachCapabilities);
            CapabilityManager.INSTANCE.register(CocoonTracker.class, new Capability.IStorage<CocoonTracker>()
            {
                @Nullable
                @Override
                public INBT writeNBT(Capability<CocoonTracker> capability, CocoonTracker instance, Direction side)
                {
                    throw new IllegalStateException("This capability is not serializable");
                }

                @Override
                public void readNBT(Capability<CocoonTracker> capability, CocoonTracker instance, Direction side, INBT nbt)
                {
                    throw new IllegalStateException("This capability is not serializable");
                }
            }, () -> {
                throw new IllegalStateException("This capability is not instantiable");
            });
        }

        private static void attachCapabilities(AttachCapabilitiesEvent<Chunk> event)
        {
            Chunk c = event.getObject();
            if (c.getWorld().isRemote)
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
