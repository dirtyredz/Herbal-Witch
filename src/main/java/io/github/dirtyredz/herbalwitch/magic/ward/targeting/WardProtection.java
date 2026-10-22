package io.github.dirtyredz.herbalwitch.magic.ward.targeting;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.RepellentProfiles;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardIndex;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What a charged ward does, and nothing more: the selected mob families cannot choose a player who is
 * inside its volume, and its periodic sweep clears such targets they already hold. It never attacks,
 * pushes, teleports or removes a mob, prevents no damage and does not touch spawning; any player inside
 * benefits (friends need no allowlist), and players and pets are never its targets. Protection must be
 * allowed for the owner at the mob's and at the player's position (an absent owner through the
 * principal check); otherwise it is suspended. Acquisition asks the index for one chunk; a sweep
 * inspects at most {@link WardCatalog#CHECKS_PER_SWEEP} mobs of one slice of its volume (a cursor walks
 * the slices so crowded areas cannot starve the same mobs), within the dimension's per-tick budget.
 */
public final class WardProtection {
    /** Width of a sweep slice, blocks. */
    public static final int SLICE = 4;
    private static final Map<Long, Integer> CURSORS = new HashMap<>();
    private static final Map<String, long[]> BUDGET = new HashMap<>();

    private WardProtection() {
    }

    public static void reset() {
        CURSORS.clear();
        BUDGET.clear();
    }

    /** Whether a ward of this profile keeps {@code mob} away. */
    public static boolean matches(WardCatalog.Profile profile, LivingEntity mob) {
        return switch (profile) {
            case UNDEAD -> RepellentProfiles.UNDEAD.matches(mob);
            case ARTHROPODS -> RepellentProfiles.ARTHROPOD.matches(mob);
            case BOTH -> RepellentProfiles.UNDEAD.matches(mob) || RepellentProfiles.ARTHROPOD.matches(mob);
        };
    }

    /** Whether some active ward stops {@code mob} choosing {@code player} now. */
    public static boolean protects(ServerLevel level, Mob mob, Player player) {
        String dimension = level.dimension().location().toString();
        for (WardIndex.Entry ward : WardService.index().covering(dimension, player.blockPosition())) {
            if (matches(ward.profile(), mob) && allowed(level, ward, mob, player)) {
                return true;
            }
        }
        return false;
    }

    private static boolean allowed(ServerLevel level, WardIndex.Entry ward, Mob mob, Player player) {
        return EntityActionPermission.checkProtect(level, ward.owner(), mob.blockPosition()).allowed()
                && EntityActionPermission.checkProtect(level, ward.owner(), player.blockPosition()).allowed();
    }

    /** Forge bus: a protected player cannot be chosen. */
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && event.getNewTarget() instanceof Player player && mob.level() instanceof ServerLevel level
                && protects(level, mob, player)) {
            event.setCanceled(true);
        }
    }

    /** One sweep of one ward: clears matching mobs' targets on players inside it; returns the mobs inspected. */
    public static int sweep(ServerLevel level, WardIndex.Entry ward) {
        String dimension = ward.dimension();
        long[] budget = BUDGET.computeIfAbsent(dimension, k -> new long[] {-1, 0});
        if (budget[0] != level.getGameTime()) {
            budget[0] = level.getGameTime();
            budget[1] = 0;
        }
        int allowance = (int) Math.min(WardCatalog.CHECKS_PER_SWEEP, WardCatalog.CHECKS_PER_DIMENSION_TICK - budget[1]);
        if (allowance <= 0) {
            return 0;
        }
        int slices = (2 * ward.radius() + SLICE) / SLICE;
        int cursor = CURSORS.getOrDefault(ward.id(), 0) % slices;
        CURSORS.put(ward.id(), cursor + 1);
        int x0 = ward.centre().getX() - ward.radius() + cursor * SLICE;
        AABB slice = new AABB(x0, ward.centre().getY() - ward.height(), ward.centre().getZ() - ward.radius(),
                Math.min(x0 + SLICE, ward.centre().getX() + ward.radius() + 1), ward.centre().getY() + ward.height() + 1,
                ward.centre().getZ() + ward.radius() + 1);
        List<Mob> mobs = new ArrayList<>();
        level.getEntities(EntityTypeTest.forClass(Mob.class), slice, m -> matches(ward.profile(), m), mobs, allowance);
        budget[1] += mobs.size();
        for (Mob mob : mobs) {
            if (mob.getTarget() instanceof Player player && ward.contains(player.blockPosition()) && allowed(level, ward, mob, player)) {
                mob.setTarget(null);
            }
        }
        return mobs.size();
    }
}
