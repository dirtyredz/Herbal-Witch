package io.github.dirtyredz.herbalwitch.companions.animals.care.interaction;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import java.util.ArrayList;
import java.util.List;

/** Feature contributions observe committed care without owning ordinary animal relationships. */
public final class AnimalCareContributions {
    public interface Contribution {
        boolean allowCappedFeed(ServerPlayer owner, Mob animal);
        void completed(ServerPlayer owner, Mob animal, boolean healing);
    }
    private static final List<Contribution> CONTRIBUTORS = new ArrayList<>();
    private AnimalCareContributions() {}
    public static void register(Contribution contribution) { CONTRIBUTORS.add(contribution); }
    public static boolean capped(ServerPlayer owner, Mob mob) { return CONTRIBUTORS.stream().anyMatch(c -> c.allowCappedFeed(owner, mob)); }
    public static void completed(ServerPlayer owner, Mob mob, boolean healing) { CONTRIBUTORS.forEach(c -> c.completed(owner, mob, healing)); }
}
