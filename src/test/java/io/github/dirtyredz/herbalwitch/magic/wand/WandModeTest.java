package io.github.dirtyredz.herbalwitch.magic.wand;

import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.persistence.WandItemData;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WandModeTest {
    @Test void exactSevenStableSelectorsRoundTrip() {
        assertArrayEquals(new String[] {"harvest", "plant", "water", "collect", "light", "focus", "familiar"},
                java.util.Arrays.stream(WandMode.values()).map(WandMode::id).toArray(String[]::new));
        for (WandMode mode : WandMode.values()) {
            assertEquals(mode, WandItemData.decode(WandItemData.encode(mode)).orElseThrow());
        }
    }

    @Test void malformedPreferenceIsPreservedAndRefused() {
        assertTrue(WandItemData.decode(new CompoundTag()).isEmpty());
        CompoundTag tag = WandItemData.encode(WandMode.FOCUS);
        tag.putInt("schema", 2);
        assertTrue(WandItemData.decode(tag).isEmpty());
        tag.putInt("schema", 1);
        tag.putString("mode", "teleport");
        assertTrue(WandItemData.decode(tag).isEmpty());
        assertEquals("teleport", tag.getString("mode"));
    }
}
