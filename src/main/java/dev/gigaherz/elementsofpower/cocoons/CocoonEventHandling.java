package dev.gigaherz.elementsofpower.cocoons;


import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.TickEvent;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static dev.gigaherz.elementsofpower.ElementsOfPowerMod.ATTACHMENT_TYPES;

@Mod.EventBusSubscriber(modid=ElementsOfPowerMod.MODID,bus= Mod.EventBusSubscriber.Bus.FORGE)
public class CocoonEventHandling
{
    @SubscribeEvent
    private static void worldTick(TickEvent.LevelTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel)
            get(serverLevel).processCocoons(serverLevel);
    }

    @SubscribeEvent
    private static void playerTick(TickEvent.PlayerTickEvent ev)
    {
        Player player = ev.player;
        if (player.level().isClientSide || !EntitySelector.NO_SPECTATORS.test(player))
            return;

        var combine = player.getCapability(PlayerCombinedMagicContainers.CAPABILITY);
        if (combine != null)
        {
            ServerLevel sw = (ServerLevel) player.level();

            Vec3 p = player.position();
            Vec3 min = p.subtract(8, 8, 8);
            Vec3 max = p.add(9, 9, 9);
            ChunkPos mn = new ChunkPos(BlockPos.containing(min));
            ChunkPos mx = new ChunkPos(BlockPos.containing(max));
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

                        var tracker = c.getData(ElementsOfPowerMod.COCOON_TRACKER);
                        for(var te : tracker.getCocoons()) {
                            BlockPos pos = te.getBlockPos();
                            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < (8.5 * 8.5)
                                    && sw.random.nextInt(100) == 0)
                            {
                                te.transferToPlayer(sw.random, combine);
                            }
                        }
                    }
                }
            }
        }
    }

    private static final String PENDING_TRACKER_NAME = ElementsOfPowerMod.location("pending_tracker").toString();

    public static PendingTracker get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PendingTracker::new, PendingTracker::new), PENDING_TRACKER_NAME);
    }

    public static void track(CocoonTileEntity te)
    {
        if (te.getLevel() instanceof ServerLevel serverLevel)
        {
            get(serverLevel).addCocoon(te);
        }
    }

    public static void untrack(CocoonTileEntity te)
    {
        if (te.getLevel() instanceof ServerLevel serverLevel)
        {
            get(serverLevel).removeCocoon(te);
        }
    }

    public static class PendingTracker extends SavedData
    {
        private final Queue<CocoonTileEntity> pending_additions = Queues.newConcurrentLinkedQueue();
        private final Queue<CocoonTileEntity> pending_removals = Queues.newConcurrentLinkedQueue();

        public PendingTracker()
        {
        }

        public PendingTracker(CompoundTag compoundTag)
        {
        }

        public void addCocoon(CocoonTileEntity te)
        {
            pending_additions.add(te);
        }

        public void removeCocoon(CocoonTileEntity te)
        {
            pending_removals.add(te);
        }

        public void processCocoons(ServerLevel level)
        {
            processQueue(level, pending_additions, CocoonTracker::addCocoon);
            processQueue(level, pending_removals, CocoonTracker::removeCocoon);
        }

        private void processQueue(ServerLevel level, Queue<CocoonTileEntity> queue, BiConsumer<CocoonTracker, CocoonTileEntity> action)
        {
            while (!queue.isEmpty())
            {
                CocoonTileEntity te = queue.remove();
                BlockPos pos = te.getBlockPos();
                ChunkPos cpos = new ChunkPos(pos);
                if (level.hasChunk(cpos.x, cpos.z))
                {
                    ChunkAccess chunk = level.getChunk(cpos.x, cpos.z, ChunkStatus.FULL, false);
                    if (chunk instanceof LevelChunk levelChunk)
                    {
                        var tracker = levelChunk.getData(ElementsOfPowerMod.COCOON_TRACKER);
                        action.accept(tracker, te);
                    }
                }
            }
        }

        @Override
        public CompoundTag save(CompoundTag p_77763_)
        {
            return p_77763_;
        }
    }

    public static class CocoonTracker
    {
        private final List<CocoonTileEntity> cocoons = Lists.newArrayList();

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
