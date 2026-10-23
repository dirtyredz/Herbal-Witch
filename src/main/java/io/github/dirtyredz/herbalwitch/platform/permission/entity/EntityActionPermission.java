package io.github.dirtyredz.herbalwitch.platform.permission.entity;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * The one boundary every harmful custom entity action passes (splash vials, blinding dust, binding
 * and sleep, coated-weapon poison, thorn retaliation, delayed poison ticks) and the one ward protection
 * consults. It supplements the block-level {@code ActionPermissionService}; a block permission is
 * never read as permission to affect an entity. Rules, in order: the actor never harms itself; a
 * removed or protected-tag creature ({@link #PROTECTED}: citizens by default) and another player's
 * pet are refused; another player only with this mod's preparation PvP enabled, the server's PvP on
 * and the teams' friendly-fire rules allowing it; a non-hostile target only where the actor may
 * interact (spawn protection); and while an unsupported land-claim system is loaded
 * ({@link ProtectionAdapters}) nothing non-hostile is affected. Every denial says why.
 */
public final class EntityActionPermission {
    /** Creatures no harmful preparation affects (villagers and wandering traders by default). */
    public static final TagKey<EntityType<?>> PROTECTED = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "protected_entities"));

    /** Why an action was refused; {@link #NONE} when allowed. */
    public enum Denial {
        NONE,
        SELF,
        GONE,
        PROTECTED,
        FOREIGN_PET,
        PVP_OFF,
        TEAM,
        SPAWN_PROTECTION,
        CLAIMS,
        UNSUPPORTED,
        OWNER_CONFLICT;

        public boolean allowed() {
            return this == NONE;
        }
    }

    private static volatile BooleanSupplier preparationPvp = () -> false;
    private static final java.util.List<Policy> POLICIES = new java.util.concurrent.CopyOnWriteArrayList<>();

    @FunctionalInterface
    public interface Policy {
        Denial check(EntityActionContext.Action action, UUID actor, LivingEntity target);
    }

    /** Feature-owned restrictions are additive: no contributor can waive a baseline denial. */
    public static void register(Policy policy) { POLICIES.add(policy); }

    public static Denial check(EntityActionContext.Action action, Entity actor, LivingEntity target) {
        Denial baseline = checkHarm(actor, target);
        if (!baseline.allowed()) return baseline;
        return policies(action, actor == null ? null : actor.getUUID(), target);
    }
    /** Delayed/automated effects keep their original action purpose when their actor is offline. */
    public static Denial checkByPrincipal(EntityActionContext.Action action, ServerLevel level, UUID actor, LivingEntity target) {
        var baseline = checkHarmByPrincipal(level, actor, target);
        return baseline.allowed() ? policies(action, actor, target) : baseline;
    }

    private static Denial policies(EntityActionContext.Action action, UUID actor, LivingEntity target) {
        for (Policy policy : POLICIES) {
            Denial result = policy.check(action, actor, target);
            if (!result.allowed()) return result;
        }
        return Denial.NONE;
    }

    private EntityActionPermission() {
    }

    /** Installs the configured preparation-PvP switch (bootstrap). */
    public static void pvpSetting(BooleanSupplier setting) {
        preparationPvp = setting;
    }

    public static boolean mayHarm(Entity actor, LivingEntity target) {
        return checkHarm(actor, target).allowed();
    }

    /** Whether {@code actor} (a player, a mob, or null for no one) may harm {@code target} now. */
    public static Denial checkHarm(Entity actor, LivingEntity target) {
        if (target == null || target.isRemoved() || !target.isAlive()) {
            return Denial.GONE;
        }
        if (actor == target) {
            return Denial.SELF;
        }
        UUID actorId = actor == null ? null : actor.getUUID();
        Denial common = common(actorId, target);
        if (!common.allowed()) {
            return common;
        }
        if (target instanceof Player victim) {
            if (!(actor instanceof Player attacker)) {
                return Denial.PVP_OFF;
            }
            if (!preparationPvp.getAsBoolean() || victim.getServer() == null || !victim.getServer().isPvpAllowed()) {
                return Denial.PVP_OFF;
            }
            if (!attacker.canHarmPlayer(victim)) {
                return Denial.TEAM;
            }
        }
        if (!(target instanceof Enemy)) {
            if (actor instanceof Player player && !mayInteract(target.level(), player, target.blockPosition())) {
                return Denial.SPAWN_PROTECTION;
            }
            if (ProtectionAdapters.restricted()) {
                return Denial.CLAIMS;
            }
        }
        return Denial.NONE;
    }

    /**
     * Whether a recorded principal (a UUID, online or not) may still harm {@code target}: for delayed
     * damage from an earlier action. An online principal is checked as a player; an offline one can
     * never establish player PvP and is refused around unsupported claims.
     */
    public static Denial checkHarmByPrincipal(ServerLevel level, UUID principal, LivingEntity target) {
        ServerPlayer online = principal == null ? null : OnlinePlayers.find(principal).orElse(null);
        if (online != null) {
            return checkHarm(online, target);
        }
        if (target == null || target.isRemoved() || !target.isAlive()) {
            return Denial.GONE;
        }
        Denial common = common(principal, target);
        if (!common.allowed()) {
            return common;
        }
        if (target instanceof Player) {
            return Denial.PVP_OFF;
        }
        if (!(target instanceof Enemy) && ProtectionAdapters.restricted()) {
            return Denial.CLAIMS;
        }
        return Denial.NONE;
    }

    /**
     * Whether a principal's protection (a ward owned by {@code owner}) may act at {@code pos}: allowed
     * unless an unsupported claim system is loaded, or the owner is online and may not interact there.
     * Protection is never harm: it only keeps creatures from choosing a target.
     */
    public static Denial checkProtect(ServerLevel level, UUID owner, BlockPos pos) {
        if (ProtectionAdapters.restricted()) {
            return Denial.CLAIMS;
        }
        ServerPlayer online = owner == null ? null : OnlinePlayers.find(owner).orElse(null);
        if (online != null && online.level() == level && !mayInteract(level, online, pos)) {
            return Denial.SPAWN_PROTECTION;
        }
        return Denial.NONE;
    }

    private static Denial common(UUID actorId, LivingEntity target) {
        if (target.getType().is(PROTECTED)) {
            return Denial.PROTECTED;
        }
        if (target instanceof OwnableEntity pet && pet.getOwnerUUID() != null && !pet.getOwnerUUID().equals(actorId)) {
            return Denial.FOREIGN_PET;
        }
        return policies(EntityActionContext.Action.HARM, actorId, target);
    }

    private static boolean mayInteract(Level level, Player player, BlockPos pos) {
        return level.isLoaded(pos) && level.getWorldBorder().isWithinBounds(pos) && level.mayInteract(player, pos);
    }
}
