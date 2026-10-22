package io.github.dirtyredz.herbalwitch.companions.animals.care.interaction;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;

/** Thin adapter called only by EntityInteract, before native mount/feed handling can consume the click. */
public final class AnimalCareItem extends Item {
    private final Bait bait;
    public AnimalCareItem(Bait bait) { super(new Properties()); this.bait = bait; }
    public Bait bait() { return bait; }
    public AnimalCareAccess.Reason apply(ServerPlayer player, Mob mob, InteractionHand hand) {
        return bait == null ? AnimalCareService.calm(player, mob, hand) : AnimalCareService.feed(player, mob, hand, bait);
    }
}
