package io.github.dirtyredz.herbalwitch.client.magic.ritual;

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
    private static java.util.UUID creature;
    private static String creatureName = "";
    private static long creatureTick;
    private static Object creatureLevel;

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
        if (mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit && hit.getEntity() instanceof net.minecraft.world.entity.Mob mob)
            remember(mob.getUUID(), mob.getName().getString());
    }

    /** Also used by an owner's roster selection. It is intent only; the server resolves and validates it. */
    public static void remember(java.util.UUID id, String name) {
        var level = Minecraft.getInstance().level; if (level == null) return;
        creature = id; creatureName = name; creatureTick = level.getGameTime(); creatureLevel = level;
    }
    public static Optional<java.util.UUID> creature() {
        var level = Minecraft.getInstance().level;
        return level == null || level != creatureLevel || level.getGameTime() - creatureTick > MEMORY ? Optional.empty() : Optional.ofNullable(creature);
    }
    public static String creatureName() { return creature().isPresent() ? creatureName : ""; }

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
        creature = null; creatureName = ""; creatureLevel = null;
    }
}
