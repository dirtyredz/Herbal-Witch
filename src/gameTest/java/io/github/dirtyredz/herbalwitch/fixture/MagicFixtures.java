package io.github.dirtyredz.herbalwitch.fixture;

import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.CircleLayout;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.RitualCircleBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.stations.block.ProcessStationEntity;
import io.github.dirtyredz.herbalwitch.stations.process.authorization.StationOperations;
import io.github.dirtyredz.herbalwitch.stations.process.execution.ProcessExecutionPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

/**
 * Milestone 7 setup for GameTests: the witch's Start/Resume/Cancel on a station (the same server code
 * a menu-button click reaches), a witch's energy, and a complete ritual circle around an altar.
 */
public final class MagicFixtures {
    /** The circle's origin in the empty template: with the altar at {@link WitchFixtures#ALTAR} facing north. */
    public static final BlockPos CIRCLE = CircleLayout.circleOrigin(WitchFixtures.ALTAR, Direction.NORTH);

    private MagicFixtures() {
    }

    /** Presses a station operation as {@code player}; returns the outcome suffix. */
    public static String operate(GameTestHelper helper, ServerPlayer player, BlockPos station, StationOperations.Operation op) {
        ProcessStationEntity entity = (ProcessStationEntity) helper.getBlockEntity(station);
        return StationOperations.operate(player, op, entity.station(), entity.magic(),
                new ProcessExecutionPolicy.Site(helper.getLevel(), helper.absolutePos(station)));
    }

    public static int energy(ServerPlayer player) {
        return WitchStatusService.energyUnits(player);
    }

    public static int practice(ServerPlayer player) {
        return WitchCapability.of(player).map(c -> c.state().practiceSpent()).orElse(0);
    }

    /** Sets a witch's energy (units: hundredths). */
    public static void setEnergy(ServerPlayer player, int units) {
        WitchCapability.of(player).ifPresent(c -> c.state().setEnergyUnits(units));
    }

    /** A complete altar facing north at {@link WitchFixtures#ALTAR} inside a complete ritual circle, on a stone floor. */
    public static void circle(GameTestHelper helper) {
        WitchFixtures.altar(helper);
        ((RitualCircleBlock) RitualBlocks.CIRCLE.get()).placeCircle(helper.getLevel(), helper.absolutePos(CIRCLE), Direction.NORTH,
                Block.UPDATE_ALL);
        WorldFixtures.check(helper.getBlockState(WitchFixtures.ALTAR).getBlock() instanceof AltarBlock, "the altar stands");
    }
}
