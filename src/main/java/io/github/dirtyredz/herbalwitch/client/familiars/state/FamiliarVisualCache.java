package io.github.dirtyredz.herbalwitch.client.familiars.state;

import io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarVisualSnapshot;
import net.minecraft.world.entity.Entity;
import java.util.*;

/** Bounded connection-local appearance. Entity removal, world changes and disconnect clear it. */
public final class FamiliarVisualCache {
    private static final Map<UUID, FamiliarVisualSnapshot> VALUES = new LinkedHashMap<>();
    private FamiliarVisualCache() {}
    public static void accept(FamiliarVisualSnapshot visual) {
        var previous = VALUES.get(visual.creature());
        if (previous != null && previous.generation() > visual.generation()) return;
        if (previous == null && VALUES.size() >= 2048) VALUES.remove(VALUES.keySet().iterator().next());
        VALUES.put(visual.creature(), visual);
    }
    public static FamiliarVisualSnapshot get(Entity entity) {
        var value = VALUES.get(entity.getUUID()); return value != null && value.entity() == entity.getId() ? value : null;
    }
    public static void leave(net.minecraftforge.event.entity.EntityLeaveLevelEvent event) { if (event.getLevel().isClientSide) VALUES.remove(event.getEntity().getUUID()); }
    public static void clear() { VALUES.clear(); }
}
