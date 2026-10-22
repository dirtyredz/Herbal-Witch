package io.github.dirtyredz.herbalwitch.companions.animals.trust;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.Optional;

/** The provider exists on every mob; care state is allocated only on the first successful mutation. */
public final class AnimalCapability {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("herbalwitch", "animals/care");
    public static final Capability<AnimalCapability> TYPE = CapabilityManager.get(new CapabilityToken<>() {});
    private AnimalCodec.Loaded loaded;
    private long revision;

    public static Optional<AnimalCapability> of(Mob mob) { return mob.getCapability(TYPE).resolve(); }
    public Optional<AnimalState> state() { return loaded == null ? Optional.empty() : Optional.of(loaded.state()); }
    public boolean writable() { return loaded == null || loaded.writable(); }
    public long revision() { return revision; }
    public String problem() { return loaded == null ? "" : loaded.problem(); }

    AnimalState initialize() {
        if (!writable()) throw new IllegalStateException("Preserved animal state");
        if (loaded == null) loaded = AnimalCodec.read(null);
        return loaded.state();
    }

    void changed() { revision++; }

    public CompoundTag save() { return loaded == null ? new CompoundTag() : AnimalCodec.write(loaded); }
    public void load(CompoundTag tag) {
        loaded = tag.isEmpty() ? null : AnimalCodec.read(tag);
        changed();
        if (!writable()) LogUtils.getLogger().error("Animal care preserved without mutation: {}", problem());
    }
}
