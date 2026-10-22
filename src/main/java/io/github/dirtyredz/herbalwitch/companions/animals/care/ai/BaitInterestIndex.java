package io.github.dirtyredz.herbalwitch.companions.animals.care.ai;

import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareItem;
import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Spatial index of active holders, updated by player ticks. Queries rotate bucket cursors and never collect a player/entity list. */
public final class BaitInterestIndex {
    private record Holder(ServerPlayer player, ServerLevel level, long cell) {}
    private static final Map<UUID, Holder> HOLDERS = new HashMap<>();
    private static final Map<ServerLevel, Map<Long, ArrayDeque<UUID>>> CELLS = new HashMap<>();
    private static final Map<ServerLevel, AnimalAiBudget> BUDGETS = new HashMap<>();
    private BaitInterestIndex() {}

    public static AnimalAiBudget budget(ServerLevel level) {
        var budget = BUDGETS.computeIfAbsent(level, ignored -> new AnimalAiBudget());
        budget.begin(level.getGameTime()); return budget;
    }
    public static boolean holding(ServerPlayer player, Bait bait) {
        return matching(player.getMainHandItem(), bait) || matching(player.getOffhandItem(), bait);
    }
    private static boolean matching(ItemStack stack, Bait bait) { return stack.getItem() instanceof AnimalCareItem item && item.bait() != null && (bait == null || item.bait() == bait); }

    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) update(player);
    }
    /** The same index update is available to fixtures because fake players do not tick. */
    public static void update(ServerPlayer player) {
        UUID id = player.getUUID();
        if (!player.isAlive() || player.isSpectator() || !AnimalServerConfig.current().attraction() || !holding(player, null)) { remove(id); return; }
        long cell = player.chunkPosition().toLong();
        Holder old = HOLDERS.get(id);
        if (old != null && old.player == player && old.level == player.serverLevel() && old.cell == cell) return;
        remove(id);
        HOLDERS.put(id, new Holder(player, player.serverLevel(), cell));
        CELLS.computeIfAbsent(player.serverLevel(), ignored -> new HashMap<>()).computeIfAbsent(cell, ignored -> new ArrayDeque<>()).addLast(id);
    }
    private static void remove(UUID id) {
        Holder old = HOLDERS.remove(id);
        if (old == null) return;
        var cells = CELLS.get(old.level); var queue = cells.get(old.cell);
        queue.remove(id); if (queue.isEmpty()) cells.remove(old.cell); if (cells.isEmpty()) CELLS.remove(old.level);
    }
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { remove(event.getEntity().getUUID()); }

    public static boolean near(Mob mob) {
        var cells = CELLS.get((ServerLevel) mob.level());
        if (cells == null) return false;
        ChunkPos pos = mob.chunkPosition();
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) if (cells.containsKey(ChunkPos.asLong(pos.x + x, pos.z + z))) return true;
        return false;
    }

    public static ServerPlayer find(Mob mob, Bait bait, int start, java.util.function.Predicate<ServerPlayer> accepts) {
        var cells = CELLS.get((ServerLevel) mob.level());
        if (cells == null) return null;
        var rules = AnimalServerConfig.current(); var budget = budget((ServerLevel) mob.level());
        int examined = 0;
        ChunkPos pos = mob.chunkPosition();
        for (int offset = 0; offset < 9; offset++) {
            int cell = Math.floorMod(start + offset, 9);
            var queue = cells.get(ChunkPos.asLong(pos.x + cell / 3 - 1, pos.z + cell % 3 - 1));
            if (queue == null) continue;
            int available = queue.size();
            for (int n = 0; n < available && examined < rules.candidates(); n++) {
                if (!budget.candidate(rules)) return null;
                examined++;
                UUID id = queue.removeFirst(); queue.addLast(id);
                Holder holder = HOLDERS.get(id);
                ServerPlayer player = holder.player;
                if (player.isAlive() && !player.isRemoved() && player.level() == mob.level() && !player.isSpectator()
                        && mob.distanceToSqr(player) <= rules.radius() * rules.radius() && holding(player, bait) && accepts.test(player)) return player;
            }
            if (examined >= rules.candidates()) break;
        }
        return null;
    }
    public static void reset() { HOLDERS.clear(); CELLS.clear(); BUDGETS.clear(); }
}
