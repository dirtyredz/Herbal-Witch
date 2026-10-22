package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessions;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;

/**
 * The garden blessing, a circle ritual: a witch with a complete circle chooses an herb bed within
 * {@link #REACH} blocks of the altar and, after the ritual's time, every eligible herb bed of the 5 x 5
 * square around it at that height is enriched (each gives one extra fresh herb at its next mature
 * harvest, the beds' established bonus; nothing grows at once and enrichment does not stack). The plan
 * is checked when it starts and again as a preflight before anything changes; then the offering, the
 * energy and every bed change happen together, and the soil tonic's bottle comes back. Nothing is
 * consumed by a ritual that does not complete.
 */
public final class GardenBlessingAction implements AltarSessionService.Handler {
    /** Blocks from the altar's origin to the chosen bed. */
    public static final double REACH = 8.0;

    /** What a blessing session holds. */
    public record Payload(BlockPos bed, AltarOffering offering, int energyUnits) {
    }

    public static String begin(ServerPlayer witch, AltarMenu menu, Optional<BlockPos> bed) {
        if (!WitchStatusService.isWitch(witch)) {
            return "garden_blessing.not_witch";
        }
        ServerLevel level = witch.serverLevel();
        Optional<String> circle = CircleRituals.incomplete(level, menu.origin());
        if (circle.isPresent()) {
            return circle.get();
        }
        if (bed.isEmpty() || !level.isLoaded(bed.get()) || !(level.getBlockState(bed.get()).getBlock() instanceof HerbBedBlock)
                || Vec3.atCenterOf(bed.get()).distanceTo(Vec3.atCenterOf(menu.origin())) > REACH) {
            return "garden_blessing.no_bed";
        }
        GardenBlessingPlan.Plan plan = plan(level, witch, bed.get());
        if (!plan.ok()) {
            return "garden_blessing." + plan.outcome().name().toLowerCase(java.util.Locale.ROOT);
        }
        int units = EnergyService.units(WitchRules.server().workings().gardenBlessingCost());
        if (WitchStatusService.check(witch, units) != EnergyService.Result.OK) {
            return "not_enough_energy";
        }
        Optional<AltarOffering> offering = AltarOffering.capture(witch, AltarOffering.requirements(AltarActionSpec.GARDEN_BLESSING));
        if (offering.isEmpty()) {
            return "missing_offering";
        }
        RitualSite site = AltarSessionService.site(level, menu.origin());
        if (AltarSessionService.at(site).isPresent() || AltarSessionService.of(witch.getUUID()).isPresent()) {
            return "busy";
        }
        AltarSessions sessions = AltarSessionService.sessions().orElseThrow();
        AltarSession session = new AltarSession(sessions.nextId(), AltarActionSpec.GARDEN_BLESSING, site, Optional.of(witch.getUUID()),
                witch.getUUID(), WitchRules.server().workings().gardenBlessingTicks(), Map.of(witch.getUUID(), menu.nonce()));
        session.setPayload(new Payload(bed.get().immutable(), offering.get(), units));
        if (AltarSessionService.begin(session).isPresent()) {
            return "busy";
        }
        CircleRituals.glow(session, true);
        return "garden_blessing.begun";
    }

    /** The beds this witch's blessing of {@code bed} would enrich now. */
    public static GardenBlessingPlan.Plan plan(ServerLevel level, ServerPlayer witch, BlockPos bed) {
        return GardenBlessingPlan.of(bed, level::isLoaded, pos -> {
            BlockState state = level.getBlockState(pos);
            return state.getBlock() instanceof HerbBedBlock && !state.getValue(HerbBedBlock.ENRICHED);
        }, pos -> ActionPermissionService.mayModify(ActionContext.of(witch, level, pos)));
    }

    @Override
    public Optional<String> invalid(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        Optional<ServerPlayer> witch = InitiationEvents.player(session.subject());
        if (witch.isEmpty() || !p.offering().intact(witch.get())) {
            return Optional.of("inputs_changed");
        }
        if (WitchStatusService.check(witch.get(), p.energyUnits()) != EnergyService.Result.OK) {
            return Optional.of("not_enough_energy");
        }
        return CircleRituals.broken(session);
    }

    @Override
    public boolean paused(MinecraftServer server, AltarSession session) {
        return CircleRituals.paused(session);
    }

    @Override
    public void tick(MinecraftServer server, AltarSession session) {
    }

    @Override
    public void complete(MinecraftServer server, AltarSession session) {
        Payload p = session.payload();
        ServerPlayer witch = InitiationEvents.player(session.subject()).orElse(null);
        ServerLevel level = AltarSessionService.level(session.site()).orElse(null);
        if (witch == null || level == null || !p.offering().intact(witch)) {
            AltarSessionService.cancel(session, "inputs_changed");
            return;
        }
        if (CircleRituals.incomplete(level, CircleRituals.altar(session)).isPresent()) {
            AltarSessionService.cancel(session, "circle_broken");
            return;
        }
        GardenBlessingPlan.Plan plan = plan(level, witch, p.bed());
        if (!plan.ok()) {
            AltarSessionService.cancel(session, "garden_changed");
            return;
        }
        if (WitchStatusService.check(witch, p.energyUnits()) != EnergyService.Result.OK) {
            AltarSessionService.cancel(session, "not_enough_energy");
            return;
        }
        if (!AltarSessionService.completeOnce(session)) {
            return;
        }
        CircleRituals.glow(session, false);
        boolean done = CircleRituals.pay(witch, p.offering(), p.energyUnits(), () -> {
            for (BlockPos pos : plan.changes()) {
                level.setBlock(pos, level.getBlockState(pos).setValue(HerbBedBlock.ENRICHED, true), Block.UPDATE_CLIENTS);
            }
            return true;
        });
        if (!done) {
            AltarSessionService.notice(witch, "cancel.not_enough_energy");
            return;
        }
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (!witch.getInventory().add(bottle)) {
            witch.drop(bottle, false);
        }
        DiscoveryService.grant(witch, AltarEntries.key(AltarActionSpec.GARDEN_BLESSING));
        AltarSessionService.notice(witch, "garden_blessing.done");
    }

    @Override
    public void cancelled(MinecraftServer server, AltarSession session, String reason) {
        CircleRituals.glow(session, false);
    }
}
