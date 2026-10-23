package io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Schema-1 demand record codec; unknown schemas and out-of-bound maps are refused, never repaired. */
public final class NpcDemandCodec {
    public static final int MAX_BYTES = 512 * 1024;
    private static final Set<String> FIELDS = Set.of("schema", "elapsed", "windowStart", "world", "owners", "shops", "villagers", "debits");

    private NpcDemandCodec() {
    }

    public static byte[] encode(NpcDemand d) throws IOException {
        CompoundTag root = new CompoundTag();
        root.putInt("schema", 1);
        root.putLong("elapsed", d.elapsed);
        root.putLong("windowStart", d.windowStart);
        root.putInt("world", d.world);
        root.put("owners", counts(d.owners));
        root.put("shops", counts(d.shops));
        root.put("villagers", counts(d.villagers));
        ListTag debits = new ListTag();
        d.debits.forEach(id -> debits.add(NbtUtils.createUUID(id)));
        root.put("debits", debits);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NbtIo.write(root, new DataOutputStream(out));
        if (out.size() > MAX_BYTES) throw new IOException("Demand record exceeds bound");
        return out.toByteArray();
    }

    private static ListTag counts(Map<UUID, Integer> map) {
        ListTag list = new ListTag();
        map.forEach((id, count) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", id);
            entry.putInt("count", count);
            list.add(entry);
        });
        return list;
    }

    public static NpcDemand decode(byte[] bytes) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        CompoundTag root = NbtIo.read(in, new NbtAccounter(MAX_BYTES * 8L));
        try {
            if (!root.getAllKeys().equals(FIELDS) || root.getInt("schema") != 1) throw new IllegalArgumentException("Unsupported demand schema");
            NpcDemand d = new NpcDemand();
            d.elapsed = root.getLong("elapsed");
            d.windowStart = root.getLong("windowStart");
            d.world = root.getInt("world");
            if (d.elapsed < 0 || d.windowStart < 0 || d.windowStart > d.elapsed || d.world < 0) throw new IllegalArgumentException("Demand clock");
            read(root.getList("owners", Tag.TAG_COMPOUND), d.owners, ShopRules.MAX_DEMAND_IDENTITIES);
            read(root.getList("shops", Tag.TAG_COMPOUND), d.shops, ShopRules.MAX_DEMAND_IDENTITIES);
            read(root.getList("villagers", Tag.TAG_COMPOUND), d.villagers, ShopRules.MAX_DEMAND_VILLAGERS);
            ListTag debits = root.getList("debits", Tag.TAG_INT_ARRAY);
            if (debits.size() > 1024) throw new IllegalArgumentException("Too many debits");
            for (Tag tag : debits) if (!d.debits.add(NbtUtils.loadUUID(tag))) throw new IllegalArgumentException("Duplicate debit");
            return d;
        } catch (RuntimeException malformed) {
            throw new IOException("Malformed demand record", malformed);
        }
    }

    private static void read(ListTag list, Map<UUID, Integer> into, int max) {
        if (list.size() > max) throw new IllegalArgumentException("Demand map too large");
        for (Tag tag : list) {
            CompoundTag entry = (CompoundTag) tag;
            int count = entry.getInt("count");
            if (count < 1 || into.put(entry.getUUID("id"), count) != null) throw new IllegalArgumentException("Invalid demand entry");
        }
    }
}
