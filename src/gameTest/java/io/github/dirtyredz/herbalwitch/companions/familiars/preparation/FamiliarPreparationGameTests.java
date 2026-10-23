package io.github.dirtyredz.herbalwitch.companions.familiars.preparation;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.*;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.HarmAttribution;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.gametest.*;
import java.util.UUID;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarPreparationGameTests {
    private static InteractionResult apply(FamiliarFixtures.Bound b, ProductSpec product) {
        var stack = new ItemStack(AdvancedPreparations.item(product)); b.owner().setItemInHand(InteractionHand.MAIN_HAND, stack);
        return stack.getItem().interactLivingEntity(stack, b.owner(), b.mob(), InteractionHand.MAIN_HAND);
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_preparations", timeoutTicks = 100)
    public static void allTenProductsApplyOnceWithoutEnergyAndExpireWithoutStacking(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "preparations_" + UUID.randomUUID(), b -> {
            float health = b.mob().getHealth(), maximum = b.mob().getMaxHealth(); double speed = b.mob().getAttributeValue(Attributes.MOVEMENT_SPEED);
            for (var product : FamiliarPreparationCatalog.ALL) {
                check(apply(b, product) == InteractionResult.CONSUME && b.owner().getMainHandItem().is(Items.GLASS_BOTTLE), "product/container failed: " + product.id());
                check(b.record().state().getCompound("buffs").getInt(product.id()) == 3600, "wrong timer: " + product.id());
                check(apply(b, product) == InteractionResult.FAIL && b.owner().getMainHandItem().getCount() == 1, "duplicate application spent: " + product.id());
            }
            check(b.mob().getHealth() == health && b.mob().getMaxHealth() == maximum + 4, "guardian healed or stacked");
            check(Math.abs(b.mob().getAttributeValue(Attributes.MOVEMENT_SPEED) - speed * 1.2) < .00001, "swiftpaw speed wrong");
            check(b.mob().hasEffect(PreparationEffects.CONCEALMENT.get()) && b.mob().hasEffect(MobEffects.FIRE_RESISTANCE), "shared effect projections missing");
            check(WitchStatusService.energyUnits(b.owner()) == 7500, "finished preparations charged energy");
            var magic = new LivingDamageEvent(b.mob(), b.mob().damageSources().magic(), 10); FamiliarBuffEvents.damage(magic);
            check(magic.getAmount() == 8, "warding did not reduce magic exactly once");
            var ordinary = new LivingDamageEvent(b.mob(), b.mob().damageSources().generic(), 10); FamiliarBuffEvents.damage(ordinary);
            check(ordinary.getAmount() == 10, "warding reduced unrelated damage");
            b.mob().setHealth(7);
            b.owner().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems.product(EverydayProductCatalog.HEALING_SALVE)));
            check(io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareService.heal(b.owner(), b.mob(), InteractionHand.MAIN_HAND)
                    == io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess.Reason.READY
                    && b.owner().getMainHandItem().is(Items.BOWL) && b.record().state().getInt("xp") == 3, "salve or spiritbond care contribution wrong");
            b.mob().addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600));
            var store = FamiliarStore.get(b.owner().server); store.update(b.record().change(s -> {
                var buffs = s.getCompound("buffs"); for (var key : FamiliarBuffRules.PRODUCTS) buffs.putInt(key, 1); s.put("buffs", buffs);
            }), false);
            FamiliarBuffEvents.tick(new LivingEvent.LivingTickEvent(b.mob()));
            check(b.record().state().getCompound("buffs").isEmpty() && b.mob().getMaxHealth() == maximum && b.mob().getHealth() <= maximum, "expiry left buffs or unsafe health");
            check(b.mob().hasEffect(MobEffects.FIRE_RESISTANCE) && !b.mob().hasEffect(PreparationEffects.CONCEALMENT.get()), "expiry removed an external effect or kept owned concealment"); h.succeed();
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_poison", timeoutTicks = 100)
    public static void poisonCoatingsShareOneSlotAndRecheckDelayedProtection(GameTestHelper h) {
        FamiliarFixtures.bound(h, "snake", "poison_" + UUID.randomUUID(), b -> {
            check(apply(b, FamiliarPreparationCatalog.VENOMLEAF) == InteractionResult.CONSUME, "venomleaf refused");
            check(apply(b, AdvancedProductCatalog.VENOM_OIL) == InteractionResult.FAIL, "weak oil replaced venomleaf");
            var target = h.spawn(EntityType.PILLAGER, new BlockPos(7,1,5)); target.setNoAi(true);
            target.hurt(b.mob().damageSources().mobAttack(b.mob()), 2);
            check(target.hasEffect(MobEffects.POISON) && HarmAttribution.actor(target).orElseThrow().equals(b.owner().getUUID()), "shared poison did not retain owner attribution");
            var pig = h.spawn(EntityType.PIG, new BlockPos(8,1,5));
            pig.hurt(b.mob().damageSources().mobAttack(b.mob()), 2); check(!pig.hasEffect(MobEffects.POISON), "familiar poisoned a passive animal");
            boolean[] blocked = {true};
            io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission.register((level, owner, position) ->
                    blocked[0] && position.equals(target.blockPosition()) ? io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.Denial.CLAIMS
                            : io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.Denial.NONE);
            var tick = new LivingHurtEvent(target, target.damageSources().magic(), 1); HarmAttribution.onHurt(tick);
            blocked[0] = false;
            check(tick.isCanceled() && !target.hasEffect(MobEffects.POISON), "delayed familiar poison bypassed new protection");
            var store = FamiliarStore.get(b.owner().server); store.update(b.record().change(s -> s.remove("buffs")), false);
            check(apply(b, AdvancedProductCatalog.VENOM_OIL) == InteractionResult.CONSUME && b.record().state().getCompound("poisonCoating").getInt("hits") == 8, "ordinary oil did not share the familiar coating slot");
            for (int i = 0; i < 8; i++) FamiliarPoison.damage(new LivingDamageEvent(target, b.mob().damageSources().mobAttack(b.mob()), 2));
            check(!b.record().state().contains("poisonCoating"), "ordinary oil survived eight successful permitted hits"); h.succeed();
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_shadowstep", timeoutTicks = 100)
    public static void shadowstepKeepsItsChargeWhenNoSafePositionExists(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "shadowstep_" + UUID.randomUUID(), b -> {
            check(apply(b, FamiliarPreparationCatalog.SHADOWSTEP) == InteractionResult.CONSUME, "shadowstep refused");
            b.mob().moveTo(h.absolutePos(new BlockPos(4,5,5)).getCenter()); b.mob().setRemainingFireTicks(100);
            FamiliarBuffEvents.tick(new LivingEvent.LivingTickEvent(b.mob()));
            check(FamiliarBuffRules.active(b.record().state(), "shadowstep"), "failed destination consumed charge");
            b.mob().moveTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,5)))); var before = b.mob().position();
            FamiliarBuffEvents.tick(new LivingEvent.LivingTickEvent(b.mob()));
            check(!FamiliarBuffRules.active(b.record().state(), "shadowstep") && b.mob().position().distanceToSqr(before) > .1
                    && b.mob().position().distanceToSqr(before) <= 64, "successful step failed bounds or kept charge"); h.succeed();
        });
    }
}
