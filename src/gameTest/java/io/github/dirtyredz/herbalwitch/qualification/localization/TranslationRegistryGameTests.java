package io.github.dirtyredz.herbalwitch.qualification.localization;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Localisation completeness for everything the registries name (Milestone 12): every Herbal Witch
 * block, item, entity type, mob effect and creative tab has an English name in the shipped language
 * file, so no player ever sees a raw key. The source-spelled keys are covered by TranslationCoverageTest.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TranslationRegistryGameTests {
    private TranslationRegistryGameTests() {
    }

    @GameTest(template = "empty", batch = "herbalwitch_translation_registry")
    public static void everyRegisteredThingHasAnEnglishName(GameTestHelper helper) {
        JsonObject lang = english();
        List<String> missing = new ArrayList<>();
        BuiltInRegistries.BLOCK.entrySet().forEach(e -> need(lang, e.getKey().location(), e.getValue().getDescriptionId(), missing));
        BuiltInRegistries.ITEM.entrySet().forEach(e -> need(lang, e.getKey().location(), e.getValue().getDescriptionId(), missing));
        BuiltInRegistries.ENTITY_TYPE.entrySet().forEach(e -> need(lang, e.getKey().location(), e.getValue().getDescriptionId(), missing));
        BuiltInRegistries.MOB_EFFECT.entrySet().forEach(e -> need(lang, e.getKey().location(), e.getValue().getDescriptionId(), missing));
        helper.getLevel().registryAccess().registryOrThrow(Registries.CREATIVE_MODE_TAB).entrySet().forEach(e -> {
            if (e.getValue().getDisplayName().getContents() instanceof TranslatableContents title) need(lang, e.getKey().location(), title.getKey(), missing);
            else if (e.getKey().location().getNamespace().equals(HerbalWitch.MOD_ID)) missing.add(e.getKey().location() + " has an untranslated title");
        });
        if (!missing.isEmpty()) helper.fail("no English name for " + missing.size() + ": " + missing);
        helper.succeed();
    }

    private static void need(JsonObject lang, ResourceLocation id, String key, List<String> missing) {
        if (id.getNamespace().equals(HerbalWitch.MOD_ID) && !lang.has(key)) missing.add(id + " -> " + key);
    }

    private static JsonObject english() {
        try (var in = HerbalWitch.class.getResourceAsStream("/assets/herbalwitch/lang/en_us.json")) {
            if (in == null) throw new IllegalStateException("en_us.json is not on the server classpath");
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
