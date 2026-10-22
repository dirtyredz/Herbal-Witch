package io.github.dirtyredz.herbalwitch.apothecary.brewing.application.advanced;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.DrinkApplication;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat.WeaponCoating;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.RepellentProfiles;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.TargetFilters;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * The Milestone 7 oils, applied server side once their use finishes. Venom coats the eligible blade
 * in the user's off hand ({@link WeaponCoating}); without one nothing happens and nothing is used.
 * Thorn oil gives Thorns; the family repellents keep their family from choosing the wearer and clear
 * that family's existing targets on the wearer nearby. Durations refresh, never add.
 */
public final class OilApplications {
    public static final int DURATION = 90 * DrinkApplication.SECOND;

    /** The oils. */
    public enum Oil {
        VENOM,
        THORN,
        UNDEAD_REPELLENT,
        ARTHROPOD_REPELLENT
    }

    private OilApplications() {
    }

    /** Whether the oil can be applied now (venom needs an eligible off-hand blade). */
    public static boolean usable(Player player, Oil oil) {
        return oil != Oil.VENOM || WeaponCoating.eligible(player.getOffhandItem());
    }

    /** Applies the oil; false (nothing happened) when it cannot be applied. */
    public static boolean apply(Player player, Oil oil) {
        if (!usable(player, oil)) {
            return false;
        }
        switch (oil) {
            case VENOM -> WeaponCoating.coat(player.getOffhandItem(), player.level().getGameTime());
            case THORN -> player.addEffect(new MobEffectInstance(PreparationEffects.THORNS.get(), DURATION, 0));
            case UNDEAD_REPELLENT -> repel(player, RepellentProfiles.UNDEAD);
            case ARTHROPOD_REPELLENT -> repel(player, RepellentProfiles.ARTHROPOD);
        }
        return true;
    }

    private static void repel(Player player, RepellentProfiles profile) {
        player.addEffect(new MobEffectInstance(profile.repellent(), DURATION, 0));
        TargetFilters.clearTargets(player, player, profile::matches);
    }
}
