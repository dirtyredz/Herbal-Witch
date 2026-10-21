package io.github.dirtyredz.herbalwitch.data.client.language;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.data.client.botany.BotanyTranslations;
import io.github.dirtyredz.herbalwitch.data.client.decor.DecorTranslations;
import io.github.dirtyredz.herbalwitch.data.client.preparations.PreparationTranslations;
import io.github.dirtyredz.herbalwitch.data.client.showcase.ShowcaseTranslations;
import io.github.dirtyredz.herbalwitch.data.client.stations.StationTranslations;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * The only writer of {@code lang/en_us.json}. Two language providers for one locale would overwrite
 * each other's file, so every feature contributes its keys here; a duplicate key fails datagen.
 */
public class HerbalWitchLanguageProvider extends LanguageProvider {
    public HerbalWitchLanguageProvider(PackOutput output) {
        super(output, HerbalWitch.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        DecorTranslations.addTo(this);
        BotanyTranslations.addTo(this);
        StationTranslations.addTo(this);
        PreparationTranslations.addTo(this);
        ShowcaseTranslations.addTo(this);
    }
}
