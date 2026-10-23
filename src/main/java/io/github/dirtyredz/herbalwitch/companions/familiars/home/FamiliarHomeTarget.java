package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public record FamiliarHomeTarget(Vec3 position, String reason) {
    public static FamiliarHomeTarget resolve(Mob mob, FamiliarRecord record) {
        var state = record.state(); if (!state.contains("home")) return new FamiliarHomeTarget(null, "no_home");
        var home = state.getCompound("home");
        if (!home.getString("dimension").equals(mob.level().dimension().location().toString())) return new FamiliarHomeTarget(null, "other_dimension");
        var position = BlockPos.of(home.getLong("position"));
        if (!mob.level().hasChunkAt(position)) return new FamiliarHomeTarget(null, "home_unloaded");
        // A newer/unknown block schema cannot prove that the recorded instance disappeared.
        if (mob.level().getBlockEntity(position) instanceof FamiliarHomeBlockEntity block && !block.writable()) return new FamiliarHomeTarget(null, "unavailable");
        if (!(mob.level().getBlockEntity(position) instanceof FamiliarHomeIdentity identity) || !home.hasUUID("instance")
                || !identity.instance().equals(home.getUUID("instance")) || !record.owner().equals(identity.owner()) || !identity.accepts(record.species())) {
            FamiliarStore.get(mob.getServer()).update(record.change(s -> { s.remove("home"); s.putString("status", "home_missing"); }), true);
            return new FamiliarHomeTarget(null, "home_missing");
        }
        return new FamiliarHomeTarget(Vec3.atBottomCenterOf(position.above()), "");
    }
}
