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
        if (source instanceof TamableAnimal pet) { pet.setTame(true); pet.setOwnerUUID(owner.getUUID()); }
        var position = helper.absolutePos(new BlockPos(4, 1, 5)); source.moveTo(position.getCenter());
        check(helper.getLevel().addFreshEntity(source), "source insertion");
        AnimalFixtures.relationship(source, owner.getUUID(), spec.trust(), spec.feeds());
        if (!Set.of("cat", "wolf", "bat").contains(species)) check(AnimalTrustService.keeper(source, owner.getUUID()), "keeper adoption");
        helper.runAfterDelay(6, () -> whenReady(helper, () -> {
            source.moveTo(position.getCenter()); source.setTarget(null);
            String refusal = FamiliarEligibility.refusal(owner, source, helper.absolutePos(new BlockPos(4, 1, 4)));
            check(refusal == null, "fixture eligibility: " + refusal);
            check(FamiliarBindingService.bind(owner, source, helper.absolutePos(new BlockPos(4, 1, 4)), AltarOffering.none()), "fixture binding");
            var record = FamiliarStore.get(owner.server).creature(source.getUUID()).orElseThrow();
            run.accept(new Bound(owner, (Mob) helper.getLevel().getEntity(source.getUUID()), record.bond()));
        }));
    }
    /** Retained CI worlds can need more than six ticks to index their growing set of bond shards. */
    public static void whenReady(GameTestHelper helper, Runnable action) {
        if (!FamiliarStore.get(helper.getLevel().getServer()).ready()) helper.runAfterDelay(1, () -> whenReady(helper, action));
        else action.run();
    }
}
