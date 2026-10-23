package io.github.dirtyredz.herbalwitch.platform.permission;

import com.mojang.authlib.GameProfile;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.Denial;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.ProtectionAdapters;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import java.util.*;

/** UUID-aware automation authorization. Unsupported claim systems refuse even hostile targets. */
public final class PrincipalPermission {
    @FunctionalInterface public interface Policy { Denial check(ServerLevel level, UUID principal, BlockPos position); }
    private static final List<Policy> POLICIES = new java.util.concurrent.CopyOnWriteArrayList<>();
    private PrincipalPermission() {}
    public static void register(Policy policy) { POLICIES.add(policy); }
    public static Denial mayUse(ServerLevel level, UUID principal, BlockPos position) {
        if (principal == null || !level.hasChunkAt(position) || !level.getWorldBorder().isWithinBounds(position) || level.isOutsideBuildHeight(position)) return Denial.UNSUPPORTED;
        if (ProtectionAdapters.restricted()) return Denial.CLAIMS;
        var online = OnlinePlayers.find(principal).orElse(null);
        if (online != null) {
            if (!online.isAlive() || online.isSpectator() || !level.mayInteract(online, position)) return Denial.SPAWN_PROTECTION;
        } else {
            // The vanilla dedicated-server spawn rule has a complete UUID answer; no surrogate player.
            var server = level.getServer(); var spawn = level.getSharedSpawnPos(); int radius = server.getSpawnProtectionRadius();
            if (server.isDedicatedServer() && level.dimension() == Level.OVERWORLD && radius > 0 && !server.getPlayerList().getOps().isEmpty()
                    && !server.getPlayerList().isOp(new GameProfile(principal, "Familiar"))
                    && Math.max(Math.abs(position.getX() - spawn.getX()), Math.abs(position.getZ() - spawn.getZ())) <= radius) return Denial.SPAWN_PROTECTION;
        }
        for (var policy : POLICIES) { var denial = policy.check(level, principal, position); if (!denial.allowed()) return denial; }
        return Denial.NONE;
    }
}
