package io.github.dirtyredz.herbalwitch.apothecary.shop.customer;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.NpcProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemand;
import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand.NpcDemandService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopBindingService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopProjection;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransaction;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransfer;
import io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Actual nearby villagers visiting open shops. Per tick: at most four shop evaluations, sixteen
 * inspected candidates each and one path request; at most one visit per shop and the configured number
 * globally. A visit offers the register as a walk target while the villager's own brain is idle,
 * revalidates everything every tick and buys at arrival through the shared transfer path, after a
 * durable budget debit. Nothing teleports, loads chunks, breaks blocks or touches the villager's
 * inventory, offers, experience or reputation; the product is consumed as demand.
 */
public final class ShopCustomerScheduler {
    /** One customer walking to a register. */
    public record Visit(UUID villager, UUID shop, String dimension, BlockPos register, UUID offer, long revision, long started) {
    }

    private static final Map<MinecraftServer, State> STATES = new java.util.WeakHashMap<>();

    private static final class State {
        final Map<UUID, Visit> visits = new HashMap<>(); // by shop
        final Map<UUID, Long> lastVisit = new HashMap<>();
        int cursor;
    }

    private ShopCustomerScheduler() {
    }

    private static State state(MinecraftServer server) {
        return STATES.computeIfAbsent(server, s -> new State());
    }

    public static List<Visit> visits(MinecraftServer server) {
        return List.copyOf(state(server).visits.values());
    }

    public static void stopped(MinecraftServer server) {
        STATES.remove(server);
    }

    /** Configuration changes cancel every visit. */
    public static void invalidate(MinecraftServer server) {
        State state = state(server);
        for (Visit visit : List.copyOf(state.visits.values())) end(server, state, visit);
    }

    public static void tick(MinecraftServer server) {
        ShopStore store = ShopStore.get(server);
        if (!store.ready()) return;
        State state = state(server);
        for (Visit visit : List.copyOf(state.visits.values())) advance(server, store, state, visit);
        ShopRules rules = ShopServerConfig.current();
        if (!rules.npcService()) return;
        List<UUID> ids = store.ids();
        boolean pathUsed = false;
        for (int i = 0; i < Math.min(ShopRules.EVALUATIONS_PER_TICK, ids.size()); i++) {
            state.cursor = Math.floorMod(state.cursor + 1, ids.size());
            if (state.visits.size() >= rules.concurrentVisits()) return;
            ShopRecord record = store.record(ids.get(state.cursor)).orElse(null);
            if (record == null) continue;
            pathUsed |= evaluate(server, store, state, record, rules, pathUsed);
        }
    }

    /** Returns whether a path request was spent. */
    private static boolean evaluate(MinecraftServer server, ShopStore store, State state, ShopRecord record, ShopRules rules, boolean pathUsed) {
        if (!record.npcService || record.lifecycle != ShopRecord.Lifecycle.OPEN || record.blocked() || state.visits.containsKey(record.id)) return false;
        long now = NpcDemandService.now(server);
        Long last = state.lastVisit.get(record.id);
        if (last != null && now - last < rules.visitInterval()) return false;
        if (record.proceeds >= rules.proceedsCapacity()) return false;
        Optional<ShopOffer> offer = offer(record);
        if (offer.isEmpty() || !ShopBindingService.registerLoaded(server, record)) return false;
        ServerLevel level = ShopBindingService.level(server, record.binding.dimension());
        BlockPos register = BlockPos.of(record.binding.pos());
        if (!PrincipalPermission.mayUse(level, record.owner, register).allowed()) return false;
        NpcDemand demand = NpcDemandService.get(server).demand();
        if (!NpcDemandService.get(server).writable()) return false;
        List<Villager> nearby = level.getEntitiesOfClass(Villager.class, new AABB(register).inflate(ShopRules.CUSTOMER_RANGE));
        nearby.sort(java.util.Comparator.comparingDouble(v -> v.distanceToSqr(register.getCenter())));
        int inspected = 0;
        for (Villager villager : nearby) {
            if (inspected++ >= ShopRules.CANDIDATES_PER_EVALUATION) break;
            if (reserved(state, villager.getUUID()) || !ShopCustomerRules.eligible(villager)) continue;
            if (demand.check(record.owner, record.id, villager.getUUID(), rules) != NpcDemand.Refusal.NONE) continue;
            if (pathUsed) return false;
            Path path = villager.getNavigation().createPath(register, ShopRules.ARRIVAL_DISTANCE);
            if (path == null || !path.canReach()) return true;
            Visit visit = new Visit(villager.getUUID(), record.id, record.binding.dimension(), register, offer.get().id(), offer.get().revision(), now);
            state.visits.put(record.id, visit);
            steer(villager, register);
            return true;
        }
        return false;
    }

