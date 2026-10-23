package io.github.dirtyredz.herbalwitch.companions.familiars.species.flying;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;

/** Flight mechanics shared by the three adapters, independent of command decisions. */
public final class FamiliarFlight {
    private FamiliarFlight() {}
    public static FlyingPathNavigation navigation(Mob mob, Level level) {
        var navigation = new FlyingPathNavigation(mob, level);
        navigation.setCanOpenDoors(false); navigation.setCanFloat(true); navigation.setCanPassDoors(true);
        return navigation;
    }
    public static FlyingMoveControl control(Mob mob) { return new FlyingMoveControl(mob, 20, true); }
}
