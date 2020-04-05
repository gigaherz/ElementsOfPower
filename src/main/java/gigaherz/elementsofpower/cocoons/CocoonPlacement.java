package gigaherz.elementsofpower.cocoons;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

public class CocoonPlacement extends Placement<NoPlacementConfig>
{
    public static final CocoonPlacement INSTANCE = new CocoonPlacement(NoPlacementConfig::deserialize);

    public CocoonPlacement(Function<Dynamic<?>, ? extends NoPlacementConfig> configFactoryIn)
    {
        super(configFactoryIn);
    }

    @Override
    public Stream<BlockPos> getPositions(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generatorIn, Random random, NoPlacementConfig configIn, BlockPos pos)
    {
        List<BlockPos> positions = new ArrayList<>();
        int topFilledSegment = worldIn.getChunk(pos).getTopFilledSegment();
        for (int y = 0; y < topFilledSegment; y += 16)
        {
            int n = random.nextInt(2) + 4;
            for (int i = 0; i < n; i++)
            {
                int px = random.nextInt(16) + pos.getX();
                int pz = random.nextInt(16) + pos.getZ();
                int py = random.nextInt(16) + y;
                positions.add(new BlockPos(px, py, pz));
            }
        }
        return positions.stream();
    }
}