package dev.gigaherz.elementsofpower.misc;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class TextureVariantsGen implements DataProvider
{
    private final PackOutput.PathProvider pathProvider;
    private final ExistingFileHelper existingFileHelper;
    private final String modid;

    public TextureVariantsGen(PackOutput packOutput, ExistingFileHelper existingFileHelper, String modid)
    {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures");
        this.existingFileHelper = existingFileHelper;
        this.modid = modid;
    }

    protected abstract void genTextures(BiConsumer<ResourceLocation, Supplier<NativeImage>> consumer);

    protected void genPaletteSwap(BiConsumer<ResourceLocation, Supplier<NativeImage>> consumer, String name, ResourceLocation inputFile, ResourceLocation referencePaletteFile, ResourceLocation targetPaletteFile)
    {
        genTexture(consumer, name, () -> {
            NativeImage inputTexture = loadTexture(inputFile);
            NativeImage referencePaletteTexture = loadTexture(referencePaletteFile);
            NativeImage targetPaletteTexture = loadTexture(targetPaletteFile);

            Hsla[] referencePalette = extractPalette(referencePaletteTexture);
            Hsla[] targetPalette = extractPalette(targetPaletteTexture);

            int targetMax = targetPalette.length - 1;
            int refMax = referencePalette.length - 1;

            return inputTexture.mappedCopy(color -> {
                var original = Hsla.fromRgb(color);

                if (original.a() == 0)
                {
                    return color;
                }

                // find closest value
                Hsla closest = null;
                int nClosest = -1;
                int vClosest = Integer.MAX_VALUE;
                for (int j = 0; j < referencePalette.length; j++)
                {
                    var candidate = referencePalette[j];

                    var vDistance =  Math.abs(original.l() - candidate.l());
                    if (closest == null || vDistance < vClosest)
                    {
                        closest = candidate;
                        nClosest = j;
                        vClosest = vDistance;
                    }
                }

                return targetPalette[nClosest * targetMax / refMax].alpha(original.a()).toRgb();
            });
        });
    }

    protected Hsla[] extractPalette(NativeImage inputTexture)
    {
        return Arrays.stream(inputTexture.getPixelsRGBA()).mapToObj(Hsla::fromRgb).filter(c -> c.a() > 0).distinct().sorted(Comparator.comparingInt(Hsla::a).thenComparingInt(Hsla::l)).toArray(Hsla[]::new);
    }

    protected NativeImage loadTexture(ResourceLocation inputFile)
    {
        try (var stream = existingFileHelper.getResource(inputFile, PackType.CLIENT_RESOURCES, ".png", "textures").open())
        {
            return NativeImage.read(stream);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    protected void genTexture(BiConsumer<ResourceLocation, Supplier<NativeImage>> consumer, String name, Supplier<NativeImage> supplier)
    {
        consumer.accept(new ResourceLocation(modid, name), supplier);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput)
    {
        Map<ResourceLocation, Supplier<NativeImage>> entries = new HashMap<>();

        genTextures(entries::put);

        return CompletableFuture.allOf(entries.entrySet().stream().map((entry) -> {
            var key = entry.getKey();
            var value = entry.getValue();
            Path path = this.pathProvider.file(key, "png");
            return saveOne(pOutput, value, path);
        }).toArray(CompletableFuture[]::new));
    }

    static CompletableFuture<?> saveOne(CachedOutput pOutput, Supplier<NativeImage> imageSupplier, Path pPath)
    {
        return CompletableFuture.runAsync(() -> {
            try (var image = imageSupplier.get())
            {
                var bytes = image.asByteArray();
                var hash = Hashing.sha1().hashBytes(bytes);
                pOutput.writeIfNeeded(pPath, bytes, hash);
            }
            catch (IOException ioexception)
            {
                LOGGER.error("Failed to save file to {}", pPath, ioexception);
            }
        }, Util.backgroundExecutor());
    }

    @Override
    public String getName()
    {
        return "Elements of Power texture variants data provider";
    }
}
