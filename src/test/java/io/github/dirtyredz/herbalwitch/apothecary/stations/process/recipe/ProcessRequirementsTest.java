package io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Recipe requirements: absent means ordinary, JSON and wire round trips, bounds, type policies and the cost multiplier. */
class ProcessRequirementsTest {
    @Test
    void anAbsentObjectIsOrdinaryAndOrdinaryWritesNothing() {
        JsonObject recipe = new JsonObject();
        assertEquals(ProcessRequirements.NONE, ProcessRequirements.read(recipe));
        ProcessRequirements.NONE.write(recipe);
        assertFalse(recipe.has("requirements"), "ordinary recipes keep their Milestone 3 JSON (and fingerprint)");
    }

    @Test
    void witchRecipesRoundTripThroughJsonAndTheWire() {
        ProcessRequirements witch = ProcessRequirements.witch(15);
        JsonObject recipe = new JsonObject();
        witch.write(recipe);
        assertEquals(witch, ProcessRequirements.read(JsonParser.parseString(recipe.toString()).getAsJsonObject()));
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        witch.toNetwork(buf);
        assertEquals(witch, ProcessRequirements.fromNetwork(buf));
    }

    @Test
    void boundsAreEnforced() {
        assertThrows(JsonParseException.class, () -> new ProcessRequirements(true, -1));
        assertThrows(JsonParseException.class, () -> new ProcessRequirements(true, ProcessRequirements.MAX_ENERGY + 1));
        assertThrows(JsonParseException.class, () -> new ProcessRequirements(false, 5), "energy needs a witch");
        JsonObject unknown = JsonParser.parseString("{\"requirements\": {\"witch\": true, \"mana\": 3}}").getAsJsonObject();
        assertThrows(JsonParseException.class, () -> ProcessRequirements.read(unknown));
        FriendlyByteBuf bad = new FriendlyByteBuf(Unpooled.buffer());
        bad.writeBoolean(false);
        bad.writeVarInt(7);
        assertThrows(IllegalArgumentException.class, () -> ProcessRequirements.fromNetwork(bad));
    }

    @Test
    void typePoliciesAcceptOnlyTheirKind() {
        ProcessRequirements.Policy.ORDINARY.check(ProcessRequirements.NONE);
        assertThrows(JsonParseException.class, () -> ProcessRequirements.Policy.ORDINARY.check(ProcessRequirements.witch(5)));
        ProcessRequirements.Policy.MAGICAL.check(ProcessRequirements.witch(5));
        assertThrows(JsonParseException.class, () -> ProcessRequirements.Policy.MAGICAL.check(ProcessRequirements.NONE));
        ProcessRequirements.Policy.EITHER.check(ProcessRequirements.NONE);
        ProcessRequirements.Policy.EITHER.check(ProcessRequirements.witch(5));
    }

    @Test
    void theMultiplierRoundsUpAndNeverMakesACostFree() {
        ProcessRequirements ten = ProcessRequirements.witch(10);
        assertEquals(10, ten.effectiveEnergy(100));
        assertEquals(15, ten.effectiveEnergy(150));
        assertEquals(1, ProcessRequirements.witch(1).effectiveEnergy(1), "a fraction of a point still costs one");
        assertEquals(0, ten.effectiveEnergy(0), "a zero multiplier makes witch work free (still witch-only)");
        assertEquals(0, ProcessRequirements.NONE.effectiveEnergy(500));
        assertTrue(ten.magical() && !ProcessRequirements.NONE.magical());
    }
}
