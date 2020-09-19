package gigaherz.elementsofpower.cocoons;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class CocoonPlacement extends Placement<NoPlacementConfig>
{
    public static final CocoonPlacement INSTANCE = new CocoonPlacement(NoPlacementConfig.field_236555_a_);

    public CocoonPlacement(Codec<NoPlacementConfig> configFactoryIn)
    {
        super(configFactoryIn);
    }

    @Override
    public Stream<BlockPos> func_241857_a(WorldDecoratingHelper helper, Random random, NoPlacementConfig config, BlockPos pos)
    {
        List<BlockPos> positions = new ArrayList<>();

        int top = 0;
        for(int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                top = Math.max(top, helper.func_242893_a(Heightmap.Type.MOTION_BLOCKING, x+pos.getX(), z+pos.getZ()));
            }
        }

        int sections = MathHelper.ceil(top / 16.0f) * 16;
        for (int y = 0; y < sections; y += 16)
        {
            int n = random.nextInt(3);
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