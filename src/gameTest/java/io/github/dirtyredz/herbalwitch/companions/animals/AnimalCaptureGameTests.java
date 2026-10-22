package io.github.dirtyredz.herbalwitch.companions.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.*;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.*;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureRecord.Phase;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.payload.VanillaCaptureAdapters;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfiles;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.*;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.UUID;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class AnimalCaptureGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_animal_adapter_matrix")
    public static void fifteenRealNativePayloadsRoundTripAgeNamesEffectsOwnersAndInventories(GameTestHelper h) {
        UUID owner = UUID.randomUUID();
        for (var profile : AnimalProfiles.ALL) {
            Mob mob = (Mob) ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(profile.id())).create(h.getLevel());
            if (!profile.capturable()) { check(VanillaCaptureAdapters.snapshot(mob) == null, "bat captured"); mob.discard(); continue; }
            mob.setCustomName(Component.literal("M8 specimen"));
            if (mob instanceof AgeableMob ageable) ageable.setAge(-12000);
            if (mob instanceof TamableAnimal pet) { pet.setTame(true); pet.setOwnerUUID(owner); pet.setOrderedToSit(true); }
            if (mob instanceof AbstractHorse horse) { horse.setTamed(true); horse.setOwnerUUID(owner); }
            AnimalFixtures.relationship(mob, owner, 80, 4);
            mob.addEffect(new MobEffectInstance(PreparationEffects.SLEEP.get(), 80));
            CompoundTag nativeData = new CompoundTag(); mob.saveWithoutId(nativeData);
            if (profile.id().matches("minecraft:(donkey|mule|llama)")) {
                nativeData.putBoolean("ChestedHorse", true); var items = new ListTag(); var stack = new ItemStack(Items.DIAMOND, 7).save(new CompoundTag());
                stack.putByte("Slot", (byte) 2); items.add(stack); nativeData.put("Items", items); mob.load(nativeData);
            }
            CompoundTag payload = VanillaCaptureAdapters.snapshot(mob); check(payload != null, "adapter refused " + profile.id());
            var record = new CaptureRecord(mob.getUUID(), 1, UUID.randomUUID(), Phase.STORED, owner, UUID.randomUUID(), profile.id(), 1, "specimen", "source", "", payload);
            Mob copy = VanillaCaptureAdapters.restore(h.getLevel(), record); check(copy != null, "restore failed " + profile.id());
            CompoundTag after = VanillaCaptureAdapters.snapshot(copy);
            for (String key : new String[] {"UUID", "CustomName", "Health", "Age", "Owner", "Tame", "Sitting", "ChestedHorse", "Items", "ArmorItem", "SaddleItem", "Variant", "Trusted", "ActiveEffects", "ForgeCaps"})
                check(java.util.Objects.equals(payload.get(key), after.get(key)), "lost " + key + " for " + profile.id());
            copy.discard(); mob.discard();
        }
        h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_capture_cycle", timeoutTicks = 100)
    public static void realActorCaptureReleaseAndCopiedTokenHaveOneAnimalEvenWithFullInventory(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.realPlayer(h, "transport"); var cow = AnimalFixtures.animal(h, EntityType.COW);
        AnimalFixtures.relationship(cow, player.getUUID(), 80, 4); cow.addEffect(new MobEffectInstance(PreparationEffects.SLEEP.get(), 100));
        for (int i = 0; i < player.getInventory().items.size(); i++) player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.CHARM.get()));
        check(CaptureService.capture(player, cow, InteractionHand.MAIN_HAND) == null, "capture failed");
        UUID animal = cow.getUUID(); ItemStack copy = player.getMainHandItem().copy(); var data = CaptureSavedData.get(player.server);
        check(cow.isRemoved() && data.record(animal).orElseThrow().phase() == Phase.STORED, "source/ledger not stored");
        check(copy.is(AnimalItems.FILLED_CHARM.get()) && copy.getTag().getCompound("Capture").getAllKeys().equals(java.util.Set.of("version", "token", "summary")), "payload leaked to item");
        h.runAfterDelay(22, () -> {
            BlockPos clicked = h.absolutePos(new BlockPos(5, 0, 5));
            check(CaptureService.release(player, InteractionHand.MAIN_HAND, clicked, Direction.UP) == null, "release failed");
            check(player.getMainHandItem().is(AnimalItems.CHARM.get()), "held filled charm was retained");
            Mob released = (Mob) h.getLevel().getEntity(animal);
            check(released != null && released != cow && CaptureReconciliation.generation(released) == 1, "wrong released incarnation");
            check(AnimalTrustService.relationship(released, player.getUUID()).trust() == 80, "transport lost trust");
            player.setItemInHand(InteractionHand.MAIN_HAND, copy);
            check("token".equals(CaptureService.release(player, InteractionHand.MAIN_HAND, clicked, Direction.UP)), "copied token replayed");
            check(h.getLevel().getEntity(animal) == released, "replay replaced animal"); h.succeed();
        });
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_capture_denial")
    public static void fakePlayersAndUnknownPersistentStateCannotConsumeOrCapture(GameTestHelper h) {
        floor(h); var fake = AnimalFixtures.player(h, "m8_capture_fake"); var cow = AnimalFixtures.animal(h, EntityType.COW);
        AnimalFixtures.relationship(cow, fake.getUUID(), 80, 4); cow.addEffect(new MobEffectInstance(PreparationEffects.SLEEP.get(), 100));
        fake.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.CHARM.get()));
        check("actor".equals(CaptureService.capture(fake, cow, InteractionHand.MAIN_HAND)), "fake transport allowed");
        check(!cow.isRemoved() && fake.getMainHandItem().is(AnimalItems.CHARM.get()), "denied fake mutated state");
        cow.getPersistentData().putString("unknown:bond", "external state");
        check(VanillaCaptureAdapters.snapshot(cow) == null && cow.getPersistentData().contains("unknown:bond"), "unknown data stripped"); h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_custom_capability")
    public static void taggedAnimalSubclassWithSerializedForeignCapabilityIsNeverCaptured(GameTestHelper h) {
        java.util.function.Consumer<net.minecraftforge.event.AttachCapabilitiesEvent<Entity>> attach = event -> {
            if (event.getObject() instanceof io.github.dirtyredz.herbalwitch.companions.animals.fixture.ForeignCapabilityCow) {
                event.addCapability(ResourceLocation.parse("m8_test:foreign_bond"), new net.minecraftforge.common.capabilities.ICapabilitySerializable<CompoundTag>() {
                    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, Direction side) {
                        return net.minecraftforge.common.util.LazyOptional.empty();
                    }
                    public CompoundTag serializeNBT() { var tag = new CompoundTag(); tag.putString("bond", "external authority"); return tag; }
                    public void deserializeNBT(CompoundTag tag) {}
                });
            }
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, attach);
        try {
            var cow = new io.github.dirtyredz.herbalwitch.companions.animals.fixture.ForeignCapabilityCow(h.getLevel());
            var data = new CompoundTag(); cow.saveWithoutId(data);
            check(cow.getType().is(io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags.CAPTURABLE), "fixture lacks capture tag");
            check(data.getCompound("ForgeCaps").contains("m8_test:foreign_bond"), "fixture did not serialize foreign capability");
            check(VanillaCaptureAdapters.snapshot(cow) == null && !cow.isRemoved(), "tagged custom animal accepted or removed");
            var after = new CompoundTag(); cow.saveWithoutId(after);
            check(data.equals(after), "refusal changed custom animal payload"); cow.discard(); h.succeed();
        } finally { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(attach); }
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_canceled_release", timeoutTicks = 100)
    public static void canceledReleaseRetainsPayloadAndItemWithoutAutomaticSecondSpawn(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.realPlayer(h, "cancel_release"); var cow = AnimalFixtures.animal(h, EntityType.COW);
        AnimalFixtures.relationship(cow, player.getUUID(), 80, 4); cow.addEffect(new MobEffectInstance(PreparationEffects.SLEEP.get(), 100));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.CHARM.get()));
        check(CaptureService.capture(player, cow, InteractionHand.MAIN_HAND) == null, "capture fixture failed");
        UUID animal = cow.getUUID(); ItemStack before = player.getMainHandItem().copy();
        h.runAfterDelay(22, () -> {
            java.util.function.Consumer<net.minecraftforge.event.entity.EntityJoinLevelEvent> cancel = event -> {
                if (event.getEntity().getUUID().equals(animal)) event.setCanceled(true);
            };
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, cancel);
            try {
                var clicked = h.absolutePos(new BlockPos(5, 0, 5));
                check("quarantine".equals(CaptureService.release(player, InteractionHand.MAIN_HAND, clicked, Direction.UP)), "canceled spawn not quarantined");
                var record = CaptureSavedData.get(player.server).record(animal).orElseThrow();
                check(record.phase() == Phase.QUARANTINED && record.storedPayload(), "failed insertion lost recovery payload");
                check(ItemStack.matches(before, player.getMainHandItem()) && h.getLevel().getEntity(animal) == null, "failed insertion changed item or left animal");
                check("token".equals(CaptureService.release(player, InteractionHand.MAIN_HAND, clicked, Direction.UP)), "failed insertion automatically replayed");
                check(CaptureSavedData.get(player.server).record(animal).orElseThrow() == record, "denied replay changed ledger"); h.succeed();
            } finally { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(cancel); }
        });
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_release_placement")
    public static void exactReleasePlacementRejectsCollisionHazardsAndDryAxolotl(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_release_place");
        var cow = EntityType.COW.create(h.getLevel()); var clicked = h.absolutePos(new BlockPos(4, 0, 5));
        CaptureReleasePlacement.position(cow, clicked, Direction.UP); check(CaptureReleasePlacement.safe(player, cow, clicked), "clear supported cell refused");
        h.setBlock(new BlockPos(4, 1, 5), Blocks.STONE); check(!CaptureReleasePlacement.safe(player, cow, clicked), "collision accepted");
        h.setBlock(new BlockPos(4, 1, 5), Blocks.AIR); h.setBlock(new BlockPos(4, 0, 5), Blocks.MAGMA_BLOCK);
        check(!CaptureReleasePlacement.safe(player, cow, clicked), "hazard support accepted");
        h.setBlock(new BlockPos(4, 0, 5), Blocks.STONE); var axolotl = EntityType.AXOLOTL.create(h.getLevel());
        CaptureReleasePlacement.position(axolotl, clicked, Direction.UP); check(!CaptureReleasePlacement.safe(player, axolotl, clicked), "dry axolotl released");
        h.setBlock(new BlockPos(4, 1, 5), Blocks.WATER); check(CaptureReleasePlacement.safe(player, axolotl, clicked), "water axolotl refused");
        cow.discard(); axolotl.discard(); h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_stale_join")
    public static void releasedGenerationRejectsStaleAndDuplicateRealEntityJoins(GameTestHelper h) {
        floor(h); var original = EntityType.COW.create(h.getLevel()); var payload = VanillaCaptureAdapters.snapshot(original);
        var record = new CaptureRecord(original.getUUID(), 1, UUID.randomUUID(), Phase.CAPTURE_PREPARED, UUID.randomUUID(), UUID.randomUUID(), "minecraft:cow", 1, "Cow", "", "", payload)
                .phase(Phase.STORED, "").phase(Phase.RELEASE_PREPARED, "").phase(Phase.RELEASED, "").compact();
        var data = CaptureSavedData.get(h.getLevel().getServer()); check(data.commit(record), "test ledger failed");
        check(!h.getLevel().addFreshEntity(original), "missing generation joined");
        check(AnimalTrustService.generation(original, 0, 1), "generation fixture failed");
        var pos = h.absolutePos(new BlockPos(4, 1, 5)); original.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
        check(h.getLevel().addFreshEntity(original), "released generation refused");
        var duplicate = EntityType.COW.create(h.getLevel()); var saved = new CompoundTag(); original.saveWithoutId(saved); duplicate.load(saved);
        check(!h.getLevel().addFreshEntity(duplicate) && h.getLevel().getEntity(record.animal()) == original, "duplicate incarnation joined");
        duplicate.discard(); original.discard(); h.succeed();
    }
}
