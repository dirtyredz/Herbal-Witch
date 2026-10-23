package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.ai.FamiliarGuard;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarGuardSecurityGameTests {
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_guard_authorization", timeoutTicks = 100)
    public static void guardRefusesVisitorsPetsPassiveMobsAndRechecksRevokedPermissionBeforeHit(GameTestHelper h) {
        FamiliarFixtures.bound(h, "wolf", "guard_permissions", bound -> {
            var owner = bound.owner(); var visitor = AnimalFixtures.player(h, "m9_guard_visitor");
            owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            try (var scope = OnlinePlayers.register(List.of(owner, visitor))) {
                check(FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.GUARD_WITCH).equals("command_set"), "guard command refused");
                var zombie = h.spawn(EntityType.ZOMBIE, 5, 1, 5); zombie.setNoAi(true); zombie.setTarget(owner);
                var cow = h.spawn(EntityType.COW, 5, 1, 6); var cat = h.spawn(EntityType.CAT, 3, 1, 5); cat.setTame(true); cat.setOwnerUUID(visitor.getUUID());
                var guard = new FamiliarGuard(bound.mob()); guard.tick(owner.position());
                try {
                    check(guard.allowed(zombie) && !guard.allowed(cow) && !guard.allowed(cat), "strict hostile filter failed");
                    io.github.dirtyredz.herbalwitch.platform.permission.entity.ProtectionAdapters.overrideForTest(List.of("ftbchunks"));
                    try { check(!guard.allowed(zombie), "unsupported claims allowed automatic hostile harm"); }
                    finally { io.github.dirtyredz.herbalwitch.platform.permission.entity.ProtectionAdapters.overrideForTest(null); }
                    zombie.setTarget(visitor); check(!guard.allowed(zombie), "unrelated hostile counted as owner's threat"); zombie.setTarget(owner);
                    guard.perform(); check(guard.target() == zombie, "bounded observation missed the real threat");
                    var deny = new AtomicBoolean(true); UUID id = zombie.getUUID();
                    EntityActionPermission.register((action, actor, target) -> deny.get() && target.getUUID().equals(id) ? EntityActionPermission.Denial.CLAIMS : EntityActionPermission.Denial.NONE);
                    float health = zombie.getHealth();
                    try { guard.attack(); check(guard.target() == null && zombie.getHealth() == health, "permission revoked after observation still allowed a hit"); }
                    finally { deny.set(false); }
                } finally { guard.clear(); }
            }
            h.succeed();
        });
    }
}
