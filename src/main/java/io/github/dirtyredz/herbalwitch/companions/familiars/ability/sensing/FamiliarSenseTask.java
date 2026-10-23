package io.github.dirtyredz.herbalwitch.companions.familiars.ability.sensing;

import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.companions.familiars.ability.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.ability.action.FamiliarCombatPowers;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarObservationsStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.work.*;
import io.github.dirtyredz.herbalwitch.platform.permission.*;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import java.util.*;

/** One scheduled turn examines at most 32 entities or 64 blocks; no loaded-chunk search or tickets. */
public final class FamiliarSenseTask implements FamiliarWork.Task {
    private final Mob familiar;
    private final FamiliarPower power;
    private final FamiliarHerbCursor herbs;
    private final long rulesRevision = FamiliarServerConfig.revision();
    private boolean done;
    private int turns;
    public FamiliarSenseTask(Mob familiar, FamiliarPower power, FamiliarHerbCursor herbs) { this.familiar = familiar; this.power = power; this.herbs = herbs; }
    public boolean done() { return done; }
    /** What each entity power can use, so the bounded scan is never filled by entities it would skip. */
    private static java.util.function.Predicate<Entity> relevant(FamiliarPower power) {
        return switch (power) {
            case RETRIEVE -> e -> e instanceof net.minecraft.world.entity.item.ItemEntity;
            case THREATS -> FamiliarQueries.HOSTILE.or(e -> e instanceof io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.PreparationProjectile);
            case TRACK -> FamiliarQueries.HOSTILE;
            default -> e -> e instanceof Mob;
        };
    }
    @Override public boolean valid() { return !done && rulesRevision == FamiliarServerConfig.revision() && FamiliarAbilityService.ready(familiar, power)
            && FamiliarReconciliation.record(familiar).flatMap(r -> OnlinePlayers.find(r.owner())).filter(p -> p.level() == familiar.level()).isPresent(); }
    @Override public void perform() {
        if (!valid()) { done = true; return; }
        done = true;
        var record = FamiliarReconciliation.record(familiar).orElseThrow(); var owner = OnlinePlayers.find(record.owner()).orElse(null); if (owner == null) return;
        var result = new ArrayList<FamiliarObservationsStore.Target>(); int radius = FamiliarServerConfig.current().senseRadius();
        if (power == FamiliarPower.HERBS || power == FamiliarPower.GARDEN) {
            for (int i = 0; i < FamiliarQueries.BLOCKS && result.size() < 8; i++) {
                BlockPos pos = herbs.next(familiar.blockPosition());
                if (pos.distSqr(familiar.blockPosition()) > radius * radius || !familiar.level().hasChunkAt(pos)
                        || !ActionPermissionService.mayUse(ActionContext.of(owner, familiar.level(), pos))
                        || !PrincipalPermission.mayUse(owner.serverLevel(), record.owner(), pos).allowed()) continue;
                var state = familiar.level().getBlockState(pos);
                if (state.getBlock() instanceof HerbCropBlock crop && crop.isMaxAge(state) == (power == FamiliarPower.HERBS))
                    result.add(new FamiliarObservationsStore.Target(null, pos, state, "herb", state.getBlock().getName().getString()));
            }
        } else for (var entity : FamiliarQueries.entities((ServerLevel) familiar.level(), familiar.getBoundingBox().inflate(radius), relevant(power))) {
            if (result.size() >= (power == FamiliarPower.TRACK ? 1 : 8)) break;
            if (power == FamiliarPower.RETRIEVE) {
                if (entity instanceof net.minecraft.world.entity.item.ItemEntity item
                        && io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval.FamiliarRetrievalRules.eligible(owner, familiar, record, item))
                    result.add(new FamiliarObservationsStore.Target(item.getUUID(), item.blockPosition(), null, "item", item.getName().getString(), item.getItem().save(new net.minecraft.nbt.CompoundTag())));
                continue;
            }
            if (power == FamiliarPower.THREATS && record.species().equals("cat")
                    && entity instanceof io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.PreparationProjectile projectile
                    && projectile.burst().harmful() && !projectile.isRemoved() && !projectile.isInvisible()
                    && familiar.distanceToSqr(projectile) <= radius * radius && familiar.hasLineOfSight(projectile)
                    && PrincipalPermission.mayUse(owner.serverLevel(), owner.getUUID(), projectile.blockPosition()).allowed()) {
                result.add(new FamiliarObservationsStore.Target(projectile.getUUID(), projectile.blockPosition(), null, "magic", "Harmful preparation"));
                continue;
            }
            if (!(entity instanceof Mob candidate) || entity == familiar || entity instanceof Player || !candidate.isAlive() || candidate.isInvisible()
                    || !familiar.hasLineOfSight(candidate) || familiar.distanceToSqr(candidate) > radius * radius) continue;
            boolean allowed = power == FamiliarPower.CREATURES
                    ? EntityActionPermission.checkHarm(owner, candidate).allowed() && PrincipalPermission.mayUse(owner.serverLevel(), owner.getUUID(), candidate.blockPosition()).allowed()
                    : FamiliarCombatPowers.target(familiar, candidate, radius);
            if (!allowed || record.species().equals("bat") && familiar.level().getMaxLocalRawBrightness(candidate.blockPosition()) > 7) continue;
            result.add(new FamiliarObservationsStore.Target(candidate.getUUID(), candidate.blockPosition(), null, "creature", candidate.getName().getString()));
        }
        if (result.isEmpty()) {
            if ((power == FamiliarPower.HERBS || power == FamiliarPower.GARDEN) && ++turns < 200) done = false;
            return;
        }
        if (power.sensing()) FamiliarAbilityService.perform(familiar, power, () -> FamiliarObservationsStore.publish(owner, familiar, result));
        else FamiliarObservationsStore.publish(owner, familiar, result);
    }
}
