package gigaherz.elementsofpower;

import java.io.File;

public class ConfigManager
{
    public static boolean EnableGemstoneOregen = true;
    public static boolean EnableCocoonGeneration = true;

    public static boolean DisableAequivaleoSupport = true;

    public static void init(File configurationFile)
    {
        /*Configuration config = new Configuration(configurationFile);
        config.load();

        config.setCategoryComment("OreGen", "Settings related to Ore generation");

        EnableGemstoneOregen = config.get("OreGen", "EnableGemstoneOreGeneration", true).getBoolean();
        EnableCocoonGeneration = config.get("OreGen", "EnableCocoonGeneration", true).getBoolean();

        config.save();*/
    }
}
