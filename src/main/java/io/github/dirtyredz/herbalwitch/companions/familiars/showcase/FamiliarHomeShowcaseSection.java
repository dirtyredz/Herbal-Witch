package io.github.dirtyredz.herbalwitch.companions.familiars.showcase;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarHomes;
import io.github.dirtyredz.herbalwitch.showcase.layout.*;
import java.util.*;

/** Functional empty homes; first permitted book use claims each unowned sample. */
public final class FamiliarHomeShowcaseSection implements ShowcaseSection {
    @Override public String id() { return "familiar_homes"; }
    @Override public String titleKey() { return "showcase.herbalwitch.section.familiar_homes"; }
    @Override public List<ShowcasePlacement> placements() {
        var out = new ArrayList<ShowcasePlacement>(); int z = 1;
        for (var kind : FamiliarHomeKind.values()) {
            out.add(ShowcasePlacement.label(0, z, ShowcasePlacement.Line.translated("block.herbalwitch." + kind.path().replace('/', '.')),
                    ShowcasePlacement.Line.translated("showcase.herbalwitch.label.familiar_home")));
            out.add(ShowcasePlacement.content(1, 1, z, () -> FamiliarHomes.get(kind).block().get().defaultBlockState())); z += 3;
        }
        return out;
    }
}
