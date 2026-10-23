package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarSpeciesAdapters;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraftforge.gametest.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class FamiliarSpeciesBindingGameTests {
    private static void species(GameTestHelper helper, String key, boolean duplicate) {
        floor(helper);
        var player = AnimalFixtures.realPlayer(helper, "m9_species_" + key + (duplicate ? "_duplicate" : ""));
        check(WitchStatusService.convert(player, UUID.randomUUID()), "witch setup");
        var spec = FamiliarCatalog.find(key).orElseThrow();
        var type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(spec.originalType()));
        var source = (Mob) type.create(helper.getLevel());
        source.setPersistenceRequired();
        if (source instanceof AgeableMob ageable) ageable.setAge(0);
        if (source instanceof TamableAnimal pet) { pet.setTame(true); pet.setOwnerUUID(player.getUUID()); }
        source.moveTo(helper.absolutePos(new BlockPos(4, 1, 5)).getCenter());
        check(helper.getLevel().addFreshEntity(source), "source insertion");
        AnimalFixtures.relationship(source, player.getUUID(), spec.trust(), spec.feeds());
        if (!Set.of("cat", "wolf", "bat").contains(key)) check(AnimalTrustService.keeper(source, player.getUUID()), "keeper adoption");
        helper.runAfterDelay(6, () -> {
            source.moveTo(helper.absolutePos(new BlockPos(4, 1, 5)).getCenter()); source.setTarget(null);
            var origin = helper.absolutePos(new BlockPos(4, 1, 4));
            String refusal = FamiliarEligibility.refusal(player, source, origin); check(refusal == null, "eligibility: " + refusal);
            source.getPersistentData().putString("foreign", "preserve me");
            check(FamiliarSpeciesAdapters.snapshot(source).isEmpty(), "foreign payload silently stripped"); source.getPersistentData().remove("foreign");
            io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures.bindPrepared(helper, player, source, helper.absolutePos(new BlockPos(4,1,5)), bound -> {
            var store = FamiliarStore.get(player.server); var r = store.creature(source.getUUID()).orElseThrow();
            var familiar = (Mob) helper.getLevel().getEntity(source.getUUID());
            check(familiar != null && familiar != source && familiar.getType() == FamiliarEntities.type(key), "wrong familiar species");
            check(FamiliarReconciliation.matches(familiar, r) && AnimalTrustService.relationship(familiar, player.getUUID()).feeds() == spec.feeds(), "identity or relationship lost");
            check(WitchStatusService.energyUnits(player) == 7500, "species charged wrong energy");
            if (duplicate) {
                CompoundTag saved = new CompoundTag(); familiar.saveWithoutId(saved);
                var copied = FamiliarEntities.type(key).create(helper.getLevel()); copied.load(saved);
                check(!helper.getLevel().addFreshEntity(copied), "duplicate current incarnation admitted");
                check(familiar.isRemoved() && store.record(r.bond()).orElseThrow().phase() == FamiliarRecord.Phase.QUARANTINED, "conflict chose first incarnation instead of quarantine");
            }
            helper.succeed();
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_raven", timeoutTicks = 100) public static void raven(GameTestHelper helper) { species(helper, "raven", false); }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_owl", timeoutTicks = 100) public static void owl(GameTestHelper helper) { species(helper, "owl", false); }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_fox", timeoutTicks = 100) public static void fox(GameTestHelper helper) { species(helper, "fox", false); }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_wolf", timeoutTicks = 100) public static void wolf(GameTestHelper helper) { species(helper, "wolf", false); }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_snake", timeoutTicks = 100) public static void snake(GameTestHelper helper) { species(helper, "snake", false); }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_rabbit", timeoutTicks = 100) public static void rabbit(GameTestHelper helper) { species(helper, "rabbit", false); }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_bat", timeoutTicks = 100) public static void bat(GameTestHelper helper) { species(helper, "bat", false); }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_duplicate", timeoutTicks = 100) public static void duplicate(GameTestHelper helper) { species(helper, "cat", true); }
}
