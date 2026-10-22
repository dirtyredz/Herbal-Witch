package io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.repellent;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * The mob families a repellent, an amulet or a ward can name: never arbitrary entity classes, only
 * these explicit, data-tagged families. {@code herbalwitch:undead} (zombie, husk, drowned, skeleton,
 * stray, wither skeleton, phantom) and {@code herbalwitch:arthropods} (spider, cave spider,
 * silverfish, endermite); a datapack may extend either tag.
 */
public enum RepellentProfiles {
    UNDEAD("undead", () -> PreparationEffects.UNDEAD_REPELLENT.get()),
    ARTHROPOD("arthropods", () -> PreparationEffects.ARTHROPOD_REPELLENT.get());

    private final TagKey<EntityType<?>> tag;
    private final Supplier<MobEffect> repellent;

    RepellentProfiles(String tag, Supplier<MobEffect> repellent) {
        this.tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, tag));
        this.repellent = repellent;
    }

    public TagKey<EntityType<?>> tag() {
        return tag;
    }

    /** The body effect that keeps this family from choosing its wearer. */
    public MobEffect repellent() {
        return repellent.get();
    }

    /** Whether the creature belongs to this family. */
    public boolean matches(LivingEntity entity) {
        return entity.getType().is(tag);
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}
