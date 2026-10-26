package io.github.dirtyredz.herbalwitch.client.grimoire.guide;

import guideme.Guide;
import guideme.compiler.TagCompiler;
import guideme.ui.GuideUiHost;
import io.github.dirtyredz.herbalwitch.client.grimoire.guide.animation.WatchLessonTag;
import io.github.dirtyredz.herbalwitch.client.grimoire.guide.content.LiveRecipesTag;
import io.github.dirtyredz.herbalwitch.client.grimoire.guide.content.CraftingRecipesTag;
import io.github.dirtyredz.herbalwitch.client.grimoire.guide.content.GuideRecipeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;

/** Public, client-only documentation. No book access, discovery or gameplay mutations. */
public final class GuideClient {
    private static Guide guide;

    private GuideClient() { }

    public static void setup() {
        guide = Guide.builder(new ResourceLocation("herbalwitch", "witch_path"))
                .folder("guide").defaultNamespace("herbalwitch").defaultLanguage("en_us")
                .startPage(new ResourceLocation("herbalwitch", "path/start.md"))
                .extension(TagCompiler.EXTENSION_POINT, new LiveRecipesTag())
                .extension(TagCompiler.EXTENSION_POINT, new CraftingRecipesTag())
                .extension(TagCompiler.EXTENSION_POINT, new WatchLessonTag()).build();
        MinecraftForge.EVENT_BUS.addListener(GuideRecipeSlot::beforeScreen);
        MinecraftForge.EVENT_BUS.addListener(GuideRecipeSlot::afterScreen);
        MinecraftForge.EVENT_BUS.addListener(GuideClient::recipesUpdated);
        MinecraftForge.EVENT_BUS.addListener(GuideClient::tagsUpdated);
    }

    private static void recipesUpdated(RecipesUpdatedEvent event) { refresh(); }

    private static void tagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) refresh();
    }

    private static void refresh() {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().screen instanceof GuideUiHost host && host.getGuide() == guide) {
                host.reloadPage();
            }
        });
    }
}
