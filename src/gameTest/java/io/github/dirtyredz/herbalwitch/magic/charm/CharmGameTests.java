package io.github.dirtyredz.herbalwitch.magic.charm;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.equipment.CharmEquipment;
import io.github.dirtyredz.herbalwitch.magic.charm.equipment.CharmEvents;
import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Charms worn in the off hand: a sachet softens any mob's blow, an amulet only its family's, the
 * charge runs down by the second while worn and the charm breaks when spent. Fake players neither tick
 * nor take damage, so the tests drive the wear seam and the damage handler directly.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CharmGameTests {
    private static final BlockPos FEET = new BlockPos(4, 1, 2);

    private CharmGameTests() {
    }

    private static float blow(ServerPlayer wearer, net.minecraft.world.entity.LivingEntity attacker, float amount) {
        LivingDamageEvent event = new LivingDamageEvent(wearer, attacker.damageSources().mobAttack(attacker), amount);
        CharmEvents.onDamage(event);
        return event.getAmount();
    }

    @GameTest(template = TEMPLATE)
    public static void aSachetSoftensAnyMobAndAnAmuletOnlyItsFamily(GameTestHelper helper) {
        floor(helper);
        ServerPlayer wearer = standing(helper, "m7_charm_wearer", FEET);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 1, 5));
        zombie.setNoAi(true);
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(1, 1, 6));
        spider.setNoAi(true);
        check(blow(wearer, zombie, 10.0F) == 10.0F, "an unprotected blow was softened");
        wearer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(CharmItems.item(CharmCatalog.PROTECTIVE_SACHET)));
        check(CharmEquipment.active(wearer).isPresent(), "an off-hand charm is not active");
        check(blow(wearer, zombie, 10.0F) == 8.5F, "the sachet left " + blow(wearer, zombie, 10.0F));
        check(blow(wearer, zombie, 40.0F) == 38.0F, "the sachet's cap does not hold");
        wearer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(CharmItems.item(CharmCatalog.UNDEAD_AMULET)));
        check(blow(wearer, zombie, 10.0F) == 7.0F, "the undead amulet left " + blow(wearer, zombie, 10.0F));
        check(blow(wearer, spider, 10.0F) == 10.0F, "the undead amulet softened a spider");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aWornCharmSpendsItsChargeAndBreaks(GameTestHelper helper) {
        floor(helper);
        ServerPlayer wearer = standing(helper, "m7_charm_spender", FEET);
        ItemStack charm = new ItemStack(CharmItems.item(CharmCatalog.PROTECTIVE_SACHET));
        wearer.setItemInHand(InteractionHand.OFF_HAND, charm);
        int capacity = ProtectiveCharmItem.capacity();
        check(!CharmEvents.wear(wearer, charm, CharmEvents.TICKS_PER_CHARGE - 1) && ProtectiveCharmItem.used(charm) == 0,
                "spent before a whole second");
        check(!CharmEvents.wear(wearer, charm, 1) && ProtectiveCharmItem.used(charm) == 1, "a second did not count");
        check(!CharmEvents.wear(wearer, charm, CharmEvents.TICKS_PER_CHARGE * (capacity - 2)), "broke early");
        check(ProtectiveCharmItem.remaining(charm) == 1, "remaining " + ProtectiveCharmItem.remaining(charm));
        check(CharmEvents.wear(wearer, charm, CharmEvents.TICKS_PER_CHARGE) && charm.isEmpty(), "a spent charm did not break");
        check(CharmEvents.counter(wearer) == 0 && CharmEquipment.active(wearer).isEmpty(), "the broken charm still counts");
        helper.succeed();
    }
}
