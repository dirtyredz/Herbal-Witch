package io.github.dirtyredz.herbalwitch.preparations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata.PreparationMetadata;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat.WeaponCoating;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Registry-driven cases captured before merging applications and item adapters. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PreparationContractGameTests {
    private record Use(ProductSpec product, boolean advanced, MobEffect effect, int ticks, UseAnim animation) {
        Item item() { return advanced ? AdvancedPreparations.item(product) : PreparationItems.product(product); }
    }

    @GameTest(template = TEMPLATE)
    public static void everyTimedDrinkAndOilKeepsItsEffectContainerAndLabel(GameTestHelper helper) {
        List<Use> cases = List.of(
                new Use(EverydayProductCatalog.RECOVERY_TEA, false, MobEffects.REGENERATION, 1800, UseAnim.DRINK),
                new Use(EverydayProductCatalog.WARMTH_REMEDY, false, PreparationEffects.WARMTH.get(), 1800, UseAnim.DRINK),
                new Use(EverydayProductCatalog.COOLING_REMEDY, false, MobEffects.FIRE_RESISTANCE, 1200, UseAnim.DRINK),
                new Use(EverydayProductCatalog.MINING_TONIC, false, MobEffects.DIG_SPEED, 1800, UseAnim.DRINK),
                new Use(EverydayProductCatalog.TRAVEL_TONIC, false, MobEffects.MOVEMENT_SPEED, 1800, UseAnim.DRINK),
                new Use(AdvancedProductCatalog.NIGHT_VISION, true, MobEffects.NIGHT_VISION, 2400, UseAnim.DRINK),
                new Use(AdvancedProductCatalog.WATER_BREATHING, true, MobEffects.WATER_BREATHING, 2400, UseAnim.DRINK),
                new Use(AdvancedProductCatalog.RESISTANCE, true, MobEffects.DAMAGE_RESISTANCE, 1800, UseAnim.DRINK),
                new Use(EverydayProductCatalog.FISHING_BLEND, false, MobEffects.LUCK, 1800, UseAnim.BRUSH),
                new Use(EverydayProductCatalog.PEST_REPELLENT, false, PreparationEffects.PEST_REPELLENT.get(), 1800, UseAnim.BRUSH),
                new Use(AdvancedProductCatalog.THORN_OIL, true, PreparationEffects.THORNS.get(), 1800, UseAnim.BRUSH),
                new Use(AdvancedProductCatalog.UNDEAD_REPELLENT, true, PreparationEffects.UNDEAD_REPELLENT.get(), 1800, UseAnim.BRUSH),
                new Use(AdvancedProductCatalog.ARTHROPOD_REPELLENT, true, PreparationEffects.ARTHROPOD_REPELLENT.get(), 1800, UseAnim.BRUSH));
        for (var mode : new GameType[]{GameType.SURVIVAL, GameType.CREATIVE}) {
            for (Use row : cases) {
                var user = player(helper, "contract_" + mode.getId() + row.product().id(), mode, Direction.NORTH);
                user.removeAllEffects();
                var item = row.item();
                var stack = PreparationMetadata.labelled(new ItemStack(item, 2));
                stack.getOrCreateTag().putInt("Purity", 3);
                user.setItemInHand(InteractionHand.MAIN_HAND, stack);
                check(item.getUseDuration(stack) == 32 && item.getUseAnimation(stack) == row.animation(), "use shape " + row.product().id());
                check(item.use(helper.getLevel(), user, InteractionHand.MAIN_HAND).getResult().consumesAction(), "start " + row.product().id());
                var left = item.finishUsingItem(stack, helper.getLevel(), user);
                var effect = user.getEffect(row.effect());
                check(effect != null && effect.getDuration() == row.ticks() && effect.getAmplifier() == 0, "effect " + row.product().id());
                check(left.getCount() == (mode == GameType.CREATIVE ? 2 : 1), "consumption " + row.product().id());
                check(user.getInventory().countItem(Items.GLASS_BOTTLE) == (mode == GameType.CREATIVE ? 0 : 1), "container " + row.product().id());
                check(left.getTag().getInt("Purity") == 3 && PreparationMetadata.isLabelled(left), "metadata changed");
                var lines = new ArrayList<Component>();
                item.appendHoverText(left, helper.getLevel(), lines, TooltipFlag.NORMAL);
                check(lines.size() == (row.advanced() ? 2 : 1), "label policy " + row.product().id());
                if (row.advanced()) check(((TranslatableContents) lines.get(1).getContents()).getKey().equals(PreparationMetadata.LABEL_KEY), "label key");
                user.stopUsingItem();
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void teaRestAndHealingKeepBothUseBoundaries(GameTestHelper helper) {
        var user = player(helper, "special_drink_contract", GameType.SURVIVAL, Direction.NORTH);
        var tea = PreparationItems.product(EverydayProductCatalog.ENERGY_TEA);
        var stack = new ItemStack(tea, 2);
        user.setItemInHand(InteractionHand.MAIN_HAND, stack);
        user.getFoodData().setFoodLevel(10);
        check(tea.use(helper.getLevel(), user, InteractionHand.MAIN_HAND).getResult().consumesAction(), "tea start");
        tea.finishUsingItem(stack, helper.getLevel(), user);
        check(user.getFoodData().getFoodLevel() == 14 && stack.getCount() == 1, "tea food/consumption");
        check(tea.use(helper.getLevel(), user, InteractionHand.MAIN_HAND).getResult() == InteractionResult.FAIL, "tea cooldown at start");
        check(tea.finishUsingItem(stack, helper.getLevel(), user) == stack && stack.getCount() == 1, "tea cooldown at finish");
        user.getStats().setValue(user, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 100000);
        var rest = PreparationItems.product(EverydayProductCatalog.RESTFUL_INFUSION);
        check(rest.finishUsingItem(new ItemStack(rest), helper.getLevel(), user).is(Items.GLASS_BOTTLE), "rest bottle");
        check(user.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) == 0, "rest stat");
        var healing = AdvancedPreparations.item(AdvancedProductCatalog.HEALING);
        var dose = new ItemStack(healing);
        user.setItemInHand(InteractionHand.MAIN_HAND, dose);
        user.setHealth(user.getMaxHealth());
        check(healing.use(helper.getLevel(), user, InteractionHand.MAIN_HAND).getResult() == InteractionResult.FAIL, "healing at full health");
        user.setHealth(10);
        check(healing.use(helper.getLevel(), user, InteractionHand.MAIN_HAND).getResult().consumesAction(), "hurt healing start");
        user.setHealth(user.getMaxHealth());
        check(healing.finishUsingItem(dose, helper.getLevel(), user) == dose && dose.getCount() == 1, "healing finish recheck");
        user.setHealth(10);
        check(healing.finishUsingItem(dose, helper.getLevel(), user).is(Items.GLASS_BOTTLE), "healing bottle");
        check(user.getHealth() == 16 && user.getCooldowns().isOnCooldown(healing), "healing amount/cooldown");
        user.stopUsingItem();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void venomKeepsItsHandAndFinishWeaponChecksAndLanternIsPourOnly(GameTestHelper helper) {
        var user = player(helper, "special_oil_contract", GameType.SURVIVAL, Direction.NORTH);
        var venom = AdvancedPreparations.item(AdvancedProductCatalog.VENOM_OIL);
        var stack = new ItemStack(venom);
        user.setItemInHand(InteractionHand.MAIN_HAND, stack);
        check(venom.use(helper.getLevel(), user, InteractionHand.MAIN_HAND).getResult() == InteractionResult.FAIL, "venom without blade");
        user.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_SWORD));
        check(venom.use(helper.getLevel(), user, InteractionHand.OFF_HAND).getResult() == InteractionResult.FAIL, "venom off-hand start");
        check(venom.use(helper.getLevel(), user, InteractionHand.MAIN_HAND).getResult().consumesAction(), "venom start");
        user.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        check(venom.finishUsingItem(stack, helper.getLevel(), user) == stack && stack.getCount() == 1, "venom finish recheck");
        user.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_SWORD));
        check(venom.finishUsingItem(stack, helper.getLevel(), user).is(Items.GLASS_BOTTLE), "venom bottle");
        check(WeaponCoating.read(user.getOffhandItem(), helper.getLevel().getGameTime()).isPresent(), "venom coating");
        var lantern = PreparationItems.product(EverydayProductCatalog.LANTERN_OIL);
        var fuel = new ItemStack(lantern);
        user.setItemInHand(InteractionHand.MAIN_HAND, fuel);
        check(lantern.use(helper.getLevel(), user, InteractionHand.MAIN_HAND).getResult() == InteractionResult.PASS, "lantern self-use");
        check(lantern.finishUsingItem(fuel, helper.getLevel(), user) == fuel && fuel.getCount() == 1, "lantern consumed");
        check(lantern.getUseDuration(fuel) == 0 && lantern.getUseAnimation(fuel) == UseAnim.NONE, "lantern animation");
        user.stopUsingItem();
        helper.succeed();
    }
}
