package io.github.dirtyredz.herbalwitch.companions.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.care.taming.*;
import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.function.Consumer;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
import static io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess.Reason.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class AnimalTamingGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_animal_keeper")
    public static void keeperNeedsEarnedHistoryAndExtraBaitAndCannotBeStolen(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_keeper"); var competitor = AnimalFixtures.player(h, "m8_keeper_other");
        var cow = AnimalFixtures.animal(h, EntityType.COW); AnimalFixtures.relationship(cow, player.getUUID(), 80, 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(Bait.GRAIN), 2));
        check(AnimalTamingService.adopt(player, cow) == TRUST && player.getMainHandItem().getCount() == 2, "three feeds adopted cow");
        AnimalFixtures.relationship(cow, player.getUUID(), 80, 4);
        check(AnimalTamingService.adopt(player, cow) == READY && player.getMainHandItem().getCount() == 1, "keeper adoption failed");
        check(AnimalOwnership.of(cow, AnimalEligibility.profile(cow).orElseThrow()).ownedBy(player.getUUID()) && cow.isPersistenceRequired(), "keeper did not persist");
        competitor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(Bait.GRAIN)));
        check(AnimalTamingService.adopt(competitor, cow) != READY && competitor.getMainHandItem().getCount() == 1, "competitor stole keeper");
        check(AnimalTrustService.relationship(cow, player.getUUID()).trust() == 80, "adoption changed trust"); h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_native_tame")
    public static void nativeCatTamingEventCanCancelWithoutConsumingThenNativeOwnerIsSet(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_native_cat"); var cat = AnimalFixtures.animal(h, EntityType.CAT);
        AnimalFixtures.relationship(cat, player.getUUID(), 100, 5); player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(Bait.FISH), 2));
        Consumer<AnimalTameEvent> cancel = event -> { if (event.getAnimal() == cat) event.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, cancel);
        try { check(AnimalTamingService.adopt(player, cat) == CANCELED && !cat.isTame() && player.getMainHandItem().getCount() == 2, "canceled tame changed state"); }
        finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
        check(AnimalTamingService.adopt(player, cat) == READY && cat.isTame() && player.getUUID().equals(cat.getOwnerUUID()), "native cat owner missing");
        check(player.getMainHandItem().getCount() == 1, "native adoption cost wrong"); h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_ambiguous_owner")
    public static void tamedAnimalWithMissingNativeOwnerFailsClosed(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_missing_owner"); var horse = AnimalFixtures.animal(h, EntityType.HORSE);
        horse.setTamed(true); AnimalFixtures.relationship(horse, player.getUUID(), 100, 5);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(Bait.GRAIN)));
        check(AnimalOwnership.of(horse, AnimalEligibility.profile(horse).orElseThrow()).conflict(), "missing owner treated as unowned");
        check(AnimalTamingService.adopt(player, horse) != READY && player.getMainHandItem().getCount() == 1, "ambiguous horse retamed"); h.succeed();
    }
}
