package io.github.dirtyredz.herbalwitch.companions.familiars.observation;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarObservations;
import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** At most 32 observed bonds per online owner, each with eight five-second action tokens. */
public final class FamiliarObservationsStore {
    public record Target(UUID entity, BlockPos block, BlockState state, String kind, String label, net.minecraft.nbt.CompoundTag stack) {
        public Target(UUID entity, BlockPos block, BlockState state, String kind, String label) { this(entity, block, state, kind, label, new net.minecraft.nbt.CompoundTag()); }
        public Target { stack = stack.copy(); }
        @Override public net.minecraft.nbt.CompoundTag stack() { return stack.copy(); }
    }
    private record Page(UUID bond, long generation, ServerLevel level, long until, long rulesRevision, Map<UUID, Target> targets) {}
    private static final Map<UUID, LinkedHashMap<UUID, Page>> OWNERS = new HashMap<>();
    private FamiliarObservationsStore() {}
    public static void invalidate() {
        OWNERS.forEach((id, pages) -> io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers.find(id).ifPresent(owner ->
                pages.values().forEach(page -> Messages.send(owner, new FamiliarObservations(page.bond(), page.level().dimension().location().toString(), List.of())))));
        OWNERS.clear();
    }
    public static boolean publish(ServerPlayer owner, Mob familiar, List<Target> targets) {
        var record = FamiliarReconciliation.record(familiar).orElse(null);
        if (targets.isEmpty() || targets.size() > 8 || record == null || !record.owner().equals(owner.getUUID()) || owner.level() != familiar.level()) return false;
        var mapped = new LinkedHashMap<UUID, Target>(); var marks = new ArrayList<FamiliarObservations.Mark>();
        for (var target : targets) {
            var token = UUID.randomUUID(); mapped.put(token, target);
            marks.add(new FamiliarObservations.Mark(token, target.block().asLong(), target.label().substring(0, Math.min(64, target.label().length())), target.kind()));
        }
        var pages = OWNERS.computeIfAbsent(owner.getUUID(), ignored -> new LinkedHashMap<>());
        pages.remove(record.bond()); pages.put(record.bond(), new Page(record.bond(), record.generation(), owner.serverLevel(), owner.level().getGameTime() + 100,
                io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig.revision(), Map.copyOf(mapped)));
        while (pages.size() > 32) pages.remove(pages.keySet().iterator().next());
        Messages.send(owner, new FamiliarObservations(record.bond(), familiar.level().dimension().location().toString(), marks)); return true;
    }
    public static Optional<Target> target(ServerPlayer owner, Mob familiar, UUID token) {
        var record = FamiliarReconciliation.record(familiar).orElse(null); if (record == null || token == null || !record.owner().equals(owner.getUUID())) return Optional.empty();
        var pages = OWNERS.get(owner.getUUID()); var page = pages == null ? null : pages.get(record.bond());
        if (page == null || page.rulesRevision() != io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig.revision()
                || page.level() != familiar.level() || page.level() != owner.level() || page.generation() != record.generation() || page.until() < owner.level().getGameTime()) return Optional.empty();
        var target = page.targets().get(token); if (target == null || !familiar.level().hasChunkAt(target.block())) return Optional.empty();
        if (target.entity() != null) {
            var entity = page.level().getEntity(target.entity()); if (entity == null || entity.isRemoved()) return Optional.empty();
        } else if (!familiar.level().getBlockState(target.block()).equals(target.state())) return Optional.empty();
        return Optional.of(target);
    }
    public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) { OWNERS.remove(event.getEntity().getUUID()); }
    public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent event) { OWNERS.clear(); }
}
