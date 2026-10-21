package io.github.dirtyredz.herbalwitch.data.client.decor;

import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorCreativeTab;
import net.minecraftforge.common.data.LanguageProvider;

/** English names: "<Finish> <Piece>" for every furniture block, and the decor tab. */
public final class DecorTranslations {
    private DecorTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        lang.add(DecorCreativeTab.TITLE_KEY, "The Herbal Witch: Cottage Decor");
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            lang.add(entry.block().get(), entry.spec().displayName(entry.finish()));
        }
    }
}
