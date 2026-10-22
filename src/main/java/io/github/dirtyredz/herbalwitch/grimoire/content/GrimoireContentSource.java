package io.github.dirtyredz.herbalwitch.grimoire.content;

import java.util.function.Consumer;

/**
 * A feature's contribution to the grimoire. Sources run on both sides from the same inputs (the
 * recipe manager, the server's herb conditions and the editorial definitions), so the server and
 * every client build the same entries. Later features (witchcraft, familiars, rituals) register
 * their own source; they add typed entries and grant discoveries through the discovery service,
 * and never treat grimoire knowledge as permission to do anything.
 */
@FunctionalInterface
public interface GrimoireContentSource {
    /** Emits this source's entries, deterministically ordered; editorial overlays are applied afterwards. */
    void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out);
}
