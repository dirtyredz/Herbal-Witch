package io.github.dirtyredz.herbalwitch.companions.familiars.fixture;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
import java.util.function.Consumer;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

/** Real entity and persisted player receipt; no fake familiar implementation or navigation. */
public final class FamiliarFixtures {
    private FamiliarFixtures() {}
    public record Bound(ServerPlayer owner, Mob mob, UUID bond) {
        public FamiliarRecord record() { return FamiliarStore.get(owner.server).record(bond).orElseThrow(); }
    }
    public static void bound(GameTestHelper helper, String species, String name, Consumer<Bound> run) {
        floor(helper); var owner = AnimalFixtures.realPlayer(helper, "m9_" + name);
        check(WitchStatusService.convert(owner, UUID.randomUUID()), "witch setup");
        var spec = FamiliarCatalog.find(species).orElseThrow();
        var source = (Mob) ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(spec.originalType())).create(helper.getLevel());
        if (source instanceof AgeableMob ageable) ageable.setAge(0);
        source.setPersistenceRequired(); // A remote real-player dimension fixture must not despawn this source.
        if (source instanceof TamableAnimal pet) { pet.setTame(true); pet.setOwnerUUID(owner.getUUID()); }
        var position = helper.absolutePos(new BlockPos(4, 1, 5)); source.moveTo(position.getCenter());
        check(helper.getLevel().addFreshEntity(source), "source insertion");
        AnimalFixtures.relationship(source, owner.getUUID(), spec.trust(), spec.feeds());
        if (!Set.of("cat", "wolf", "bat").contains(species)) check(AnimalTrustService.keeper(source, owner.getUUID()), "keeper adoption");
        helper.runAfterDelay(6, () -> bindPrepared(helper, owner, source, position, run));
    }
    /** Shared setup for already spawned, trusted native sources, including the exact-species matrix. */
    public static void bindPrepared(GameTestHelper helper, ServerPlayer owner, Mob source, BlockPos position, Consumer<Bound> run) {
        whenReady(helper, () -> bind(helper, owner, source, position, run, 20));
    }
    private static void bind(GameTestHelper helper, ServerPlayer owner, Mob source, BlockPos position, Consumer<Bound> run, int retries) {
        source.moveTo(position.getCenter()); source.setTarget(null);
        var altar = helper.absolutePos(new BlockPos(4, 1, 4));
        String refusal = FamiliarEligibility.refusal(owner, source, altar); check(refusal == null, "fixture eligibility: " + refusal);
        check(io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters.snapshot(source).isPresent(), "fixture source snapshot refused");
        check(WitchStatusService.energyUnits(owner) >= 2500, "fixture binding energy");
        var store = FamiliarStore.get(owner.server);
        if (!FamiliarBindingService.bind(owner, source, altar, AltarOffering.none())) {
            if (retries > 0 && store.creature(source.getUUID()).isEmpty() && source.isAlive() && !source.isRemoved()
                    && !io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt.pending(owner)) {
                helper.runAfterDelay(1, () -> bind(helper, owner, source, position, run, retries - 1)); return;
            }
            check(false, "fixture binding failed after transaction-budget wait: alive="+source.isAlive()+", removed="+source.isRemoved()+", record="+store.creature(source.getUUID()));
        }
        var record = store.creature(source.getUUID()).orElseThrow();
        run.accept(new Bound(owner, (Mob) helper.getLevel().getEntity(source.getUUID()), record.bond()));
    }
    /** Waits for the bond index (retained CI worlds can need more than six ticks) and for an entity-ticking plot. */
    public static void whenReady(GameTestHelper helper, Runnable action) {
        if (!FamiliarStore.get(helper.getLevel().getServer()).ready() || !io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.entitiesLive(helper))
            helper.runAfterDelay(1, () -> whenReady(helper, action));
        else action.run();
    }
}
