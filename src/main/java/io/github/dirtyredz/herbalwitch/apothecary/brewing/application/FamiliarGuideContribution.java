package io.github.dirtyredz.herbalwitch.apothecary.brewing.application;

import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Bootstrap-only ownership handoff for the familiar guide. Brewing supplies a deferred fallback;
 * the bonding feature installs its own source at its own reading priority. Suppress the fallback
 * before content is assembled, rather than relying on a late override of a first-key-wins index.
 * This contract knows no companion implementation and owns no gameplay or discovery state.
 */
public final class FamiliarGuideContribution {
    public static final FamiliarGuideContribution INSTANCE = new FamiliarGuideContribution(GrimoireContent::register);
    private final BiConsumer<Integer, GrimoireContentSource> register;
    private volatile boolean installed;

    FamiliarGuideContribution(BiConsumer<Integer, GrimoireContentSource> register) {
        this.register = Objects.requireNonNull(register);
    }

    /** Called once from the owning feature's registration, before either side builds content. */
    public synchronized void install(int priority, GrimoireContentSource source) {
        Objects.requireNonNull(source);
        if (installed) throw new IllegalStateException("Familiar guide already has an owner");
        register.accept(priority, source);
        installed = true;
    }

    /** The fallback remains lazy, so bootstrap order cannot leave a competing familiar entry. */
    public GrimoireContentSource fallback(GrimoireContentSource source) {
        Objects.requireNonNull(source);
        return (inputs, out) -> {
            if (!installed) source.contribute(inputs, out);
        };
    }
}
