package io.github.dirtyredz.herbalwitch.companions.animals.species;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/** Reads live tags on demand. No scan or cache can retain revoked participation after reload. */
public final class AnimalEligibility {
    private AnimalEligibility() {}

    public static Optional<AnimalProfile> profile(Mob mob) {
        var type = mob.getType();
        return resolve(EntityType.getKey(type).toString(), mob instanceof Animal,
                type.is(AnimalTags.ELIGIBLE), type.is(AnimalTags.EXCLUDED) || type.is(EntityActionPermission.PROTECTED)
                        || type.getCategory() == MobCategory.MONSTER || mob instanceof net.minecraft.world.entity.monster.Enemy || !mob.isAlive() || mob.isRemoved(),
                AnimalServerConfig.current().modded(), baitGroups(type));
    }

    public static Set<Bait> baitGroups(EntityType<?> type) {
        Set<Bait> groups = EnumSet.noneOf(Bait.class);
        for (Bait bait : Bait.values()) if (type.is(AnimalTags.bait(bait))) groups.add(bait);
        return groups;
    }

    /** Pure selection seam used by the independent matrix tests. Ambiguity fails closed. */
    public static Optional<AnimalProfile> resolve(String id, boolean animal, boolean eligible, boolean excluded,
                                                  boolean modded, Set<Bait> groups) {
        if (!eligible || excluded || id.startsWith("doggytalents:") || id.startsWith("doggytalentsnext:") || groups.size() != 1)
            return Optional.empty();
        Bait bait = groups.iterator().next();
        var known = AnimalProfiles.find(id);
        if (known.isPresent()) {
            var p = known.get();
            return Optional.of(new AnimalProfile(id, bait, p.ownership(), p.goalAi(), p.capturable()));
        }
        if (!modded || !animal || id.startsWith("minecraft:")) return Optional.empty();
        return Optional.of(new AnimalProfile(id, bait, AnimalProfile.Ownership.UNKNOWN, false, false));
    }
}
