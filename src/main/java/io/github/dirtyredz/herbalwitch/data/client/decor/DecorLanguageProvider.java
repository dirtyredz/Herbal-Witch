package io.github.dirtyredz.herbalwitch.data.client.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorCreativeTab;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/** English names: "<Finish> <Piece>" for every furniture block, and the decor tab. */
public class DecorLanguageProvider extends LanguageProvider {
    public DecorLanguageProvider(PackOutput output) {
        super(output, HerbalWitch.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(DecorCreativeTab.TITLE_KEY, "The Herbal Witch: Cottage Decor");
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            add(entry.block().get(), entry.spec().displayName(entry.finish()));
        }
    }
}
