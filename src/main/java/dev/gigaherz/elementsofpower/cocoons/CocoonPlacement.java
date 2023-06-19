package dev.gigaherz.elementsofpower.cocoons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CocoonPlacement extends PlacementModifier
{
    public static final int CONFIG_VERSION=1;
    public static final CocoonPlacement INSTANCE = new CocoonPlacement();
    public static final Codec<CocoonPlacement> CODEC = Codec.INT.comapFlatMap(
            i -> i == CONFIG_VERSION ? DataResult.success(INSTANCE) : DataResult.error("Uknown CocoonPlacement version " + i),
            c -> CONFIG_VERSION);
    public static final PlacementModifierType<CocoonPlacement> TYPE = register("elementsofpower:cocoon_placement", CODEC);

    private static <P extends PlacementModifier> PlacementModifierType<P> register(String name, Codec<P> codec) {
        return Registry.register(Registry.PLACEMENT_MODIFIERS, name, () -> codec);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos)
    {
        List<BlockPos> positions = new ArrayList<>();

        int top = 0;
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                top = Math.max(top, context.getHeight(Heightmap.Types.MOTION_BLOCKING, x + pos.getX(), z + pos.getZ()));
            }
        }

        int bottomSection = context.getMinBuildHeight();
        int topSection = Mth.ceil(top / 16.0f) * 16;
        for (int y = bottomSection; y < topSection; y += 16)
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

    @Override
    public PlacementModifierType<?> type()
    {
        return TYPE;
    }
}