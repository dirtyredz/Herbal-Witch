package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.*;
import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import java.util.*;

public final class FamiliarEquipmentVisuals {
    private static final Map<Mob, FamiliarVisualSnapshot> SENT = new WeakHashMap<>();
    private FamiliarEquipmentVisuals() {}
    public static FamiliarVisualSnapshot project(Mob mob) {
        var r = FamiliarReconciliation.record(mob).orElse(null); if (r == null) return null;
        var gear = FamiliarEquipmentInventory.item(r, 1); var charm = FamiliarEquipmentInventory.item(r, 0);
        boolean resting = r.command() == FamiliarCommand.STAY && mob.getDeltaMovement().horizontalDistanceSqr() < .0004 && mob.getTarget() == null;
        return new FamiliarVisualSnapshot(mob.getId(), mob.getUUID(), r.generation(),
                FamiliarEquipmentRules.equipped(r.species(), gear) ? FamiliarCatalog.find(r.species()).orElseThrow().equipment() : "",
                charm.getItem() instanceof ProtectiveCharmItem item ? item.spec().id() : "", r.command().key(), resting);
    }
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide || mob.tickCount % 10 != 0) return;
        var visual = project(mob); if (visual == null || visual.equals(SENT.get(mob))) return;
        SENT.put(mob, visual); Messages.tracking(mob, visual);
    }
    public static void tracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof Mob mob) {
            var visual = project(mob); if (visual != null) Messages.send(player, visual);
        }
    }
    public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent event) { SENT.clear(); }
}
