package io.github.dirtyredz.herbalwitch.data.fixture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/** File access shared by the resource tests: the generated and authored trees, JSON and PNG reading. */
public final class ResourceFiles {
    public static final Path GENERATED = Path.of("src/generated/resources");
    public static final Path GENERATED_ASSETS = GENERATED.resolve("assets/herbalwitch");
    public static final Path MAIN_ASSETS = Path.of("src/main/resources/assets/herbalwitch");

    private ResourceFiles() {
    }

    public static JsonObject json(Path path) {
        try {
            return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        } catch (IOException e) {
            throw new AssertionError("cannot read " + path, e);
        }
    }

    public static JsonObject generated(String relative) {
        return json(GENERATED.resolve(relative));
    }

    /** Every generated file, relative and slash-separated, without the datagen cache. */
    public static Set<String> generatedFiles() {
        try (Stream<Path> walk = Files.walk(GENERATED)) {
            return new TreeSet<>(walk.filter(Files::isRegularFile)
                    .map(p -> GENERATED.relativize(p).toString().replace('\\', '/'))
                    .filter(p -> !p.startsWith(".cache/"))
                    .toList());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /** {@code block.herbalwitch.a.b.c} for registry path {@code a/b/c}. */
    public static String langKey(String kind, String path) {
        return kind + ".herbalwitch." + path.replace('/', '.');
    }

    /** A gzip-compressed vanilla structure (NBT), decoded with the game's own reader. */
    public static net.minecraft.nbt.CompoundTag structure(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            return net.minecraft.nbt.NbtIo.readCompressed(in);
        } catch (IOException e) {
            throw new AssertionError("cannot read " + file, e);
        }
    }

    public static int[] pngSize(Path file) {
        try (InputStream in = Files.newInputStream(file); DataInputStream data = new DataInputStream(in)) {
            data.skipBytes(16); // signature, IHDR length and type
            return new int[] {data.readInt(), data.readInt()};
        } catch (IOException e) {
            throw new AssertionError("cannot read " + file, e);
        }
    }

    /** A decoded 8-bit RGBA PNG (as written by tools/art/png.mjs: one IDAT stream, filter 0). */
    public record Png(int width, int height, int[] rgba) {
        public int alpha(int x, int y) {
            return rgba[y * width + x] & 0xFF;
        }

        /** Opaque pixels as a bit set index y * width + x. */
        public Set<Integer> mask() {
            Set<Integer> mask = new TreeSet<>();
            for (int i = 0; i < rgba.length; i++) {
                if ((rgba[i] & 0xFF) != 0) {
                    mask.add(i);
                }
            }
            return mask;
        }
    }

    public static Png readPng(Path file) {
        try {
            byte[] bytes = Files.readAllBytes(file);
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.position(8);
            int width = 0;
            int height = 0;
            ByteArrayOutputStream idat = new ByteArrayOutputStream();
            while (buf.remaining() > 8) {
                int length = buf.getInt();
                String type = new String(bytes, buf.position(), 4, java.nio.charset.StandardCharsets.US_ASCII);
                buf.position(buf.position() + 4);
                if (type.equals("IHDR")) {
                    width = buf.getInt(buf.position());
                    height = buf.getInt(buf.position() + 4);
                    if (bytes[buf.position() + 9] != 6 || bytes[buf.position() + 8] != 8) {
                        throw new AssertionError(file + " is not 8-bit RGBA");
                    }
                } else if (type.equals("IDAT")) {
                    idat.write(bytes, buf.position(), length);
                }
                buf.position(buf.position() + length + 4);
            }
            Inflater inflater = new Inflater();
            inflater.setInput(idat.toByteArray());
            byte[] raw = new byte[height * (1 + width * 4)];
            int read = 0;
            while (read < raw.length && !inflater.finished()) {
                read += inflater.inflate(raw, read, raw.length - read);
            }
            int[] rgba = new int[width * height];
            for (int y = 0; y < height; y++) {
                int row = y * (1 + width * 4);
                if (raw[row] != 0) {
                    throw new AssertionError(file + " uses PNG filter " + raw[row]);
                }
                for (int x = 0; x < width; x++) {
                    int o = row + 1 + x * 4;
                    rgba[y * width + x] = (raw[o] & 0xFF) << 24 | (raw[o + 1] & 0xFF) << 16 | (raw[o + 2] & 0xFF) << 8 | (raw[o + 3] & 0xFF);
                }
            }
            return new Png(width, height, rgba);
        } catch (IOException | DataFormatException e) {
            throw new AssertionError("cannot decode " + file, e);
        }
    }
}
