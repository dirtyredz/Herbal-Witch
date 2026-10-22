package io.github.dirtyredz.herbalwitch.brewing.projectile;

import io.github.dirtyredz.herbalwitch.brewing.application.advanced.ThrowableApplications;
import io.github.dirtyredz.herbalwitch.brewing.application.advanced.ThrowableApplications.Burst;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * What a thrown preparation does where it lands, server side and once: it reaches the living creatures
 * within {@link #RADIUS} blocks with a clear line from the impact (blocks stop it), nearest first with
 * UUID tie-breaking, at most {@link #MAX_TARGETS}; each passes the burst's own eligibility (permission
 * for harmful bursts). Smoke is a burst of a bounded number of particles, never a lingering cloud.
 */
public final class PreparationImpact {
    public static final double RADIUS = 3.0;
    public static final int MAX_TARGETS = 32;
    public static final int PARTICLES = 24;

    private PreparationImpact() {
    }

    /** Applies the burst at {@code at}; returns the creatures it changed. */
    public static List<LivingEntity> burst(ServerLevel level, Vec3 at, Burst burst, Entity thrower, Entity carrier) {
        List<LivingEntity> reached = reachable(level, at, thrower, carrier);
        List<LivingEntity> changed = new ArrayList<>();
        for (LivingEntity target : reached) {
            if (ThrowableApplications.affect(burst, thrower, target)) {
                changed.add(target);
            }
        }
        particles(level, at, burst);
        return changed;
    }

    /** The creatures a burst at {@code at} reaches, nearest first, capped; never the thrower for harmful bursts. */
    public static List<LivingEntity> reachable(ServerLevel level, Vec3 at, Entity thrower, Entity carrier) {
        AABB box = new AABB(at, at).inflate(RADIUS);
        List<LivingEntity> near = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && !e.isSpectator() && e.getBoundingBox().getCenter().distanceTo(at) <= RADIUS + e.getBbWidth() / 2));
        near.sort(Comparator.comparingDouble((LivingEntity e) -> e.getBoundingBox().getCenter().distanceToSqr(at))
                .thenComparing(Entity::getUUID));
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : near) {
            if (out.size() >= MAX_TARGETS) {
                break;
            }
            if (clear(level, at, e, carrier)) {
                out.add(e);
            }
        }
        return out;
    }

    private static boolean clear(ServerLevel level, Vec3 at, LivingEntity target, Entity carrier) {
        Vec3 to = target.getBoundingBox().getCenter();
        HitResult hit = level.clip(new ClipContext(at, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, carrier));
        return hit.getType() == HitResult.Type.MISS;
    }

    private static void particles(ServerLevel level, Vec3 at, Burst burst) {
        var particle = switch (burst) {
            case CONCEALMENT -> ParticleTypes.CAMPFIRE_COSY_SMOKE;
            case BLINDING -> ParticleTypes.WHITE_ASH;
            case SLEEP -> ParticleTypes.NOTE;
            default -> ParticleTypes.EFFECT;
        };
        level.sendParticles(particle, at.x, at.y + 0.3, at.z, PARTICLES, RADIUS / 3, 0.4, RADIUS / 3, 0.02);
        level.levelEvent(2002, net.minecraft.core.BlockPos.containing(at), burst == Burst.BLINDING ? 0xE8E0C8 : 0x8A6A9A);
    }
}
