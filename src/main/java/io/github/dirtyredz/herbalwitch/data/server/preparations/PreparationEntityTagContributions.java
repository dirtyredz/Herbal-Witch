package io.github.dirtyredz.herbalwitch.data.server.preparations;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.RepellentProfiles;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationTags;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchEntityTagsProvider;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import net.minecraft.world.entity.EntityType;

/** M7 memberships, unchanged by the shared writer extraction. */
public final class PreparationEntityTagContributions {
    private PreparationEntityTagContributions() {}
    public static void addTo(HerbalWitchEntityTagsProvider.Sink sink) {
        sink.tag(PreparationTags.REPELLED_PESTS).add(EntityType.SILVERFISH, EntityType.ENDERMITE);
        sink.tag(RepellentProfiles.UNDEAD.tag()).add(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.SKELETON,
                EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.PHANTOM);
        sink.tag(RepellentProfiles.ARTHROPOD.tag()).add(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE);
        sink.tag(ControlEffects.IMMUNE).add(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WARDEN);
        sink.tag(ControlEffects.ALLOWED);
        sink.tag(EntityActionPermission.PROTECTED).add(EntityType.VILLAGER, EntityType.WANDERING_TRADER);
    }
}
