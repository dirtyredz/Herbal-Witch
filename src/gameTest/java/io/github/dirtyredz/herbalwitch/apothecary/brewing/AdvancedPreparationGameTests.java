package io.github.dirtyredz.herbalwitch.apothecary.brewing;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.FamiliarPreparationApplications;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.OilApplication;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ThrowableApplications;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ThrowableApplications.Burst;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat.CombatEvents;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat.WeaponCoating;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEvents;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.PreparationImpact;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.ProjectileLimits;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Milestone 7 preparations in the world: draughts and their bottle, a venom coating on a cow and its
 * spent hit, vials that spare players without preparation PvP and reach mobs in line of sight, binding
 * and sleep with their immunity, a repellent keeping the undead from choosing its wearer, familiar
 * preparations refusing without a bonded familiar, and the per-thrower projectile limit.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdvancedPreparationGameTests {
    private static final BlockPos FEET = new BlockPos(4, 1, 2);

    private AdvancedPreparationGameTests() {
    }

    private static Zombie zombie(GameTestHelper helper, BlockPos at) {
        Zombie z = helper.spawn(EntityType.ZOMBIE, at);
        z.setNoAi(true);
        return z;
    }

    @GameTest(template = TEMPLATE)
    public static void aDraughtGrantsItsEffectAndReturnsItsBottle(GameTestHelper helper) {
        floor(helper);
        ServerPlayer drinker = standing(helper, "m7_drinker", FEET);
        ItemStack draught = new ItemStack(AdvancedPreparations.item(AdvancedProductCatalog.NIGHT_VISION));
        drinker.setItemInHand(InteractionHand.MAIN_HAND, draught);
        ItemStack left = draught.getItem().finishUsingItem(draught, helper.getLevel(), drinker);
        check(drinker.hasEffect(MobEffects.NIGHT_VISION), "no night vision");
        check(left.is(Items.GLASS_BOTTLE), "the bottle did not come back: " + left);
        net.minecraft.world.item.Item healingItem = AdvancedPreparations.item(AdvancedProductCatalog.HEALING);
        ItemStack healing = new ItemStack(healingItem);
        check(healingItem.finishUsingItem(healing, helper.getLevel(), drinker).is(healingItem), "healing was drunk at full health");
        drinker.setHealth(10.0F);
        healingItem.finishUsingItem(healing, helper.getLevel(), drinker);
        check(drinker.getHealth() == 16.0F, "healing restored to " + drinker.getHealth());
        check(drinker.getCooldowns().isOnCooldown(healingItem), "healing has no cooldown");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aVenomCoatingPoisonsACowAndSpendsAHit(GameTestHelper helper) {
        floor(helper);
        ServerPlayer hunter = standing(helper, "m7_hunter", FEET);
        hunter.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_SWORD));
        check(OilApplication.apply(hunter, OilApplication.Oil.VENOM), "the blade was not coated");
        ItemStack blade = hunter.getOffhandItem();
        hunter.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        hunter.setItemInHand(InteractionHand.MAIN_HAND, blade);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(4, 1, 4));
        CombatEvents.onDamage(new LivingDamageEvent(cow, hunter.damageSources().playerAttack(hunter), 1.0F));
        check(cow.hasEffect(MobEffects.POISON), "the cow was not poisoned");
        long now = helper.getLevel().getGameTime();
        check(WeaponCoating.read(blade, now).map(c -> c.hits() == WeaponCoating.HITS - 1).orElse(false), "a hit was not spent");
        ItemStack bare = new ItemStack(Items.STICK);
        hunter.setItemInHand(InteractionHand.OFF_HAND, bare);
        check(!OilApplication.apply(hunter, OilApplication.Oil.VENOM), "a stick took a coating");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void vialsSparePlayersWithoutPvpAndReachMobsInSight(GameTestHelper helper) {
        floor(helper);
        ServerPlayer thrower = standing(helper, "m7_thrower", FEET);
        ServerPlayer bystander = standing(helper, "m7_bystander", FEET.east(2));
        check(!ThrowableApplications.affect(Burst.POISON, thrower, bystander), "poison reached a player without preparation PvP");
        check(ThrowableApplications.affect(Burst.CONCEALMENT, thrower, bystander), "concealment is a gift to players");
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(1, 1, 7));
        check(!ThrowableApplications.affect(Burst.CONCEALMENT, thrower, cow), "concealment reached a wild animal");
        Zombie near = zombie(helper, new BlockPos(4, 1, 5));
        Zombie far = zombie(helper, new BlockPos(4, 1, 8));
        Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 4)));
        List<?> hit = PreparationImpact.burst(helper.getLevel(), at, Burst.SLOWING, thrower, null);
        check(near.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the near zombie was not slowed");
        check(!far.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "a zombie out of the burst was slowed");
        check(!bystander.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the bystander was slowed");
        check(hit.size() <= PreparationImpact.MAX_TARGETS, "the burst is unbounded");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void bindingAndSleepHoldOnceThenGrantImmunity(GameTestHelper helper) {
        floor(helper);
        ServerPlayer binder = standing(helper, "m7_binder", FEET);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 1, 5));
        check(ThrowableApplications.affect(Burst.SLEEP, binder, z), "sleep did not land");
        check(!ThrowableApplications.affect(Burst.BINDING, binder, z), "a second control stacked");
        LivingAttackEvent attack = new LivingAttackEvent(binder, z.damageSources().mobAttack(z), 2.0F);
        ControlEvents.onAttack(attack);
        check(attack.isCanceled(), "a sleeper attacked");
        check(!ThrowableApplications.affect(Burst.BINDING, binder, binder), "a player was held");
        z.hurt(binder.damageSources().playerAttack(binder), 1.0F);
        check(!z.hasEffect(PreparationEffects.SLEEP.get()), "a blow did not wake the sleeper");
        helper.runAfterDelay(2, () -> {
            check(z.hasEffect(PreparationEffects.STEADFAST.get()), "no immunity after waking");
            check(!ControlEffects.controllable(z), "control could land again at once");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void aRepellentKeepsTheUndeadFromChoosingItsWearer(GameTestHelper helper) {
        floor(helper);
        ServerPlayer wearer = standing(helper, "m7_wearer", FEET);
        Zombie z = zombie(helper, new BlockPos(4, 1, 5));
        z.setTarget(wearer);
        check(z.getTarget() == wearer, "the zombie could not choose the wearer beforehand");
        check(OilApplication.apply(wearer, OilApplication.Oil.UNDEAD_REPELLENT), "the repellent was not applied");
        check(z.getTarget() == null, "applying did not clear the existing target");
        z.setTarget(wearer);
        check(z.getTarget() == null, "the undead chose a wearer of their repellent");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aFamiliarPreparationRefusesWithoutABondedFamiliar(GameTestHelper helper) {
        floor(helper);
        ServerPlayer keeper = standing(helper, "m7_keeper", FEET);
        ItemStack tonic = new ItemStack(AdvancedPreparations.item(FamiliarPreparationCatalog.GUARDIAN));
        keeper.setItemInHand(InteractionHand.MAIN_HAND, tonic);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(4, 1, 4));
        check(!FamiliarPreparationApplications.available(), "a familiar consumer is installed before familiars exist");
        InteractionResult result = tonic.getItem().interactLivingEntity(tonic, keeper, cow, InteractionHand.MAIN_HAND);
        check(result == InteractionResult.FAIL && keeper.getMainHandItem().getCount() == 1, "the tonic was used up: " + result);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aThrowerHasAtMostFourVialsInFlight(GameTestHelper helper) {
        floor(helper);
        ServerPlayer lobber = standing(helper, "m7_lobber", FEET);
        ItemStack vials = new ItemStack(AdvancedPreparations.item(AdvancedProductCatalog.SLOWING), 8);
        lobber.setItemInHand(InteractionHand.MAIN_HAND, vials);
        for (int i = 0; i < ProjectileLimits.PER_THROWER + 1; i++) {
            vials.getItem().use(helper.getLevel(), lobber, InteractionHand.MAIN_HAND);
        }
        check(ProjectileLimits.owned(lobber.getUUID()) == ProjectileLimits.PER_THROWER,
                ProjectileLimits.owned(lobber.getUUID()) + " vials in flight");
        check(vials.getCount() == 8 - ProjectileLimits.PER_THROWER, "the refused throw consumed a vial");
        helper.succeedWhen(() -> check(ProjectileLimits.owned(lobber.getUUID()) == 0, "vials still counted after landing"));
    }
}
