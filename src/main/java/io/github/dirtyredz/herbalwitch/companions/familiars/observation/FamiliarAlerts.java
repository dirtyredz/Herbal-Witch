package io.github.dirtyredz.herbalwitch.companions.familiars.observation;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import java.util.*;

/** Shared owner/target throttle: several familiars observing one threat produce one private warning. */
public final class FamiliarAlerts {
    private static final Map<UUID, LinkedHashMap<UUID, Long>> RECENT = new HashMap<>();
    private FamiliarAlerts() {}
    public static void warn(ServerPlayer owner, Mob familiar, Mob target) {
        long now = owner.server.overworld().getGameTime();
        var recent = RECENT.computeIfAbsent(owner.getUUID(), ignored -> new LinkedHashMap<>());
        if (recent.getOrDefault(target.getUUID(), Long.MIN_VALUE / 2) + 100 > now) return;
        recent.remove(target.getUUID()); recent.put(target.getUUID(), now);
        while (recent.size() > 64) recent.remove(recent.keySet().iterator().next());
        owner.displayClientMessage(Component.translatable("familiar.herbalwitch.threat", familiar.getName(), target.getName()), true);
    }
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { RECENT.remove(event.getEntity().getUUID()); }
    public static void stopped(ServerStoppedEvent event) { RECENT.clear(); }
}
