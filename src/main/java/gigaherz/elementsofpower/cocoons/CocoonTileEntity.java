package gigaherz.elementsofpower.cocoons;

import gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.essentializer.gui.IMagicAmountContainer;
import gigaherz.elementsofpower.items.MagicOrbItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;
import java.util.Random;

public class CocoonTileEntity extends TileEntity implements ITickableTileEntity, IMagicAmountContainer
{
    @ObjectHolder("elementsofpower:cocoon")
    public static TileEntityType<CocoonTileEntity> TYPE;

    public MagicAmounts essenceContained = MagicAmounts.EMPTY;

    public CocoonTileEntity(TileEntityType<?> type)
    {
        super(type);
    }

    public CocoonTileEntity()
    {
        super(TYPE);
    }

    @Override
    public void read(CompoundNBT compound)
    {
        super.read(compound);

        essenceContained = new MagicAmounts(compound.getCompound("Magic"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound = super.write(compound);

        compound.put("Magic", essenceContained.serializeNBT());

        return compound;
    }

    private static final int SPAWN_COOLDOWN = 100;
    private static final int SPAWN_COOLDOWN_RANDOM = 100;
    private int spawnLivingEssenceCooldown = SPAWN_COOLDOWN + (this.hashCode() % SPAWN_COOLDOWN_RANDOM);

    @Override
    public void tick()
    {
        if (world == null || world.isRemote)
            return;

        if (spawnLivingEssenceCooldown > 0)
        {
            spawnLivingEssenceCooldown--;
            return;
        }

        if (!essenceContained.isEmpty())
        {
            Random random = ((ServerWorld) world).rand;

            List<PlayerEntity> players = world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos).expand(8, 8, 8),
                    player -> player.getCapability(PlayerCombinedMagicContainers.CAPABILITY).isPresent());

            if (players.size() > 0)
            {
                MagicAmounts am = essenceContained;
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    am = am.with(i, (float) Math.floor(essenceContained.get(i) * random.nextFloat()));
                }

                if (!am.isEmpty())
                {
                    MagicAmounts[] refRemaining = new MagicAmounts[]{am};
                    for (PlayerEntity e : players)
                    {
                        if (e.getCapability(PlayerCombinedMagicContainers.CAPABILITY).map(magic -> {
                            refRemaining[0] = magic.addMagic(refRemaining[0]);
                            return refRemaining[0].isEmpty();
                        }).orElse(false))
                            break;
                    }
                }
            }

            spawnLivingEssenceCooldown = SPAWN_COOLDOWN + random.nextInt(SPAWN_COOLDOWN_RANDOM);
        }
    }

    public void addEssences(ItemStack stack)
    {
        essenceContained = essenceContained.add(((MagicOrbItem) stack.getItem()).getElement(), 1);

        BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag)
    {
        read(tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
    {
        super.onDataPacket(net, packet);
        handleUpdateTag(packet.getNbtCompound());
    }

    @Override
    public MagicAmounts getContainedMagic()
    {
        return essenceContained;
    }
}
