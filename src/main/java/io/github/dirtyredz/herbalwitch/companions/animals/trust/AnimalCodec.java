package io.github.dirtyredz.herbalwitch.companions.animals.trust;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Set;

/** Strict schema 1. Malformed/future payloads are preserved whole, never reset into writable state. */
public final class AnimalCodec {
    public static final int SCHEMA = 1;
    private static final Set<String> FIELDS = Set.of("schema", "relationships", "clock", "hurt", "heal", "calm",
            "calmBlocked", "calmSource", "keeper", "generation");

    public record Loaded(AnimalState state, CompoundTag preserved, String problem) {
        public boolean writable() { return preserved == null; }
    }

    private AnimalCodec() {}

    public static Loaded read(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return new Loaded(new AnimalState(), null, "");
        try {
            require(FIELDS.containsAll(tag.getAllKeys()), "unknown fields");
            require(tag.contains("schema", Tag.TAG_INT) && tag.getInt("schema") == SCHEMA, "unsupported schema");
            AnimalState state = new AnimalState();
            state.clock = number(tag, "clock");
            require(state.clock <= Long.MAX_VALUE - TrustRules.FEED_INTERVAL, "clock overflow");
            state.hurtUntil = gate(tag, "hurt", state.clock, TrustRules.HURT_LOCKOUT);
            state.healUntil = gate(tag, "heal", state.clock, TrustRules.HEAL_INTERVAL);
            state.calmUntil = gate(tag, "calm", state.clock, TrustRules.CALM_TICKS);
            state.calmBlockedUntil = gate(tag, "calmBlocked", state.clock, TrustRules.CALM_LOCKOUT);
            state.generation = number(tag, "generation");
            if (tag.contains("keeper")) { require(tag.hasUUID("keeper"), "keeper UUID"); state.keeper = tag.getUUID("keeper"); }
            if (tag.contains("calmSource")) { require(tag.hasUUID("calmSource"), "calm source UUID"); state.calmSource = tag.getUUID("calmSource"); }
            require((state.calmRemaining() > 0) == (state.calmSource != null), "calm source mismatch");
            require(tag.get("relationships") instanceof ListTag, "relationships missing");
            ListTag list = (ListTag) tag.get("relationships");
            require(list.size() <= TrustRules.MAX_CAPACITY && (list.isEmpty() || list.getElementType() == Tag.TAG_COMPOUND), "relationship bounds");
            for (Tag value : list) {
                CompoundTag entry = (CompoundTag) value;
                require(entry.getAllKeys().equals(Set.of("player", "trust", "feeds", "nextFeed"))
                        && entry.hasUUID("player") && entry.contains("trust", Tag.TAG_INT) && entry.contains("feeds", Tag.TAG_INT), "relationship fields");
                var view = new AnimalRelationshipView(entry.getInt("trust"), entry.getInt("feeds"),
                        gate(entry, "nextFeed", state.clock, TrustRules.FEED_INTERVAL));
                require(state.relationships.put(entry.getUUID("player"), view) == null, "duplicate player");
            }
            return new Loaded(state, null, "");
        } catch (IllegalArgumentException exception) {
            return new Loaded(new AnimalState(), tag.copy(), exception.getMessage());
        }
    }

    private static long number(CompoundTag tag, String key) {
        require(tag.contains(key, Tag.TAG_LONG) && tag.getLong(key) >= 0, "invalid " + key);
        return tag.getLong(key);
    }

    private static long gate(CompoundTag tag, String key, long clock, long max) {
        long value = number(tag, key);
        require(value <= clock + max, "invalid " + key + " horizon");
        return value;
    }

    private static void require(boolean condition, String problem) {
        if (!condition) throw new IllegalArgumentException(problem);
    }

    public static CompoundTag write(Loaded loaded) {
        return loaded.writable() ? write(loaded.state()) : loaded.preserved().copy();
    }

    public static CompoundTag write(AnimalState state) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", SCHEMA);
        tag.putLong("clock", state.clock);
        tag.putLong("hurt", state.hurtUntil);
        tag.putLong("heal", state.healUntil);
        tag.putLong("calm", state.calmUntil);
        tag.putLong("calmBlocked", state.calmBlockedUntil);
        tag.putLong("generation", state.generation);
        if (state.keeper != null) tag.putUUID("keeper", state.keeper);
        if (state.calmSource != null) tag.putUUID("calmSource", state.calmSource);
        ListTag entries = new ListTag();
        state.relationships.forEach((player, view) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", player);
            entry.putInt("trust", view.trust());
            entry.putInt("feeds", view.feeds());
            entry.putLong("nextFeed", view.nextFeed());
            entries.add(entry);
        });
        tag.put("relationships", entries);
        return tag;
    }
}
