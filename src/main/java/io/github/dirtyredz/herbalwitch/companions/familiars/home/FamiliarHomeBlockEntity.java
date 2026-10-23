package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarHomes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Identity and a repairable cache only. Never exposes equipment or a capability inventory. */
public final class FamiliarHomeBlockEntity extends BlockEntity implements FamiliarHomeIdentity {
    private UUID instance = UUID.randomUUID(), owner, occupant;
    private CompoundTag unknown;
    public FamiliarHomeBlockEntity(BlockPos pos, BlockState state) { super(FamiliarHomes.TYPE.get(), pos, state); }
    @Override public UUID instance() { return instance; }
    @Override public UUID owner() { return owner; }
    public UUID occupant() { return occupant; }
    public boolean writable() { return unknown == null; }
    @Override public boolean accepts(String species) { return writable() && getBlockState().getBlock() instanceof FamiliarHomeBlock block && block.kind().accepts(species); }
    public void owner(UUID value) { if (writable() && owner == null) { owner = value; setChanged(); } }
    public void repair() {
        if (!(level instanceof ServerLevel server) || !writable()) return;
        var store = FamiliarStore.get(server.getServer()); if (!store.ready()) return;
        UUID current = store.homes().occupant(instance).flatMap(store::record)
                .filter(r -> FamiliarHomeService.matches(this, r.state().getCompound("home")) && r.owner().equals(owner)).map(r -> r.bond()).orElse(null);
        if (!Objects.equals(occupant, current)) { occupant = current; setChanged(); }
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (unknown != null) { tag.put("Home", unknown.copy()); return; }
        var home = new CompoundTag(); home.putInt("schema", 1); home.putUUID("instance", instance);
        if (owner != null) home.putUUID("owner", owner); if (occupant != null) home.putUUID("occupant", occupant);
        tag.put("Home", home);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); owner = null; occupant = null; unknown = null;
        var home = tag.getCompound("Home");
        if (home.getInt("schema") != 1 || !home.hasUUID("instance") || !Set.of("schema", "instance", "owner", "occupant").containsAll(home.getAllKeys())
                || home.contains("owner") && !home.hasUUID("owner") || home.contains("occupant") && !home.hasUUID("occupant")) { unknown = home.copy(); return; }
        instance = home.getUUID("instance"); if (home.hasUUID("owner")) owner = home.getUUID("owner"); if (home.hasUUID("occupant")) occupant = home.getUUID("occupant");
    }
}
