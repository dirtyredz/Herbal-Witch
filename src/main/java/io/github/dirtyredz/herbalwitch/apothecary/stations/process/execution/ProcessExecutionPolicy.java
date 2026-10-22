package io.github.dirtyredz.herbalwitch.apothecary.stations.process.execution;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStatus;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/**
 * Who may advance and complete a station's job, consulted by the job but never owning inventory:
 * whether the matched recipe may hold its reservation, whether it may progress this tick, and how a
 * completed batch is settled around the inventory commit. {@link #ORDINARY} keeps Milestone 3's
 * behaviour (anything without requirements runs, automation included); stations that run witch
 * recipes compose a magical policy instead. Policies keep no energy themselves.
 */
public interface ProcessExecutionPolicy {
    /** Where the job runs: the station's level and origin cell. */
    record Site(Level level, BlockPos pos) {
    }

    /** Ordinary processing: recipes without requirements always run; a magical one never does. */
    ProcessExecutionPolicy ORDINARY = new ProcessExecutionPolicy() {
        @Override
        public boolean holds(ProcessRecipe recipe, Site site) {
            return !recipe.description().requirements().magical();
        }

        @Override
        public Optional<ProcessStatus> gate(ProcessRecipe recipe, Site site) {
            return recipe.description().requirements().magical() ? Optional.of(ProcessStatus.NEEDS_WITCH) : Optional.empty();
        }

        @Override
        public Optional<ProcessStatus> complete(ProcessRecipe recipe, Site site, ProcessCommit commit) {
            commit.apply();
            return Optional.empty();
        }

        @Override
        public void reset() {
        }
    };

    /**
     * Whether the recipe's inputs should be held reserved now: always for ordinary recipes, and for a
     * magical recipe only while an authorised batch exists (so unstarted ingredients stay removable).
     */
    boolean holds(ProcessRecipe recipe, Site site);

    /** The status that holds the job back this tick, or empty when it may progress. Mutates no inventory. */
    Optional<ProcessStatus> gate(ProcessRecipe recipe, Site site);

    /**
     * Settles one finished batch: revalidates whatever the policy needs, applies {@code commit} and any
     * payment, and puts everything back on a synchronous failure. Returns the status to show when the
     * batch was refused (nothing changed), or empty when it completed.
     */
    Optional<ProcessStatus> complete(ProcessRecipe recipe, Site site, ProcessCommit commit);

    /** The recipe changed, the job was cancelled or a batch completed: forget any authorisation. */
    void reset();

    /** The energy one batch of the recipe costs here now, after the server's multiplier (0 for ordinary). */
    default int energyCost(ProcessRecipe recipe) {
        return 0;
    }

    /** Whose batch this is, for a viewer's screen: 0 nobody's, 1 the viewer's, 2 someone else's. */
    default int relation(UUID viewer) {
        return 0;
    }

    /** Persists the authorisation (if any) beside the job. */
    default void save(CompoundTag job) {
    }

    default void load(CompoundTag job) {
    }
}
