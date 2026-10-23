package io.github.dirtyredz.herbalwitch.data.client.decor;

import io.github.dirtyredz.herbalwitch.decor.behavior.seating.SeatingService;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorCreativeTab;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorEntities;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSamples;
import io.github.dirtyredz.herbalwitch.platform.placement.PlacementChecks;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.Locale;

/** English names of the furniture pieces, the decor tab and the placement refusals. */
public final class DecorTranslations {
    private DecorTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        lang.add(DecorCreativeTab.TITLE_KEY, "The Herbal Witch: Cottage Decor");
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            lang.add(entry.block().get(), entry.spec().displayName());
        }
        lang.add(key(PlacementChecks.Problem.UNLOADED), "Part of that space is not loaded.");
        lang.add(key(PlacementChecks.Problem.OUTSIDE_WORLD), "That would reach outside the world.");
        lang.add(key(PlacementChecks.Problem.OCCUPIED), "Something is in the way.");
        lang.add(key(PlacementChecks.Problem.OBSTRUCTED), "Something is standing in the way.");
        lang.add(key(PlacementChecks.Problem.UNSUPPORTED), "It needs something to stand on, hang from or lean against there.");
        lang.add(key(PlacementChecks.Problem.DENIED), "You may not build there.");
        lang.add(DecorEntities.SEAT.get(), "Seat");
        lang.add(SeatingService.OCCUPIED, "Someone is already sitting there.");
        lang.add(SeatingService.NO_ROOM, "There is no room to sit there.");
        lang.add(DecorShowcaseSamples.LABEL + "hinge_right", "Right hinge");
        lang.add(DecorShowcaseSamples.LABEL + "open_left", "Open, left");
        lang.add(DecorShowcaseSamples.LABEL + "open_right", "Open, right");
    }

    private static String key(PlacementChecks.Problem problem) {
        return PlacementChecks.KEY + problem.name().toLowerCase(Locale.ROOT);
    }
}
