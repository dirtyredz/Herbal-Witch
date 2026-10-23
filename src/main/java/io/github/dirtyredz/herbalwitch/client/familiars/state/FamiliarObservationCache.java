package io.github.dirtyredz.herbalwitch.client.familiars.state;

import io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarObservations;
import net.minecraft.client.Minecraft;
import java.util.*;

/** Private marks expire after five loaded client seconds and never cross worlds. */
public final class FamiliarObservationCache {
    private record Entry(FamiliarObservations page, Object level, long until) {}
    private static final Map<UUID, Entry> PAGES = new LinkedHashMap<>();
    private FamiliarObservationCache() {}
    public static void accept(FamiliarObservations page) {
        var level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().toString().equals(page.dimension())) return;
        PAGES.remove(page.bond()); PAGES.put(page.bond(), new Entry(page, level, level.getGameTime() + 100));
        while (PAGES.size() > 32) PAGES.remove(PAGES.keySet().iterator().next());
    }
    public static List<FamiliarObservations.Mark> marks(UUID bond) {
        var level = Minecraft.getInstance().level;
        PAGES.values().removeIf(e -> e.level() != level || level == null || e.until() <= level.getGameTime());
        var entry = PAGES.get(bond); return entry == null ? List.of() : entry.page().marks();
    }
    public static void clear() { PAGES.clear(); }
    public static List<FamiliarObservations.Mark> visible() {
        marks(null); return PAGES.values().stream().flatMap(e -> e.page().marks().stream()).limit(64).toList();
    }
}
