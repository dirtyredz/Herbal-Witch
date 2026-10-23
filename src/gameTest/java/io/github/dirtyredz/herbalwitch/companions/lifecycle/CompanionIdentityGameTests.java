package io.github.dirtyredz.herbalwitch.companions.lifecycle;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class CompanionIdentityGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_companion_identity")
    public static void ordinarySourceBeforeFirstRecordStillBlocksDuplicateAdmission(GameTestHelper h) {
        floor(h);
        var server = h.getLevel().getServer();
        var source = AnimalFixtures.animal(h, EntityType.COW);
        var copy = EntityType.COW.create(h.getLevel());
        try {
            copy.setUUID(source.getUUID());
            check(CompanionLifecycleAuthority.live(server, source.getUUID()), "unrecorded live source was missed");
            check(!CompanionLifecycleAuthority.admit(server, copy), "duplicate source was admitted");
            check(h.getLevel().getEntity(source.getUUID()) == source && !source.isRemoved(), "rejection changed source");
            CompanionLifecycleAuthority.retire(server, source.getUUID());
            check(source.isRemoved() && !CompanionLifecycleAuthority.live(server, source.getUUID()), "retirement left live source");
        } finally { copy.discard(); source.discard(); }
        h.succeed();
    }

    @GameTest(template = "empty", batch = "herbalwitch_companion_identity")
    public static void discardedGenerationAndItsLateLeaveCannotHideTheReplacement(GameTestHelper h) {
        floor(h);
        var server = h.getLevel().getServer();
        var original = AnimalFixtures.animal(h, EntityType.COW);
        var replacement = EntityType.COW.create(h.getLevel());
        try {
            check(CompanionLifecycleAuthority.admit(server, original), "source admission refused");
            replacement.setUUID(original.getUUID()); replacement.moveTo(original.position());
            original.discard();
            check(!CompanionLifecycleAuthority.live(server, original.getUUID()), "leave kept discarded object");
            check(CompanionLifecycleAuthority.admit(server, replacement), "replacement admission refused");
            check(h.getLevel().addFreshEntity(replacement), "real replacement insertion refused");
            CompanionLifecycleAuthority.leave(new EntityLeaveLevelEvent(original, h.getLevel()));
            check(CompanionLifecycleAuthority.live(server, replacement.getUUID()), "late leave hid current incarnation");
            check(h.getLevel().getEntity(replacement.getUUID()) == replacement, "replacement changed");
        } finally { original.discard(); replacement.discard(); }
        h.succeed();
    }
}
