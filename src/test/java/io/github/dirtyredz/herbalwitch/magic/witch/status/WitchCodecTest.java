package io.github.dirtyredz.herbalwitch.magic.witch.status;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The saved witch form: round trip, fresh players, and unreadable data preserved exactly and never overwritten. */
class WitchCodecTest {
    private static final UUID ID = UUID.nameUUIDFromBytes("witch".getBytes());

    private static WitchState witch() {
        WitchState s = new WitchState();
        s.initiate(ID);
        s.setEnergyUnits(5000);
        s.setPracticeSpent(1200);
        s.setTeaCooldownTicks(60);
        s.setRestfulTicks(300);
        s.setLastSleepRewardDay(9);
        return s;
    }

    private static CompoundTag tagWith(Consumer<CompoundTag> edit) {
        CompoundTag tag = WitchCodec.write(witch());
        edit.accept(tag);
        return tag;
    }

    private static void assertPreserved(CompoundTag tag) {
        CompoundTag before = tag.copy();
        WitchCodec.Loaded loaded = WitchCodec.read(tag);
        assertFalse(loaded.writable(), "unreadable data must not be writable");
        assertTrue(loaded.problem().isPresent());
        assertEquals(before, WitchCodec.write(loaded), "the saved tag is written back exactly");
        assertFalse(loaded.state().initiated());
        assertTrue(loaded.state().sameAs(new WitchState()), "the working state is a fresh non-witch");
    }

    @Test
    void absentOrEmptyTagIsAFreshWritablePlayer() {
        for (CompoundTag tag : new CompoundTag[] {null, new CompoundTag()}) {
            WitchCodec.Loaded loaded = WitchCodec.read(tag);
            assertTrue(loaded.writable());
            assertTrue(loaded.problem().isEmpty());
            assertTrue(loaded.state().sameAs(new WitchState()));
        }
    }

    @Test
    void initiatedStateRoundTripsEveryField() {
        WitchCodec.Loaded loaded = WitchCodec.read(WitchCodec.write(witch()));
        assertTrue(loaded.writable());
        assertTrue(loaded.problem().isEmpty());
        WitchState s = loaded.state();
        assertTrue(s.initiated());
        assertEquals(ID, s.initiationId().orElseThrow());
        assertEquals(5000, s.energyUnits());
        assertEquals(1200, s.practiceSpent());
        assertEquals(60, s.teaCooldownTicks());
        assertEquals(300, s.restfulTicks());
        assertEquals(9, s.lastSleepRewardDay());
        assertTrue(s.sameAs(witch()));
    }

    @Test
    void freshNonWitchRoundTrips() {
        WitchCodec.Loaded loaded = WitchCodec.read(WitchCodec.write(new WitchState()));
        assertTrue(loaded.writable());
        assertTrue(loaded.state().sameAs(new WitchState()));
        assertEquals(WitchState.NEVER, loaded.state().lastSleepRewardDay());
    }

    @Test
    void missingSchemaIsPreserved() {
        assertPreserved(tagWith(t -> t.remove(WitchCodec.SCHEMA_KEY)));
    }

    @Test
    void unsupportedSchemaIsPreserved() {
        assertPreserved(tagWith(t -> t.putInt(WitchCodec.SCHEMA_KEY, 2)));
    }

    @Test
    void wrongTagTypeIsPreserved() {
        assertPreserved(tagWith(t -> t.putString(WitchCodec.ENERGY, "lots")));
    }

    @Test
    void missingFieldIsPreserved() {
        assertPreserved(tagWith(t -> t.remove(WitchCodec.SLEEP_DAY)));
        assertPreserved(tagWith(t -> t.remove(WitchCodec.TEA)));
    }

    @Test
    void negativeOrHugeNumbersArePreserved() {
        assertPreserved(tagWith(t -> t.putInt(WitchCodec.ENERGY, -1)));
        assertPreserved(tagWith(t -> t.putInt(WitchCodec.PRACTICE, WitchCodec.MAX_UNITS + 1)));
        assertPreserved(tagWith(t -> t.putLong(WitchCodec.SLEEP_DAY, -2)));
    }

    @Test
    void initiatedWithoutIdentityIsPreserved() {
        assertPreserved(tagWith(t -> t.remove(WitchCodec.INITIATION)));
    }

    @Test
    void identityWithoutWitchStatusIsPreserved() {
        assertPreserved(tagWith(t -> t.putBoolean(WitchCodec.INITIATED, false)));
    }

    @Test
    void energyOnANonWitchIsPreserved() {
        WitchState odd = new WitchState();
        odd.setEnergyUnits(5);
        assertPreserved(WitchCodec.write(odd));
    }

    @Test
    void loadedCopyIsIndependent() {
        WitchCodec.Loaded ok = WitchCodec.read(WitchCodec.write(witch()));
        WitchCodec.Loaded okCopy = ok.copy();
        okCopy.state().setEnergyUnits(1);
        assertEquals(5000, ok.state().energyUnits());

        WitchCodec.Loaded bad = WitchCodec.read(tagWith(t -> t.putInt(WitchCodec.SCHEMA_KEY, 2)));
        WitchCodec.Loaded badCopy = bad.copy();
        badCopy.preserved().orElseThrow().putInt("extra", 1);
        assertFalse(bad.preserved().orElseThrow().contains("extra"));
        assertFalse(badCopy.writable());
        assertEquals(bad.problem(), badCopy.problem());
    }
}
