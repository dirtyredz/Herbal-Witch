package io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import io.github.dirtyredz.herbalwitch.companions.familiars.equipment.*;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.ItemPickupPermission;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;

public final class FamiliarRetrievalRules {
    public static final TagKey<Item> SMALL_OBJECTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("herbalwitch", "familiars/retrievable"));
    private FamiliarRetrievalRules() {}
    public static boolean simple(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(SMALL_OBJECTS) || stack.hasTag() || stack.getMaxStackSize() <= 1 || stack.isDamageableItem()
                || stack.getItem() instanceof BlockItem block && block.getBlock() instanceof net.minecraft.world.level.block.EntityBlock
                || stack.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).isPresent()) return false;
        var encoded = stack.save(new CompoundTag());
        return !encoded.contains("ForgeCaps") || encoded.getCompound("ForgeCaps").isEmpty();
    }
    public static boolean eligible(ServerPlayer owner, Mob mob, FamiliarRecord record, ItemEntity source) {
        if (source.getClass() != ItemEntity.class || source.isRemoved() || source.level() != mob.level() || !simple(source.getItem())
                || !source.getPersistentData().isEmpty() || !mob.hasLineOfSight(source)
                || mob.distanceToSqr(source) > FamiliarServerConfig.current().senseRadius() * FamiliarServerConfig.current().senseRadius()
                || !ItemPickupPermission.allowed(owner, source) || FamiliarPickupReceipts.get(owner.server).retired(source.getUUID())) return false;
        var nativeData = new CompoundTag(); source.addAdditionalSaveData(nativeData);
        boolean attributed = nativeData.hasUUID("Thrower") && nativeData.getUUID("Thrower").equals(owner.getUUID())
                || nativeData.hasUUID("Owner") && nativeData.getUUID("Owner").equals(owner.getUUID());
        if (!attributed && !FamiliarServerConfig.current().publicRetrieval()) return false;
        var cargo = FamiliarEquipmentInventory.cargo(record);
        int capacity = FamiliarEquipmentRules.cargoCapacity(record.species(), FamiliarEquipmentInventory.item(record, 1));
        return (cargo.isEmpty() || ItemStack.isSameItemSameTags(cargo, source.getItem())) && cargo.getCount() + source.getItem().getCount() <= capacity;
    }
}
