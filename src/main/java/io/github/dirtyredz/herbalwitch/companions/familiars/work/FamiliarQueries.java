package io.github.dirtyredz.herbalwitch.companions.familiars.work;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import java.util.*;

public final class FamiliarQueries {
    public static final int CANDIDATES = 32, RESULTS = 8, BLOCKS = 64;
    private FamiliarQueries() {}
    /** Stop the engine iterator at 32, before feature filtering. No materialized unlimited query. */
    public static List<Entity> entities(ServerLevel level, AABB bounds) {
        List<Entity> result = new ArrayList<>();
        level.getEntities(EntityTypeTest.forClass(Entity.class), bounds, ignored -> true, result, CANDIDATES);
        return result;
    }
}
