package io.github.dirtyredz.herbalwitch.apothecary.brewing.application;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.PestRepellentEvents;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.TimedEffect;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat.WeaponCoating;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.RepellentProfiles;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.TargetFilters;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.platform.time.TickTime;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import java.util.function.Supplier;

/** Body oils refresh effects; venom coats an off-hand blade. Lantern oil remains pour-only. */
public final class OilApplication {
    public static final int DURATION = 90 * TickTime.SECOND;

    public enum Oil {
        FISHING(() -> MobEffects.LUCK),
        PEST(() -> PreparationEffects.PEST_REPELLENT.get()),
        LANTERN(null),
        VENOM(null),
        THORN(() -> PreparationEffects.THORNS.get()),
        UNDEAD_REPELLENT(() -> RepellentProfiles.UNDEAD.repellent()),
        ARTHROPOD_REPELLENT(() -> RepellentProfiles.ARTHROPOD.repellent());

        private final TimedEffect effect;

        Oil(Supplier<MobEffect> effect) {
            this.effect = effect == null ? null : new TimedEffect(effect, DURATION, 0);
        }
    }

    private OilApplication() { }

    public static boolean usable(Player player, Oil oil) {
        return oil != Oil.VENOM || WeaponCoating.eligible(player.getOffhandItem());
    }

    public static boolean apply(Player player, Oil oil) {
        if (!usable(player, oil)) {
            return false;
        }
        if (oil.effect != null) {
            player.addEffect(oil.effect.instance());
        }
        switch (oil) {
            case PEST -> PestRepellentEvents.clearTargets(player);
            case VENOM -> WeaponCoating.coat(player.getOffhandItem(), player.level().getGameTime());
            case UNDEAD_REPELLENT -> TargetFilters.clearTargets(player, player, RepellentProfiles.UNDEAD::matches);
            case ARTHROPOD_REPELLENT -> TargetFilters.clearTargets(player, player, RepellentProfiles.ARTHROPOD::matches);
            default -> { }
        }
        return true;
    }
}
