package io.github.dirtyredz.herbalwitch.client.ritual.widgets;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarUpdate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * An action's offering as the altar screen lists it: each item with what the player carries against
 * what it needs (from the server's counts over main inventory and offhand), green when met and pink
 * when missing, followed by where it is taken from.
 */
public final class OfferingLines {
    static final int MET = 0xFF2F5A2C;
    static final int MISSING = 0xFFB0305A;
    static final int INK = 0xFF3B2416;

    private OfferingLines() {
    }

    /** Writes the lines; returns the height used. */
    public static int write(AltarUi ui, AltarUpdate update, AltarActionSpec action, int x, int y, int width) {
        List<AltarUpdate.Count> counts = update.counts().getOrDefault(action, List.of());
        int h = 0;
        for (int i = 0; i < action.offering().size(); i++) {
            AltarActionSpec.Offering o = action.offering().get(i);
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(o.item()));
            int have = i < counts.size() ? counts.get(i).have() : 0;
            Component name = item == null ? Component.literal(o.item()) : item.getDescription();
            h += ui.text(Component.translatable("ritual.herbalwitch.ui.offering_line", name, have, o.count()), x, y + h, width,
                    have >= o.count() ? MET : MISSING);
        }
        h += ui.text(Component.translatable("ritual.herbalwitch.ui.scope"), x, y + h, width, INK);
        return h;
    }

    /** Whether the server's counts say every requirement is met. */
    public static boolean complete(AltarUpdate update, AltarActionSpec action) {
        List<AltarUpdate.Count> counts = update.counts().getOrDefault(action, List.of());
        return counts.size() == action.offering().size() && counts.stream().allMatch(c -> c.have() >= c.need());
    }
}
