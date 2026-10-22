package io.github.dirtyredz.herbalwitch.client.ritual;

import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Optional;

/**
 * Which herb bed and which ward stone this player last looked at, so the altar screen can offer the
 * circle's rituals for them ("look at the bed, then go to the altar"). Client memory only: the request
 * names the position and the server revalidates everything; forgotten on disconnect, a dimension
 * change, or after {@link #MEMORY} ticks.
 */
public final class RitualTargets {
    public static final int MEMORY = 20 * 60 * 2;
    private static Target bed;
    private static Target ward;

    private record Target(BlockPos pos, long tick, Object level) {
    }

    private RitualTargets() {
    }

    public static void register(IEventBus forge) {
        forge.addListener(RitualTargets::clientTick);
        forge.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> clear());
    }

    private static void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || mc.level == null || mc.player == null) {
            return;
        }
        if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            Block block = mc.level.getBlockState(hit.getBlockPos()).getBlock();
            if (block instanceof HerbBedBlock) {
                bed = new Target(hit.getBlockPos().immutable(), mc.level.getGameTime(), mc.level);
            } else if (block instanceof WardStoneBlock) {
                ward = new Target(hit.getBlockPos().immutable(), mc.level.getGameTime(), mc.level);
            }
        }
    }

    public static Optional<BlockPos> bed() {
        return valid(bed);
    }

    public static Optional<BlockPos> ward() {
        return valid(ward);
    }

    private static Optional<BlockPos> valid(Target target) {
        Minecraft mc = Minecraft.getInstance();
        if (target == null || mc.level == null || target.level() != mc.level || mc.level.getGameTime() - target.tick() > MEMORY) {
            return Optional.empty();
        }
        return Optional.of(target.pos());
    }

    public static void clear() {
        bed = null;
        ward = null;
    }
}
