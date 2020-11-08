package gigaherz.elementsofpower;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigManager
{
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static
    {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class CommonConfig
    {
        public final ForgeConfigSpec.BooleanValue disableAequivaleoSupport;
        public final ForgeConfigSpec.BooleanValue verboseDebug;

        CommonConfig(ForgeConfigSpec.Builder builder)
        {
            builder.push("integration");
            disableAequivaleoSupport = builder.define("disableAequivaleoSupport", false);
            verboseDebug = builder
                    .comment("If enabled, the internal recipe scanner will print per-item information in the logs.")
                    .define("verboseDebug", false);
            builder.pop();
        }
    }

}
