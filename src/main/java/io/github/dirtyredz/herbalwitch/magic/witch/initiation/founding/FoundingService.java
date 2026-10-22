package io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * The world's founding eligibility on a running server, always through the Overworld's
 * {@link FoundingSavedData} and always on the server thread (so a claim is exclusive across every
 * altar and dimension). Survival founding is public: open means any non-witch may attempt it.
 */
public final class FoundingService {
    /** GameTest seam: when set and false, the next durable persist fails (a controlled interruption). */
    private static volatile BooleanSupplier persistHook;

    private FoundingService() {
    }

    public static FoundingSavedData data(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(FoundingSavedData::load, FoundingSavedData::create, FoundingSavedData.NAME);
    }

    /** Whether a survival founding may begin now (public: no world records are exposed). */
    public static boolean open(MinecraftServer server) {
        FoundingSavedData data = data(server);
        return WitchRules.server().foundingEnabled() && data.writable() && data.record().state() == FoundingRecord.State.OPEN;
    }

    /** Takes the exclusive world claim for {@code candidate} at {@code site}; empty when closed or held. */
    public static Optional<UUID> claim(MinecraftServer server, ServerPlayer candidate, RitualSite site) {
        if (!open(server)) {
            return Optional.empty();
        }
        FoundingSavedData data = data(server);
        Optional<UUID> claim = data.record().claim(candidate.getUUID(), site, UUID.randomUUID());
        data.setDirty();
        return claim;
    }

    /** Ordinary cancellation before commit: the claim is released. */
    public static void release(MinecraftServer server, UUID claim) {
        FoundingSavedData data = data(server);
        if (data.record().release(claim)) {
            data.setDirty();
        }
    }

    /** Whether {@code claim} still holds the world (a conversion elsewhere voids it). */
    public static boolean holds(MinecraftServer server, UUID claim) {
        FoundingRecord record = data(server).record();
        return record.state() == FoundingRecord.State.CLAIMED && record.claim().map(claim::equals).orElse(false);
    }

    /**
     * Persists the durable claim immediately before an irreversible completion. False (and the claim
     * released) when the claim is gone or the write failed: the caller must then consume nothing.
     */
    public static boolean beginCommit(MinecraftServer server, UUID claim) {
        FoundingSavedData data = data(server);
        if (!data.record().beginCommit(claim)) {
            return false;
        }
        data.setDirty();
        BooleanSupplier hook = persistHook;
        persistHook = null;
        boolean persisted = (hook == null || hook.getAsBoolean()) && data.persistNow(server);
        if (!persisted) {
            data.record().abortCommit(claim);
            data.setDirty();
        }
        return persisted;
    }

    /** A synchronous failure after the durable claim: back to open, nothing was consumed. */
    public static void abortCommit(MinecraftServer server, UUID claim) {
        FoundingSavedData data = data(server);
        if (data.record().abortCommit(claim)) {
            data.setDirty();
            data.persistNow(server);
        }
    }

    /** The founding conversion happened: founding is closed for good. */
    public static void complete(MinecraftServer server, UUID claim, UUID founder) {
        FoundingSavedData data = data(server);
        if (data.record().complete(claim, founder)) {
            data.setDirty();
            data.persistNow(server);
        }
    }

    /** Conversion listener: the first conversion by any route closes open eligibility. */
    public static void conversionCompleted(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            FoundingSavedData data = data(server);
            if (data.writable() && data.record().conversionElsewhere()) {
                data.setDirty();
            }
        }
    }

    /** An operator closes an interrupted claim; nothing is replayed. */
    public static boolean resolveClose(MinecraftServer server, UUID claim) {
        FoundingSavedData data = data(server);
        if (data.writable() && data.record().resolveClose(claim)) {
            data.setDirty();
            data.persistNow(server);
            return true;
        }
        return false;
    }

    /** GameTest seam: the world's founding reopens (the GameTest world is shared by every test). */
    public static void resetForTest(MinecraftServer server) {
        FoundingSavedData data = data(server);
        data.record().set(FoundingRecord.State.OPEN, null, null, null, null);
        data.setDirty();
    }

    /** GameTest seam: the next {@link #beginCommit} persists only if {@code hook} answers true. */
    public static void interceptNextPersist(BooleanSupplier hook) {
        persistHook = hook;
    }
}
