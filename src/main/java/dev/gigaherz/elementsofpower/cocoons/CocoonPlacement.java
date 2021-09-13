package dev.gigaherz.elementsofpower.cocoons;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class CocoonPlacement extends FeatureDecorator<NoneDecoratorConfiguration>
{
    public static final CocoonPlacement INSTANCE = new CocoonPlacement(NoneDecoratorConfiguration.CODEC);

    public CocoonPlacement(Codec<NoneDecoratorConfiguration> configFactoryIn)
    {
        super(configFactoryIn);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext helper, Random random, NoneDecoratorConfiguration config, BlockPos pos)
    {
        List<BlockPos> positions = new ArrayList<>();

        int top = 0;
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                top = Math.max(top, helper.getHeight(Heightmap.Types.MOTION_BLOCKING, x + pos.getX(), z + pos.getZ()));
            }
        }

        int sections = Mth.ceil(top / 16.0f) * 16;
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