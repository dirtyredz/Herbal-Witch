package io.github.dirtyredz.herbalwitch.data.server.preparations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEffects;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent.RepellentProfiles;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** The pests the repellent keeps off: silverfish and endermites. (The mod's only entity tag writer.) */
public class PreparationEntityTagsProvider extends EntityTypeTagsProvider {
    public PreparationEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                         ExistingFileHelper existingFiles) {
        super(output, lookup, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch entity tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PreparationTags.REPELLED_PESTS).add(EntityType.SILVERFISH, EntityType.ENDERMITE);
        tag(RepellentProfiles.UNDEAD.tag()).add(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.SKELETON,
                EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.PHANTOM);
        tag(RepellentProfiles.ARTHROPOD.tag()).add(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE);
        tag(ControlEffects.IMMUNE).add(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WARDEN);
        tag(ControlEffects.ALLOWED); // modded creatures a pack trusts with binding and sleep (none by default)
        tag(EntityActionPermission.PROTECTED).add(EntityType.VILLAGER, EntityType.WANDERING_TRADER);
    }
}
