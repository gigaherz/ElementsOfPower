package gigaherz.elementsofpower;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigManager
{
    public static boolean EnableGemstoneOregen;
    public static boolean EnableCocoonGeneration;

    public static void init(File configurationFile)
    {
        Configuration config = new Configuration(configurationFile);
        config.load();

        config.setCategoryComment("OreGen", "Settings related to Ore generation");

        EnableGemstoneOregen = config.get("OreGen", "EnableGemstoneOreGeneration", true).getBoolean();
        EnableCocoonGeneration = config.get("OreGen", "EnableCocoonGeneration", true).getBoolean();

        config.save();
    }
}
