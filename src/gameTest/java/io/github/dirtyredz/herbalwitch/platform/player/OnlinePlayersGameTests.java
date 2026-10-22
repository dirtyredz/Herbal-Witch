package io.github.dirtyredz.herbalwitch.platform.player;

import com.mojang.authlib.GameProfile;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.List;
import java.util.UUID;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OnlinePlayersGameTests {
    @GameTest(template = TEMPLATE)
    public static void noServerAndStoppedServerExposeNoPlayers(GameTestHelper helper) {
        OnlinePlayers.Roster roster = new OnlinePlayers.Roster();
        ServerPlayer player = standing(helper, "roster_lifecycle", new BlockPos(1, 1, 1));
        check(roster.all().isEmpty() && roster.find(player.getUUID()).isEmpty(), "no-server roster is not empty");
        roster.start(List::of);
        try (var registration = roster.register(List.of(player))) {
            check(roster.find(player.getUUID()).orElseThrow() == player, "fake was not registered");
            roster.stop();
            check(roster.all().isEmpty(), "stop retained players");
            roster.start(List::of);
            check(roster.all().isEmpty(), "restart retained a stale scope");
        }
        roster.stop();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void realPlayersTakePrecedenceAndEnumerationDeduplicatesIds(GameTestHelper helper) {
        OnlinePlayers.Roster roster = new OnlinePlayers.Roster();
        UUID id = UUID.nameUUIDFromBytes("roster_same_id".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        ServerPlayer real = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(id, "roster_real"));
        ServerPlayer fake = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(id, "roster_fake"));
        roster.start(() -> List.of(real));
        try (var registration = roster.register(List.of(fake))) {
            check(roster.find(id).orElseThrow() == real, "fake shadowed a real player");
            check(roster.all().equals(List.of(real)), "duplicate UUID escaped enumeration");
        } finally {
            roster.stop();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void scopesCloseIndependentlyAndRemovedFakesDisappear(GameTestHelper helper) {
        OnlinePlayers.Roster roster = new OnlinePlayers.Roster();
        roster.start(List::of);
        ServerPlayer witch = standing(helper, "roster_witch", new BlockPos(1, 1, 1));
        ServerPlayer teacher = standing(helper, "roster_teacher", new BlockPos(2, 1, 1));
        try (var witches = roster.register(List.of(witch))) {
            try (var teaching = roster.register(List.of(teacher))) {
                check(roster.all().equals(List.of(witch, teacher)), "scope registration order changed");
            }
            check(roster.find(witch.getUUID()).orElseThrow() == witch, "teaching cleanup erased witches");
            check(roster.find(teacher.getUUID()).isEmpty(), "closed teaching scope remained");
            witch.discard();
            check(roster.all().isEmpty() && roster.find(witch.getUUID()).isEmpty(), "removed fake remained online");
        } finally {
            roster.stop();
        }
        helper.succeed();
    }
}