    private static Optional<ShopOffer> offer(ShopRecord record) {
        return record.offers.stream().filter(NpcProductCatalog::eligible).filter(o -> record.stock.count(o.key()) >= o.lot()).findFirst();
    }

    private static boolean reserved(State state, UUID villager) {
        return state.visits.values().stream().anyMatch(v -> v.villager().equals(villager));
    }

    private static void steer(Villager villager, BlockPos register) {
        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(register, 0.5F, ShopRules.ARRIVAL_DISTANCE));
    }

    private static void advance(MinecraftServer server, ShopStore store, State state, Visit visit) {
        ShopRules rules = ShopServerConfig.current();
        ShopRecord record = store.record(visit.shop()).orElse(null);
        ServerLevel level = ShopBindingService.level(server, visit.dimension());
        Villager villager = level != null && level.getEntity(visit.villager()) instanceof Villager v ? v : null;
        long now = NpcDemandService.now(server);
        boolean valid = rules.npcService() && record != null && villager != null && record.npcService && record.lifecycle == ShopRecord.Lifecycle.OPEN
                && !record.blocked() && record.binding != null && BlockPos.of(record.binding.pos()).equals(visit.register())
                && ShopBindingService.registerLoaded(server, record) && now - visit.started() <= rules.visitDuration()
                && ShopCustomerRules.eligible(villager)
                && record.offer(visit.offer()).filter(o -> o.revision() == visit.revision() && NpcProductCatalog.eligible(o)
                        && record.stock.count(o.key()) >= o.lot()).isPresent()
                && PrincipalPermission.mayUse(level, record.owner, visit.register()).allowed();
        if (!valid) {
            end(server, state, visit);
            return;
        }
        if (!ShopCustomerRules.arrived(villager, visit.register())) {
            steer(villager, visit.register());
            return;
        }
        ShopOffer offer = record.offer(visit.offer()).orElseThrow();
        if (record.proceeds + offer.price() > rules.proceedsCapacity()) {
            end(server, state, visit);
            return;
        }
        int limit = offer.key().resolve().map(i -> i.getDefaultInstance().getMaxStackSize()).orElse(1);
        var plan = new ShopTransfer.Plan(ShopTransaction.Kind.NPC_SALE, offer.id(), List.of(),
                List.of(new ShopTransaction.Goods(offer.key(), offer.lot(), limit)), List.of(), 0, offer.price(), null, 0);
        ShopTransfer.villager(store, record.id, villager.getUUID(), level.getGameTime(), plan,
                transaction -> NpcDemandService.debit(server, transaction, record.owner, record.id, villager.getUUID()));
        end(server, state, visit);
        ShopProjection.publish(server, record.id);
    }

    private static void end(MinecraftServer server, State state, Visit visit) {
        state.visits.remove(visit.shop(), visit);
        state.lastVisit.put(visit.shop(), NpcDemandService.now(server));
        ServerLevel level = ShopBindingService.level(server, visit.dimension());
        if (level != null && level.getEntity(visit.villager()) instanceof Villager villager) {
            villager.getBrain().getMemory(MemoryModuleType.WALK_TARGET)
                    .filter(target -> target.getTarget().currentBlockPosition().equals(visit.register()))
                    .ifPresent(target -> villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET));
        }
    }

}
