package io.github.dirtyredz.herbalwitch.companions.animals.config;

/** Independently switchable participation and hard resource bounds. Mechanical care values live in TrustRules. */
public record AnimalRules(boolean relationships, boolean attraction, boolean calming, boolean adoption,
                          boolean captures, boolean modded, int relationshipsPerAnimal, int radius, int candidates,
                          int attractedPerPlayer, int activeGoals, int checksPerTick, int pathsPerTick,
                          int payloadBytes, int nbtDepth, int perCaptor, int storedWorld, int lifetime,
                          int snapshotBytes, int transactionInterval) {
    public static final AnimalRules DEFAULTS = new AnimalRules(true, true, true, true, true, false,
            8, 8, 8, 8, 256, 128, 16, 32768, 16, 16, 128, 4096, 8 * 1024 * 1024, 20);

    public AnimalRules {
        bound(relationshipsPerAnimal, 1, 16); bound(radius, 1, 12); bound(candidates, 1, 16);
        bound(attractedPerPlayer, 1, 16); bound(activeGoals, 1, 512); bound(checksPerTick, 1, 256);
        bound(pathsPerTick, 1, 32); bound(payloadBytes, 1024, 65536); bound(nbtDepth, 1, 32);
        bound(perCaptor, 1, 64); bound(storedWorld, 1, 512); bound(lifetime, 1, 16384);
        bound(snapshotBytes, 65536, 16 * 1024 * 1024); bound(transactionInterval, 5, 1200);
        if (perCaptor > storedWorld || storedWorld > lifetime || payloadBytes > snapshotBytes)
            throw new IllegalArgumentException("Inconsistent animal storage bounds");
    }

    private static void bound(int value, int min, int max) {
        if (value < min || value > max) throw new IllegalArgumentException("Animal rule outside hard bounds");
    }
}
