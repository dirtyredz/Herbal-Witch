package io.github.dirtyredz.herbalwitch.companions.familiars.ability.action;

import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.GardenApplication;
import io.github.dirtyredz.herbalwitch.companions.familiars.ability.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class FamiliarGardenAid {
    private FamiliarGardenAid() {}
    public static boolean apply(Mob familiar, BlockPos position) {
        var record = FamiliarReconciliation.record(familiar).orElse(null); if (record == null || !familiar.level().hasChunkAt(position)
                || familiar.distanceToSqr(Vec3.atCenterOf(position)) > 32 * 32) return false;
        var owner = OnlinePlayers.find(record.owner()).orElse(null); if (owner == null
                || !io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission.mayUse(owner.serverLevel(), record.owner(), position).allowed()) return false;
        var before = familiar.level().getBlockState(position);
        if (!(before.getBlock() instanceof HerbCropBlock crop) || crop.isMaxAge(before)) return false;
        return FamiliarAbilityService.perform(familiar, FamiliarPower.GARDEN, () -> GardenApplication.growCrop(owner, familiar.level(), position));
    }
}
