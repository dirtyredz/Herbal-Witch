package io.github.dirtyredz.herbalwitch.magic.ritual;

import io.github.dirtyredz.herbalwitch.magic.witch.status.StatusFixtures;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.AttunementAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.FoundingAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.InitiationAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.MeditationAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationConsent;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationOffer;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingRecord;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.FRONT;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.advance;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.altar;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.fill;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.foundingOffering;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.initiationOffering;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.open;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.session;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Altar operations end to end on real menus. Each ritual runs synchronously inside its test (the
 * world's founding record is shared by every test): solo founding converts once and closes the world,
 * a failed durable persist or a closed menu consumes nothing, consenting initiation charges the
 * initiator exactly once and gives the recipient the grimoire, a full inventory blocks completion
 * without consuming, a designation is accepted without cost, meditation settles then restores and
 * stops when full, and attunement trades sage and energy for practice.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AltarActionGameTests {
    private AltarActionGameTests() {
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }

    private static int count(ServerPlayer p, net.minecraft.world.item.Item item) {
        return p.getInventory().countItem(item);
    }

    @GameTest(template = TEMPLATE)
    public static void aLoneCandidateFoundsTheFirstLineageOnce(GameTestHelper helper) {
        altar(helper);
        FoundingService.resetForTest(server(helper));
        ServerPlayer founder = player(helper, "aa_founder", FRONT);
        foundingOffering(founder);
        AltarMenu menu = open(helper, founder);
        check(menu != null, "the altar did not open");
        check(FoundingAction.begin(founder, menu).equals("founding.begun"), "founding did not begin");
        AltarSession s = session(founder);
        check(FoundingService.data(server(helper)).record().state() == FoundingRecord.State.CLAIMED, "the world claim is held");
        advance(s, WitchRules.server().foundingTicks() - 1);
        check(s.running() && !WitchStatusService.isWitch(founder), "completed early");
        advance(s, 1);
        check(!s.running() && s.state() == AltarSession.State.COMPLETED, "did not complete: " + s.state());
        check(WitchStatusService.isWitch(founder) && WitchStatusService.energyUnits(founder) == 10_000, "not a full witch");
        check(count(founder, Items.AMETHYST_SHARD) == 0 && count(founder, Items.WRITTEN_BOOK) == 0
                && count(founder, BotanyItems.fresh(HerbCatalog.MINT)) == 0, "the offering was not consumed");
        check(count(founder, GrimoireItems.BOOK.get()) == 1, "exactly one grimoire given");
        check(FoundingService.data(server(helper)).record().state() == FoundingRecord.State.COMPLETE, "founding not closed");
        check(DiscoveryService.knows(founder, AltarEntries.key(AltarActionSpec.FOUNDING)), "founding page not discovered");
        ServerPlayer late = player(helper, "aa_late", FRONT.east());
        foundingOffering(late);
        check(FoundingAction.begin(late, open(helper, late)).equals("founding.closed"), "a second founding was allowed");
        check(count(late, Items.AMETHYST_SHARD) == 1, "the refused candidate lost items");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aFailedDurablePersistAndAClosedMenuConsumeNothing(GameTestHelper helper) {
        altar(helper);
        FoundingService.resetForTest(server(helper));
        ServerPlayer candidate = player(helper, "aa_unlucky", FRONT);
        foundingOffering(candidate);
        AltarMenu menu = open(helper, candidate);
        check(FoundingAction.begin(candidate, menu).equals("founding.begun"), "founding did not begin");
        AltarSession s = session(candidate);
        FoundingService.interceptNextPersist(() -> false);
        advance(s, WitchRules.server().foundingTicks());
        check(!WitchStatusService.isWitch(candidate), "converted although the claim could not be persisted");
        check(count(candidate, Items.AMETHYST_SHARD) == 1 && count(candidate, Items.WRITTEN_BOOK) == 1, "items consumed after a failed persist");
        check(FoundingService.data(server(helper)).record().state() == FoundingRecord.State.OPEN, "founding did not reopen");
        // a second attempt is interrupted by closing the altar menu
        menu = open(helper, candidate);
        check(FoundingAction.begin(candidate, menu).equals("founding.begun"), "the retry did not begin");
        AltarSession retry = session(candidate);
        advance(retry, 20);
        candidate.closeContainer();
        advance(retry, 1);
        check(retry.state() == AltarSession.State.CANCELLED, "closing the menu did not cancel: " + retry.state());
        check(count(candidate, Items.AMETHYST_SHARD) == 1 && !WitchStatusService.isWitch(candidate), "a cancelled founding consumed something");
        check(FoundingService.data(server(helper)).record().state() == FoundingRecord.State.OPEN, "the cancelled claim was not released");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aWitchInitiatesAConsentingPlayerOnce(GameTestHelper helper) {
        altar(helper);
        ServerPlayer initiator = witch(helper, "aa_teacher", FRONT);
        ServerPlayer recipient = player(helper, "aa_student", FRONT.east());
        initiationOffering(initiator);
        AltarMenu theirs = open(helper, initiator);
        check(InitiationAction.invite(initiator, theirs, recipient.getUUID()).equals("consent.sent"), "invite failed");
        InitiationOffer offer = InitiationEvents.consent().orElseThrow().incoming(recipient.getUUID()).orElseThrow();
        AltarMenu mine = open(helper, recipient);
        check(InitiationAction.accept(recipient, mine, offer.id() + 1).startsWith("consent.no_invitation"), "a spoofed id was accepted");
        check(InitiationAction.accept(recipient, mine, offer.id()).equals("initiation.begun"), "acceptance did not begin");
        check(InitiationAction.accept(recipient, mine, offer.id()).equals("consent.no_invitation"), "a duplicate acceptance was not refused");
        AltarSession s = session(recipient);
        advance(s, WitchRules.server().initiationTicks());
        check(s.state() == AltarSession.State.COMPLETED && WitchStatusService.isWitch(recipient), "not converted: " + s.state());
        check(WitchStatusService.energyUnits(initiator) == 10_000 - 5_000, "the initiator paid " + WitchStatusService.energyUnits(initiator));
        check(WitchCapability.of(initiator).orElseThrow().state().practiceSpent() == 5_000, "the initiator's practice");
        check(WitchStatusService.energyUnits(recipient) == 10_000, "the recipient paid nothing and starts full");
        check(count(initiator, Items.AMETHYST_SHARD) == 0 && count(initiator, BotanyItems.fresh(HerbCatalog.SAGE)) == 0, "offering kept");
        check(count(recipient, GrimoireItems.BOOK.get()) == 1 && count(initiator, GrimoireItems.BOOK.get()) == 0, "the gift went astray");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aFullInventoryBlocksInitiationWithoutConsuming(GameTestHelper helper) {
        altar(helper);
        ServerPlayer initiator = witch(helper, "aa_teacher_full", FRONT);
        ServerPlayer recipient = player(helper, "aa_student_full", FRONT.east());
        initiationOffering(initiator);
        AltarMenu theirs = open(helper, initiator);
        check(InitiationAction.invite(initiator, theirs, recipient.getUUID()).equals("consent.sent"), "invite failed");
        long id = InitiationEvents.consent().orElseThrow().incoming(recipient.getUUID()).orElseThrow().id();
        AltarMenu mine = open(helper, recipient);
        check(InitiationAction.accept(recipient, mine, id).equals("initiation.begun"), "acceptance did not begin");
        fill(recipient);
        AltarSession s = session(recipient);
        advance(s, WitchRules.server().initiationTicks());
        check(s.state() == AltarSession.State.CANCELLED && !WitchStatusService.isWitch(recipient), "converted without room for the gift");
        check(count(initiator, Items.AMETHYST_SHARD) == 1 && WitchStatusService.energyUnits(initiator) == 10_000, "consumed while blocked");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anOperatorDesignationIsAcceptedWithoutCost(GameTestHelper helper) {
        altar(helper);
        ServerPlayer recipient = player(helper, "aa_designated", FRONT);
        check(InitiationEvents.designate(recipient) == InitiationConsent.Result.DESIGNATED, "designation refused");
        long id = InitiationEvents.consent().orElseThrow().incoming(recipient.getUUID()).orElseThrow().id();
        check(!WitchStatusService.isWitch(recipient), "designation alone converted");
        AltarMenu mine = open(helper, recipient);
        check(InitiationAction.accept(recipient, mine, id).equals("initiation.begun"), "acceptance did not begin");
        advance(session(recipient), WitchRules.server().initiationTicks());
        check(WitchStatusService.isWitch(recipient) && count(recipient, GrimoireItems.BOOK.get()) == 1, "designation not completed");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void meditationSettlesRestoresAndStopsWhenFull(GameTestHelper helper) {
        altar(helper);
        ServerPlayer w = witch(helper, "aa_meditating", FRONT);
        StatusFixtures.debit(w, 300);
        AltarMenu menu = open(helper, w);
        check(MeditationAction.begin(w, menu).equals("meditation.begun"), "meditation did not begin");
        AltarSession s = session(w);
        check(WitchCapability.of(w).orElseThrow().meditating(), "passive rest still runs during meditation");
        advance(s, WitchRules.server().energy().meditationSettle());
        check(WitchStatusService.energyUnits(w) == 9_700, "granted while settling");
        advance(s, WitchRules.server().energy().meditationInterval() * 2);
        check(!s.running() && WitchStatusService.energyUnits(w) == 10_000, "did not refill and stop: " + WitchStatusService.energyUnits(w));
        check(!WitchCapability.of(w).orElseThrow().meditating(), "meditation flag left on");
        check(MeditationAction.begin(w, menu).equals("meditation.full"), "a full witch began meditating");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void attunementTradesSageAndEnergyForPractice(GameTestHelper helper) {
        altar(helper);
        ServerPlayer w = witch(helper, "aa_attuning", FRONT);
        AltarMenu menu = open(helper, w);
        check(AttunementAction.begin(w, menu).equals("missing_offering"), "attuned without sage");
        io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.carry(w, new net.minecraft.world.item.ItemStack(BotanyItems.fresh(HerbCatalog.SAGE), 2));
        check(AttunementAction.begin(w, menu).equals("attunement.begun"), "attunement did not begin");
        advance(session(w), WitchRules.server().attunementTicks());
        check(count(w, BotanyItems.fresh(HerbCatalog.SAGE)) == 1, "exactly one sage used");
        check(WitchStatusService.energyUnits(w) == 9_000, "attunement cost " + WitchStatusService.energyUnits(w));
        check(WitchCapability.of(w).orElseThrow().state().practiceSpent() == 1_000, "practice not awarded");
        check(AltarSessionService.of(w.getUUID()).isEmpty(), "the session did not end");
        helper.succeed();
    }
}
