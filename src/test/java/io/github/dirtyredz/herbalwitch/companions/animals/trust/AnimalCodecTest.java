package io.github.dirtyredz.herbalwitch.companions.animals.trust;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;

class AnimalCodecTest {
    private static AnimalState sample() {
        var state = new AnimalState();
        state.feed(new UUID(0, 1), 8);
        state.tick();
        state.keeper = new UUID(0, 1);
        state.calmSource = state.keeper;
        state.calmUntil = state.clock + 200;
        state.healUntil = state.clock + 200;
        state.generation = 4;
        return state;
    }

    private static void preserved(Consumer<CompoundTag> edit) {
        var tag = AnimalCodec.write(sample());
        edit.accept(tag);
        var before = tag.copy();
        var loaded = AnimalCodec.read(tag);
        assertFalse(loaded.writable());
        tag.putString("outside", "mutation");
        assertEquals(before, AnimalCodec.write(loaded));
    }

    @Test void roundTripIncludesAllGatesOwnersAndGenerations() {
        var tag = AnimalCodec.write(sample());
        var loaded = AnimalCodec.read(tag);
        assertTrue(loaded.writable());
        assertEquals(tag, AnimalCodec.write(loaded));
        assertEquals(1199, loaded.state().relationship(new UUID(0, 1)).remaining(loaded.state().clock()));
    }

    @Test void missingAndEmptyAreFresh() {
        assertTrue(AnimalCodec.read(null).writable());
        assertTrue(AnimalCodec.read(new CompoundTag()).writable());
    }

    @Test void futureMalformedAndUnknownFieldsArePreserved() {
        preserved(t -> t.putInt("schema", 2));
        preserved(t -> t.remove("clock"));
        preserved(t -> t.putLong("clock", -1));
        preserved(t -> t.putLong("clock", Long.MAX_VALUE));
        preserved(t -> t.putString("keeper", "not a uuid"));
        preserved(t -> t.putString("future", "valuable"));
        preserved(t -> t.remove("calmSource"));
        preserved(t -> t.putString("relationships", "broken"));
    }

    @Test void duplicatesOverflowAndBadRelationshipNumbersArePreserved() {
        preserved(t -> { var list = t.getList("relationships", Tag.TAG_COMPOUND); list.add(list.getCompound(0).copy()); });
        preserved(t -> t.getList("relationships", Tag.TAG_COMPOUND).getCompound(0).putInt("trust", 101));
        preserved(t -> t.getList("relationships", Tag.TAG_COMPOUND).getCompound(0).putLong("nextFeed", 1202));
        preserved(t -> { var list = t.getList("relationships", Tag.TAG_COMPOUND);
            for (int i = 2; i <= 17; i++) { var entry = list.getCompound(0).copy(); entry.putUUID("player", new UUID(0, i)); list.add(entry); }
        });
    }
}
