package io.github.dirtyredz.herbalwitch.companions.animals.fixture;

import com.mojang.authlib.GameProfile;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class AnimalFixtures {
    private AnimalFixtures() {}
    public static ServerPlayer player(GameTestHelper helper, String name) {
        ServerPlayer player = PlayerFixtures.player(helper, name, GameType.SURVIVAL, Direction.SOUTH);
        var pos = helper.absolutePos(new BlockPos(4, 1, 3)); player.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
        return player;
    }
    /** Real ServerPlayer authority for transport; only the no-op test connection is borrowed from a uniquely named fixture. */
    public static ServerPlayer realPlayer(GameTestHelper helper, String name) {
        ServerPlayer connection = player(helper, name + "_wire");
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(
                UUID.nameUUIDFromBytes(("m8-real:" + name).getBytes(StandardCharsets.UTF_8)), "m8_" + name));
        player.connection = connection.connection; player.moveTo(connection.position()); return player;
    }
    public static <T extends Mob> T animal(GameTestHelper helper, EntityType<T> type) { return helper.spawn(type, new BlockPos(4, 1, 5)); }
    /** Persisted fixture data exercises the public codec; never adds test mutation APIs to the release jar. */
    public static void relationship(Mob mob, UUID actor, int trust, int feeds) {
        CompoundTag state = base(); ListTag relationships = new ListTag(); CompoundTag relation = new CompoundTag();
        relation.putUUID("player", actor); relation.putInt("trust", trust); relation.putInt("feeds", feeds); relation.putLong("nextFeed", 0);
        relationships.add(relation); state.put("relationships", relationships); AnimalCapability.of(mob).orElseThrow().load(state);
    }
    public static CompoundTag base() {
        CompoundTag state = new CompoundTag(); state.putInt("schema", 1); state.put("relationships", new ListTag());
        for (String field : new String[] {"clock", "hurt", "heal", "calm", "calmBlocked", "generation"}) state.putLong(field, 0);
        return state;
    }
    public static void advance(Mob mob, int ticks) {
        for (int i = 0; i < ticks; i++) io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalLifecycleEvents.tick(
                new net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent(mob));
    }
}
