package io.github.dirtyredz.herbalwitch.companions.familiars.work;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import java.util.*;
import java.util.function.Predicate;

public final class FamiliarQueries {
    public static final int CANDIDATES = 32, RESULTS = 8, BLOCKS = 64;
    /** Hostile creatures: what guards, scouts and threat or track senses look for. */
    public static final Predicate<Entity> HOSTILE = e -> e instanceof Mob && e instanceof Enemy;
    private FamiliarQueries() {}
    /**
     * At most 32 entities in {@code bounds} that pass {@code relevant}, a cheap type test applied by the
     * engine before it counts towards the bound. Counting every entity (M12 fix) let a herd or a pile of
     * dropped items hide the hostile behind it; feature eligibility still runs on the returned few.
     */
    public static List<Entity> entities(ServerLevel level, AABB bounds, Predicate<Entity> relevant) {
        List<Entity> result = new ArrayList<>();
        level.getEntities(EntityTypeTest.forClass(Entity.class), bounds, relevant, result, CANDIDATES);
        return result;
    }
}
