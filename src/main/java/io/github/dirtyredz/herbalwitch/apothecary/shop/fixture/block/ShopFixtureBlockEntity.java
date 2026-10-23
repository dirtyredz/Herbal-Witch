package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.UUID;

/**
 * Identity of a commerce fixture: a random instance UUID, the shop it claims and (for a register) the
 * binding generation it was bound under. A claim is only a reference: the shop record decides whether
 * this exact dimension, position, instance and generation belong to it, so a copied or cloned entity
 * elsewhere never binds. Stock, proceeds and offers are never stored here. Unknown data is preserved
 * untouched and makes the fixture inert.
 */
public class ShopFixtureBlockEntity extends BlockEntity {
    public static final String KEY = "Shop";
    private static final Set<String> FIELDS = Set.of("schema", "instance", "shop", "generation");
    private UUID instance = UUID.randomUUID();
    private UUID shop;
    private long generation;
    private CompoundTag unknown;

    public ShopFixtureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public UUID instance() {
        return instance;
    }

    /** The claimed shop, or null when unbound. A claim is not proof of membership. */
    public UUID shop() {
        return shop;
    }

    public long generation() {
        return generation;
    }

    public boolean writable() {
        return unknown == null;
    }

    /** Records a claim made by the shop service after the shop record accepted it. */
    public void claim(UUID shop, long generation) {
        if (!writable()) return;
        this.shop = shop;
        this.generation = generation;
        setChanged();
    }

    /** Drops a claim the record no longer recognises; a fresh instance prevents identical copies. */
    public void release() {
        if (!writable()) return;
        shop = null;
        generation = 0;
        instance = UUID.randomUUID();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (unknown != null) {
            tag.put(KEY, unknown.copy());
            return;
        }
        CompoundTag own = new CompoundTag();
        own.putInt("schema", 1);
        own.putUUID("instance", instance);
        if (shop != null) {
            own.putUUID("shop", shop);
            own.putLong("generation", generation);
        }
        tag.put(KEY, own);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        unknown = null;
        shop = null;
        generation = 0;
        if (!tag.contains(KEY)) return; // a freshly placed fixture keeps its new instance
        CompoundTag own = tag.getCompound(KEY);
        boolean valid = own.getInt("schema") == 1 && own.hasUUID("instance") && FIELDS.containsAll(own.getAllKeys())
                && (!own.contains("shop") || own.hasUUID("shop") && own.contains("generation", CompoundTag.TAG_LONG));
        if (!valid) {
            unknown = own.copy();
            return;
        }
        instance = own.getUUID("instance");
        if (own.hasUUID("shop")) {
            shop = own.getUUID("shop");
            generation = own.getLong("generation");
        }
    }
}
