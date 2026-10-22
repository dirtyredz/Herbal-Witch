package io.github.dirtyredz.herbalwitch.grimoire;

import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenu;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching.TeachingEvents;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching.TeachingService;
import io.github.dirtyredz.herbalwitch.grimoire.network.TeachingRequest;
import io.github.dirtyredz.herbalwitch.grimoire.network.TeachingUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.capture;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.content;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.moveTo;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.openBlock;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.openBook;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.place;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.recipe;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Teaching between two distinct server players through the real request handling: consent,
 * refusal, range, dimension change, a spoofed and a replayed acceptance, a reload that changes the
 * recipe, and logout. Fake players prove the authority and lifecycle rules, not two real clients.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GrimoireTeachingGameTests {
    private static final EntryKey TEA = recipe("processing/steeping/recovery_tea");

    private GrimoireTeachingGameTests() {
    }

    /** A teacher who knows the tea, at an open stand, and a pupil with an open book, two blocks apart. */
    private record Pair(ServerPlayer teacher, GrimoireMenu teacherMenu, ServerPlayer pupil, GrimoireMenu pupilMenu,
                        OnlinePlayers.Registration online) {
    }

    private static Pair pair(GameTestHelper helper, String name) {
        floor(helper);
        BlockPos stand = new BlockPos(4, 1, 4);
        place(helper, GrimoireCatalog.STAND, stand);
        ServerPlayer teacher = standing(helper, "teach_" + name + "_t", new BlockPos(4, 1, 3));
        ServerPlayer pupil = standing(helper, "teach_" + name + "_p", new BlockPos(5, 1, 2));
        OnlinePlayers.Registration online = OnlinePlayers.register(List.of(teacher, pupil));
        DiscoveryService.grant(teacher, TEA);
        GrimoireMenu teacherMenu = openBlock(helper, teacher, GrimoireCatalog.STAND, stand);
        check(teacherMenu != null, "the stand did not open");
        return new Pair(teacher, teacherMenu, pupil, openBook(pupil), online);
    }

    private static TeachingRequest request(GameTestHelper helper, GrimoireMenu menu, TeachingRequest.Op op, UUID other) {
        return new TeachingRequest(menu.containerId, menu.nonce(), content(helper).revision(), op, Optional.of(TEA), Optional.of(other));
    }

    private static void done(GameTestHelper helper, Pair p) {
        TeachingEvents.cancelInvolving(p.teacher().getUUID());
        p.teacher().closeContainer();
        p.pupil().closeContainer();
        p.online().close();
        helper.succeed();
    }

    private static TeachingService service() {
        return TeachingEvents.service().orElseThrow(() -> new IllegalStateException("no teaching service on this server"));
    }

    @GameTest(template = TEMPLATE)
    public static void anAcceptedOfferTeachesTheRecipeOnly(GameTestHelper helper) {
        Pair p = pair(helper, "accept");
        var sent = capture(() -> TeachingEvents.handle(p.teacher(), request(helper, p.teacherMenu(), TeachingRequest.Op.INVITE, p.pupil().getUUID())));
        check(service().incoming(p.pupil().getUUID()).isPresent(), "no invitation reached the pupil");
        check(!DiscoveryService.knows(p.pupil(), TEA), "the pupil learned before accepting");
        check(sent.stream().anyMatch(s -> s.to() == p.pupil() && s.message() instanceof TeachingUpdate u && u.kind() == TeachingUpdate.Kind.INCOMING),
                "the pupil was not told");
        check(sent.stream().noneMatch(s -> s.to() != p.pupil() && s.to() != p.teacher()), "someone else was told");
        TeachingEvents.handle(p.pupil(), request(helper, p.pupilMenu(), TeachingRequest.Op.ACCEPT, p.teacher().getUUID()));
        check(DiscoveryService.knows(p.pupil(), TEA), "accepting did not teach the recipe");
        check(DiscoveryService.knows(p.pupil(), TEA) && service().incoming(p.pupil().getUUID()).isEmpty(), "the offer was not consumed");
        done(helper, p);
    }

    @GameTest(template = TEMPLATE)
    public static void aDeclinedOfferTeachesNothing(GameTestHelper helper) {
        Pair p = pair(helper, "decline");
        TeachingEvents.handle(p.teacher(), request(helper, p.teacherMenu(), TeachingRequest.Op.INVITE, p.pupil().getUUID()));
        TeachingEvents.handle(p.pupil(), request(helper, p.pupilMenu(), TeachingRequest.Op.DECLINE, p.teacher().getUUID()));
        check(!DiscoveryService.knows(p.pupil(), TEA) && service().incoming(p.pupil().getUUID()).isEmpty(), "declining left state behind");
        done(helper, p);
    }

    @GameTest(template = TEMPLATE)
    public static void distanceAndDimensionChangesEndOffers(GameTestHelper helper) {
        Pair p = pair(helper, "range");
        moveTo(helper, p.pupil(), new BlockPos(4, 1, 40));
        TeachingEvents.handle(p.teacher(), request(helper, p.teacherMenu(), TeachingRequest.Op.INVITE, p.pupil().getUUID()));
        check(service().incoming(p.pupil().getUUID()).isEmpty(), "an offer crossed 36 blocks");
        moveTo(helper, p.pupil(), new BlockPos(5, 1, 2));
        TeachingEvents.handle(p.teacher(), request(helper, p.teacherMenu(), TeachingRequest.Op.INVITE, p.pupil().getUUID()));
        check(service().incoming(p.pupil().getUUID()).isPresent(), "a close offer was refused");
        TeachingEvents.changedDimension(new PlayerEvent.PlayerChangedDimensionEvent(p.pupil(),
                helper.getLevel().dimension(), net.minecraft.world.level.Level.NETHER));
        check(service().incoming(p.pupil().getUUID()).isEmpty(), "the offer survived a dimension change");
        done(helper, p);
    }

    @GameTest(template = TEMPLATE)
    public static void spoofedAndReplayedAcceptancesTeachNothing(GameTestHelper helper) {
        Pair p = pair(helper, "spoof");
        ServerPlayer intruder = standing(helper, "teach_spoof_i", new BlockPos(3, 1, 2));
        p.online().add(intruder);
        GrimoireMenu intruderMenu = openBook(intruder);
        TeachingEvents.handle(p.teacher(), request(helper, p.teacherMenu(), TeachingRequest.Op.INVITE, p.pupil().getUUID()));
        TeachingEvents.handle(intruder, request(helper, intruderMenu, TeachingRequest.Op.ACCEPT, p.teacher().getUUID()));
        check(!DiscoveryService.knows(intruder, TEA), "a third player accepted someone else's offer");
        TeachingEvents.handle(p.pupil(), request(helper, p.teacherMenu(), TeachingRequest.Op.ACCEPT, p.teacher().getUUID()));
        check(!DiscoveryService.knows(p.pupil(), TEA), "accepted through the teacher's session");
        TeachingEvents.handle(p.pupil(), request(helper, p.pupilMenu(), TeachingRequest.Op.ACCEPT, p.teacher().getUUID()));
        check(DiscoveryService.knows(p.pupil(), TEA), "the genuine acceptance failed");
        check(service().accept(p.pupil().getUUID(), p.teacher().getUUID(), TEA) == TeachingService.Result.NO_INVITATION,
                "the offer could be accepted twice");
        intruder.closeContainer();
        done(helper, p);
    }

    @GameTest(template = TEMPLATE)
    public static void aReloadThatChangesTheRecipeAndALogoutEndOffers(GameTestHelper helper) {
        Pair p = pair(helper, "reload");
        TeachingEvents.handle(p.teacher(), request(helper, p.teacherMenu(), TeachingRequest.Op.INVITE, p.pupil().getUUID()));
        check(service().incoming(p.pupil().getUUID()).isPresent(), "no offer");
        TeachingEvents.contentChanged(GrimoireContent.empty()); // a reload in which the recipe vanished
        check(service().incoming(p.pupil().getUUID()).isEmpty(), "an offer outlived its recipe");
        service().cancelInvolving(p.teacher().getUUID());
        // a fresh offer after the cooldown, then the teacher logs out
        ServerPlayer teacher2 = standing(helper, "teach_reload_t2", new BlockPos(4, 1, 3));
        p.online().add(teacher2);
        DiscoveryService.grant(teacher2, TEA);
        p.teacher().closeContainer();
        GrimoireMenu menu2 = openBlock(helper, teacher2, GrimoireCatalog.STAND, new BlockPos(4, 1, 4));
        TeachingEvents.handle(teacher2, request(helper, menu2, TeachingRequest.Op.INVITE, p.pupil().getUUID()));
        check(service().incoming(p.pupil().getUUID()).isPresent(), "the second offer was refused");
        TeachingEvents.loggedOut(new PlayerEvent.PlayerLoggedOutEvent(teacher2));
        check(service().incoming(p.pupil().getUUID()).isEmpty(), "an offer outlived its teacher's logout");
        teacher2.closeContainer();
        done(helper, p);
    }
}
