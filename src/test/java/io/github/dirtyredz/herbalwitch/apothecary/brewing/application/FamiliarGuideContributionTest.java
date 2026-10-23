package io.github.dirtyredz.herbalwitch.apothecary.brewing.application;

import io.github.dirtyredz.herbalwitch.grimoire.content.Contributions;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FamiliarGuideContributionTest {
    @Test void laterFeatureOwnsThePageWithoutFirstKeyWinsHidingIt() {
        var sources = new Contributions<GrimoireContentSource>();
        var guide = new FamiliarGuideContribution(sources::add);
        List<String> emitted = new ArrayList<>();
        sources.add(300, guide.fallback((inputs, out) -> emitted.add("deferred")));
        sources.ordered().forEach(source -> source.contribute(null, ignored -> {}));
        assertEquals(List.of("deferred"), emitted);
        emitted.clear();
        guide.install(1200, (inputs, out) -> emitted.add("companions"));
        sources.ordered().forEach(source -> source.contribute(null, ignored -> {}));
        assertEquals(List.of("companions"), emitted, "the earlier brewing fallback must not claim the key");
    }

    @Test void installationBeforeBrewingRegistrationAlsoSuppressesTheFallback() {
        var sources = new Contributions<GrimoireContentSource>();
        var guide = new FamiliarGuideContribution(sources::add);
        List<String> emitted = new ArrayList<>();
        guide.install(1200, (inputs, out) -> emitted.add("companions"));
        sources.add(300, guide.fallback((inputs, out) -> fail("fallback ran after ownership installed")));
        sources.ordered().forEach(source -> source.contribute(null, ignored -> {}));
        assertEquals(List.of("companions"), emitted);
    }

    @Test void aSecondOwnerAndNullSourcesAreRefusedWithoutChangingRegistration() {
        var sources = new Contributions<GrimoireContentSource>();
        var guide = new FamiliarGuideContribution(sources::add);
        assertThrows(NullPointerException.class, () -> guide.install(1200, null));
        assertThrows(NullPointerException.class, () -> guide.fallback(null));
        guide.install(1200, (inputs, out) -> {});
        assertThrows(IllegalStateException.class, () -> guide.install(1300, (inputs, out) -> {}));
        assertEquals(1, sources.ordered().size());
    }
}
