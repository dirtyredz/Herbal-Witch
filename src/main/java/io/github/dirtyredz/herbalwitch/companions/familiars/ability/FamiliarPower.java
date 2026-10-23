package io.github.dirtyredz.herbalwitch.companions.familiars.ability;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import io.github.dirtyredz.herbalwitch.companions.familiars.equipment.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.preparation.*;
import net.minecraft.world.entity.Mob;
import java.util.*;

public enum FamiliarPower {
    THREATS(3,200), HERBS(3,200), CREATURES(3,200), TRACK(3,200), RETRIEVE(5,200), DISTRACT(4,300), INTERCEPT(5,300), VENOM(4,200), GARDEN(5,600), REPEL(3,40);
    private final int energy, ticks;
    FamiliarPower(int energy, int ticks) { this.energy = energy; this.ticks = ticks; }
    public String key() { return name().toLowerCase(Locale.ROOT); }
    public int cost(FamiliarRecord record) { return FamiliarBuffRules.cost(record.state(), energy); }
    public boolean guardOnly() { return this == INTERCEPT || this == VENOM || this == REPEL; }
    public boolean sensing() { return this == THREATS || this == HERBS || this == CREATURES || this == TRACK; }
    public int cooldown(Mob mob, FamiliarRecord record) {
        if (this == REPEL && FamiliarBuffRules.active(record.state(), "hearthkeeper") && FamiliarBuffEffects.homeGuard(mob, record)) return 30;
        boolean gear = FamiliarEquipmentRules.equipped(record.species(), FamiliarEquipmentInventory.item(record, 1));
        if (gear && (this == THREATS && record.species().equals("cat") || this == HERBS && record.species().equals("owl")
                || this == CREATURES && record.species().equals("bat") || this == VENOM && record.species().equals("snake") || this == GARDEN)) return ticks * 4 / 5;
        return ticks;
    }
    public boolean available(Mob mob, FamiliarRecord record) {
        return available(record.species(), mob.level().isNight(), FamiliarBuffRules.active(record.state(), "nightwatch"));
    }
    public boolean available(String species, boolean night, boolean nightwatch) {
        return switch (this) {
            case THREATS -> species.equals("cat") || night && (species.equals("owl") || nightwatch);
            case HERBS -> species.equals("owl") || species.equals("rabbit");
            case CREATURES -> species.equals("snake") || species.equals("bat");
            case TRACK -> species.equals("wolf");
            case RETRIEVE -> species.equals("raven") || species.equals("fox");
            case DISTRACT -> species.equals("fox");
            case INTERCEPT -> species.equals("wolf");
            case VENOM -> species.equals("snake");
            case GARDEN -> species.equals("rabbit");
            case REPEL -> true;
        };
    }
}
