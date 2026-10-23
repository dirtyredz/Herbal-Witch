package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarMenuAccess;
import io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.UUID;

/** Canonical assignment is committed before either block entity's disposable cache is repaired. */
public final class FamiliarHomeService {
    private FamiliarHomeService() {}
    public static boolean mayUse(ServerPlayer owner, FamiliarHomeBlockEntity home, boolean claim) {
        if (!FamiliarMenuAccess.roster(owner) || !home.writable() || home.isRemoved() || home.getLevel() != owner.level()
                || !owner.canReach(home.getBlockPos(), 0) || home.owner() == null && !claim
                || home.owner() != null && !owner.getUUID().equals(home.owner())
                || !PrincipalPermission.mayUse(owner.serverLevel(), owner.getUUID(), home.getBlockPos()).allowed()) return false;
        var ray = owner.level().clip(new ClipContext(owner.getEyePosition(), Vec3.atCenterOf(home.getBlockPos()), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, owner));
        return ray.getType() == HitResult.Type.MISS || ray.getBlockPos().equals(home.getBlockPos());
    }
    public static boolean matches(FamiliarHomeBlockEntity home, CompoundTag reference) {
        return home.getLevel() != null && reference.hasUUID("instance") && home.instance().equals(reference.getUUID("instance"))
                && home.getBlockPos().asLong() == reference.getLong("position") && home.getLevel().dimension().location().toString().equals(reference.getString("dimension"));
    }
    public static String assign(ServerPlayer owner, FamiliarHomeBlockEntity home, UUID bond) {
        if (!mayUse(owner, home, false)) return "permission";
        var store = FamiliarStore.get(owner.server); var record = store.record(bond).orElse(null);
        if (record == null || !store.writable(bond) || !record.owner().equals(owner.getUUID()) || record.state().contains("receipt")
                || record.phase() != FamiliarRecord.Phase.ACTIVE && record.phase() != FamiliarRecord.Phase.INCAPACITATED) return "unavailable";
        if (!home.accepts(record.species())) return "home_incompatible";
        if (store.homes().occupant(home.instance()).filter(id -> !id.equals(bond)).isPresent()) return "home_occupied";
        var old = record.state().getCompound("home"); var reference = new CompoundTag();
        reference.putString("dimension", owner.level().dimension().location().toString()); reference.putLong("position", home.getBlockPos().asLong()); reference.putUUID("instance", home.instance());
        if (!store.update(record.change(s -> { s.put("home", reference); s.putInt("homeRest", 0); s.putString("status", ""); }), true)) return "unavailable";
        home.repair(); repairLoaded(owner.server, old); return "home_assigned";
    }
    private static void repairLoaded(net.minecraft.server.MinecraftServer server, CompoundTag reference) {
        for (var level : server.getAllLevels()) if (level.dimension().location().toString().equals(reference.getString("dimension"))) {
            var pos = BlockPos.of(reference.getLong("position"));
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof FamiliarHomeBlockEntity home && matches(home, reference)) home.repair();
        }
    }
    public static void removed(FamiliarHomeBlockEntity home) {
        if (!(home.getLevel() instanceof ServerLevel level)) return;
        var store = FamiliarStore.get(level.getServer()); if (!store.ready()) return;
        store.homes().occupant(home.instance()).flatMap(store::record).filter(r -> matches(home, r.state().getCompound("home"))).ifPresent(r ->
                store.update(r.change(s -> { s.remove("home"); s.putInt("homeRest", 0); s.putString("status", "home_missing"); }), true));
    }
}
