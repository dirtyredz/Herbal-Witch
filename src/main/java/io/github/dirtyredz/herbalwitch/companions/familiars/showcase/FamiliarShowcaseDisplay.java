package io.github.dirtyredz.herbalwitch.companions.familiars.showcase;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarVisualSnapshot;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.*;

/** A visual sample has no bond, owner, equipment inventory, energy or recovery authority. */
public final class FamiliarShowcaseDisplay {
    private static final String KEY = "herbalwitch_familiar_display";
    private FamiliarShowcaseDisplay() {}
    public static boolean isDisplay(net.minecraft.world.entity.Entity entity) { return entity.getPersistentData().getBoolean(KEY); }
    public static void prepare(Mob mob, int appearance) {
        mob.getPersistentData().putBoolean(KEY, true); mob.setNoAi(true); mob.setInvulnerable(true); mob.setPersistenceRequired();
        mob.setCanPickUpLoot(false);
        if (mob instanceof io.github.dirtyredz.herbalwitch.companions.familiars.species.flying.FamiliarRaven raven) raven.appearance(appearance);
    }
    public static FamiliarVisualSnapshot visual(Mob mob) {
        if (!isDisplay(mob)) return null;
        var species = FamiliarCatalog.ALL.stream().filter(s -> net.minecraft.world.entity.EntityType.getKey(mob.getType()).getPath().equals("familiars/entity/" + s.key())).findFirst().orElse(null);
        return species == null ? null : new FamiliarVisualSnapshot(mob.getId(), mob.getUUID(), 1, species.equipment(), CharmCatalog.ALL.get(0).id(), "stay", false);
    }
    public static void drops(LivingDropsEvent event) { if (isDisplay(event.getEntity())) { event.getDrops().clear(); event.setCanceled(true); } }
    public static void experience(LivingExperienceDropEvent event) { if (isDisplay(event.getEntity())) event.setDroppedExperience(0); }
}
