package io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization;

import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStatus;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.execution.ProcessCommit;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.execution.ProcessExecutionPolicy;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/**
 * The execution policy of a station that runs witch recipes (and possibly ordinary ones). Ordinary
 * recipes run exactly as before. A magical batch runs only after its witch explicitly starts it
 * ({@link #start}); that binds the batch to the payer, the recipe's identity and fingerprint and its
 * effective cost ({@link StationOperator}), never to whoever is nearest or merely watching. It then
 * progresses only while that payer is online, alive, in the station's dimension, within
 * {@link #RANGE} blocks of the origin, allowed to use the station and able to afford the whole cost;
 * otherwise it pauses, spending nothing. Completion revalidates all of that, applies the inventory
 * commit, debits the energy through {@link WitchStatusService} and awards practice only after both
 * succeeded, restoring the inventory and the witch's state on a synchronous failure. Every batch
 * needs its own start.
 *
 * <p>The authorisation's lease (the payer actively attending) ends when the payer leaves the server,
 * dies or changes dimension, and is never saved: after a restart or a relog the recorded payer must
 * {@link #resume} explicitly. Others may cancel only a batch whose lease has ended.
 */
public final class MagicalProcessPolicy implements ProcessExecutionPolicy {
    /** Blocks from the station origin the payer must stay within. */
    public static final double RANGE = 8.0;

    private StationOperator operator;
    private boolean leased;

    // ---------------------------------------------------------------- policy

    @Override
    public boolean holds(ProcessRecipe recipe, Site site) {
        if (!recipe.description().requirements().magical()) {
            return true;
        }
        if (operator != null && !operator.covers(recipe, energyCost(recipe))) {
            reset(); // the recipe, its fingerprint or its configured cost changed
        }
        return operator != null;
    }

    @Override
    public Optional<ProcessStatus> gate(ProcessRecipe recipe, Site site) {
        if (!recipe.description().requirements().magical()) {
            return Optional.empty();
        }
        if (operator == null) {
            return Optional.of(ProcessStatus.NEEDS_WITCH);
        }
        Optional<ServerPlayer> payer = OnlinePlayers.find(operator.player());
        if (payer.isEmpty() || !payer.get().isAlive() || payer.get().level() != site.level()) {
            leased = false; // left, died or travelled: the recorded payer must resume
            return Optional.of(ProcessStatus.NEEDS_OPERATOR);
        }
        if (!leased || !attending(payer.get(), site)) {
            return Optional.of(ProcessStatus.NEEDS_OPERATOR);
        }
        EnergyService.Result afford = WitchStatusService.check(payer.get(), EnergyService.units(operator.energy()));
        if (afford == EnergyService.Result.NOT_WITCH) {
            return Optional.of(ProcessStatus.NEEDS_WITCH);
        }
        return afford == EnergyService.Result.OK ? Optional.empty() : Optional.of(ProcessStatus.NEEDS_ENERGY);
    }

    @Override
    public Optional<ProcessStatus> complete(ProcessRecipe recipe, Site site, ProcessCommit commit) {
        if (!recipe.description().requirements().magical()) {
            commit.apply();
            return Optional.empty();
        }
        Optional<ProcessStatus> gate = gate(recipe, site);
        if (gate.isPresent()) {
            return gate;
        }
        if (operator == null || !operator.covers(recipe, energyCost(recipe))) {
            reset();
            return Optional.of(ProcessStatus.NEEDS_WITCH);
        }
        ServerPlayer payer = OnlinePlayers.find(operator.player()).orElseThrow();
        int units = EnergyService.units(operator.energy());
        Optional<WitchState> before = WitchStatusService.snapshot(payer);
        if (before.isEmpty()) {
            return Optional.of(ProcessStatus.NEEDS_WITCH);
        }
        try {
            commit.apply();
            if (WitchStatusService.debit(payer, units) != EnergyService.Result.OK) {
                commit.rollback();
                WitchStatusService.restore(payer, before.get());
                return Optional.of(ProcessStatus.NEEDS_ENERGY);
            }
            WitchStatusService.awardPractice(payer, units);
        } catch (RuntimeException e) {
            commit.rollback();
            WitchStatusService.restore(payer, before.get());
            throw e;
        }
        WitchStatusService.flush(payer);
        return Optional.empty();
    }

    @Override
    public void reset() {
        operator = null;
        leased = false;
    }

    @Override
    public int energyCost(ProcessRecipe recipe) {
        return recipe.description().requirements().effectiveEnergy(WitchRules.server().workings().processingPercent());
    }

    @Override
    public int relation(UUID viewer) {
        if (operator == null) {
            return 0;
        }
        return operator.player().equals(viewer) ? 1 : 2;
    }

    @Override
    public void save(CompoundTag job) {
        if (operator != null) {
            job.put("Operator", operator.save());
        }
    }

    @Override
    public void load(CompoundTag job) {
        operator = job.contains("Operator") ? StationOperator.load(job.getCompound("Operator")).orElse(null) : null;
        leased = false; // never saved: the payer resumes after a restart
    }

    // ---------------------------------------------------------------- operations

    /** The recorded payer, if a batch is authorised. */
    public Optional<StationOperator> operator() {
        return Optional.ofNullable(operator);
    }

    public boolean leased() {
        return leased;
    }

    /**
     * A witch starts one batch of the matched magical recipe and becomes its payer. Returns the
     * outcome key ({@code station.<outcome>}).
     */
    public String start(ServerPlayer player, ProcessRecipe recipe, Site site) {
        if (recipe == null) {
            return "nothing";
        }
        if (!recipe.description().requirements().magical()) {
            return "ordinary";
        }
        int cost = energyCost(recipe);
        if (operator != null && operator.covers(recipe, cost)) {
            return operator.player().equals(player.getUUID()) ? "already" : "busy";
        }
        if (!WitchStatusService.isWitch(player)) {
            return "not_witch";
        }
        if (!attending(player, site)) {
            return "too_far";
        }
        if (WitchStatusService.check(player, EnergyService.units(cost)) != EnergyService.Result.OK) {
            return "not_enough_energy";
        }
        operator = new StationOperator(player.getUUID(), recipe.getId(), recipe.fingerprint(), cost);
        leased = true;
        return "started";
    }

    /** The recorded payer takes up their batch again (after a relog, a restart or a dimension trip). */
    public String resume(ServerPlayer player, ProcessRecipe recipe, Site site) {
        if (operator == null || recipe == null || !operator.covers(recipe, energyCost(recipe))) {
            return "nothing";
        }
        if (!operator.player().equals(player.getUUID())) {
            return "not_yours";
        }
        if (!attending(player, site)) {
            return "too_far";
        }
        leased = true;
        return "resumed";
    }

    /**
     * Whether {@code player} may cancel the authorised batch: its payer always; anyone else only once
     * the payer's lease has ended. Nothing is consumed by cancelling.
     */
    public boolean mayCancel(ServerPlayer player) {
        return operator != null && (operator.player().equals(player.getUUID()) || !leased);
    }

    /** Alive, not spectating, in the station's level, within range and allowed to use it. */
    private static boolean attending(ServerPlayer player, Site site) {
        if (!player.isAlive() || player.isSpectator() || player.level() != site.level()) {
            return false;
        }
        BlockPos pos = site.pos();
        if (player.position().distanceTo(Vec3.atCenterOf(pos)) > RANGE) {
            return false;
        }
        return ActionPermissionService.mayUse(ActionContext.of(player, site.level(), pos));
    }
}
