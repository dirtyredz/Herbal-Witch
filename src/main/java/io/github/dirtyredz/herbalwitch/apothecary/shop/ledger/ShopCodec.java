package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransaction;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrder;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Versioned, bounded, uncompressed shard codec (schema 1). Unknown schemas, unknown fields, duplicate
 * identities and out-of-bound collections are refused, never repaired: the store preserves the file
 * and keeps the shop unavailable rather than inventing an empty shop.
 */
public final class ShopCodec {
    public static final int SCHEMA = 1;
    /** Serialized shard bound (uncompressed). */
    public static final int MAX_BYTES = 512 * 1024;
    /** Hard proceeds ceiling; the configured capacity can only be lower. */
    public static final long MAX_PROCEEDS = 1_000_000L;
    public static final int MAX_ORDERS = 64;
    public static final int MAX_TRANSACTIONS = 4;
    private static final Set<String> FIELDS = Set.of("schema", "id", "owner", "ownerName", "name", "revision", "lifecycle", "binding",
            "generation", "links", "stock", "offers", "proceeds", "orders", "history", "transactions", "receipts", "publicBuying",
            "npcService", "bans", "created");

    private ShopCodec() {
    }

    public static CompoundTag toTag(ShopRecord r) {
        CompoundTag root = new CompoundTag();
        root.putInt("schema", SCHEMA);
        root.putUUID("id", r.id);
        root.putUUID("owner", r.owner);
        root.putString("ownerName", r.ownerName);
        root.putString("name", r.name);
        root.putLong("revision", r.revision);
        root.putString("lifecycle", r.lifecycle.name());
        if (r.binding != null) root.put("binding", r.binding.save());
        root.putLong("generation", r.generation);
        ListTag links = new ListTag();
        r.links.forEach(l -> links.add(l.save()));
        root.put("links", links);
        root.put("stock", r.stock.save());
        ListTag offers = new ListTag();
        r.offers.forEach(o -> offers.add(o.save()));
        root.put("offers", offers);
        root.putLong("proceeds", r.proceeds);
        ListTag orders = new ListTag();
        r.orders.forEach(o -> orders.add(o.save()));
        root.put("orders", orders);
        ListTag history = new ListTag();
        r.history.forEach(h -> history.add(h.save()));
        root.put("history", history);
        ListTag transactions = new ListTag();
        r.transactions.forEach(t -> transactions.add(t.save()));
        root.put("transactions", transactions);
        ListTag receipts = new ListTag();
        for (ShopRecord.Receipt receipt : r.receipts) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("transaction", receipt.transaction());
            entry.putUUID("player", receipt.player());
            entry.putLong("sequence", receipt.sequence());
            entry.putBoolean("discarded", receipt.discarded());
            receipts.add(entry);
        }
        root.put("receipts", receipts);
        root.putBoolean("publicBuying", r.publicBuying);
        root.putBoolean("npcService", r.npcService);
        ListTag bans = new ListTag();
        r.bans.forEach(b -> bans.add(NbtUtils.createUUID(b)));
        root.put("bans", bans);
        root.putLong("created", r.created);
        return root;
    }

    public static byte[] encode(ShopRecord record) throws IOException {
        CompoundTag root = toTag(record);
        try {
            validate(root);
            check(record);
        } catch (RuntimeException invalid) {
            throw new IOException("Invalid shop record", invalid);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NbtIo.write(root, new DataOutputStream(out));
        if (out.size() > MAX_BYTES) throw new IOException("Shop shard exceeds " + MAX_BYTES + " bytes");
        return out.toByteArray();
    }

    public static ShopRecord decode(byte[] bytes) throws IOException {
        if (bytes.length > MAX_BYTES) throw new IOException("Shop shard exceeds hard bound");
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        CompoundTag root = NbtIo.read(in, new NbtAccounter(MAX_BYTES * 8L));
        if (in.available() != 0) throw new IOException("Trailing shop bytes");
        if (!root.contains("schema", Tag.TAG_INT) || root.getInt("schema") != SCHEMA) throw new UnsupportedSchema(root.getInt("schema"));
        try {
            validate(root);
            return fromTag(root);
        } catch (RuntimeException malformed) {
            throw new IOException("Malformed shop record", malformed);
        }
    }

    /** A schema this version cannot read: preserved, never rewritten. */
    public static final class UnsupportedSchema extends IOException {
        public UnsupportedSchema(int schema) {
            super("Unsupported shop schema " + schema);
        }
    }

    static ShopRecord fromTag(CompoundTag root) {
        ShopRecord r = new ShopRecord(root.getUUID("id"), root.getUUID("owner"), root.getString("ownerName"), root.getString("name"), root.getLong("created"));
        r.revision = root.getLong("revision");
        r.lifecycle = ShopRecord.Lifecycle.valueOf(root.getString("lifecycle"));
        r.binding = root.contains("binding") ? ShopBinding.load(root.getCompound("binding")) : null;
        r.generation = root.getLong("generation");
        for (Tag tag : root.getList("links", Tag.TAG_COMPOUND)) r.links.add(ShopLink.load((CompoundTag) tag));
        r.stock = ShopStock.load(root.getList("stock", Tag.TAG_COMPOUND));
        for (Tag tag : root.getList("offers", Tag.TAG_COMPOUND)) r.offers.add(ShopOffer.load((CompoundTag) tag));
        r.proceeds = root.getLong("proceeds");
        for (Tag tag : root.getList("orders", Tag.TAG_COMPOUND)) r.orders.add(ShopOrder.load((CompoundTag) tag));
        for (Tag tag : root.getList("history", Tag.TAG_COMPOUND)) r.history.addLast(ShopHistoryEntry.load((CompoundTag) tag));
        for (Tag tag : root.getList("transactions", Tag.TAG_COMPOUND)) r.transactions.add(ShopTransaction.load((CompoundTag) tag));
        for (Tag tag : root.getList("receipts", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) tag;
            r.receipts.addLast(new ShopRecord.Receipt(entry.getUUID("transaction"), entry.getUUID("player"), entry.getLong("sequence"), entry.getBoolean("discarded")));
        }
        r.publicBuying = root.getBoolean("publicBuying");
        r.npcService = root.getBoolean("npcService");
        for (Tag tag : root.getList("bans", Tag.TAG_INT_ARRAY)) r.bans.add(NbtUtils.loadUUID(tag));
        check(r);
        return r;
    }

    /** Structural bounds before decoding the parts. */
    static void validate(CompoundTag root) {
        if (!FIELDS.containsAll(root.getAllKeys()) || !root.getAllKeys().containsAll(fieldsWithoutOptional())) throw new IllegalArgumentException("Shop fields");
        for (String id : Set.of("id", "owner")) if (!root.hasUUID(id)) throw new IllegalArgumentException("Missing shop identity");
        if (!ShopRecord.validName(root.getString("name")) || root.getString("ownerName").length() > 64) throw new IllegalArgumentException("Shop name");
        bounded(root, 0);
    }

    private static Set<String> fieldsWithoutOptional() {
        Set<String> required = new HashSet<>(FIELDS);
        required.remove("binding");
        return required;
    }

    private static void check(ShopRecord r) {
        if (r.revision < 1 || r.generation < 0 || r.proceeds < 0 || r.proceeds > MAX_PROCEEDS) throw new IllegalArgumentException("Shop counters");
        if (r.binding != null && r.binding.generation() > r.generation) throw new IllegalArgumentException("Binding newer than generation");
        if ((r.lifecycle == ShopRecord.Lifecycle.OPEN || r.lifecycle == ShopRecord.Lifecycle.CLOSED) && r.binding == null)
            throw new IllegalArgumentException("Bound lifecycle without binding");
        if (r.offers.size() > ShopRecord.MAX_OFFERS || r.bans.size() > ShopRecord.MAX_BANS || r.history.size() > ShopHistoryEntry.CAPACITY
                || r.receipts.size() > ShopRecord.MAX_RECEIPTS || r.orders.size() > MAX_ORDERS || r.transactions.size() > MAX_TRANSACTIONS)
            throw new IllegalArgumentException("Shop collection bounds");
        long counters = r.links.stream().filter(l -> l.kind() == ShopLink.Kind.COUNTER).count();
        if (counters > ShopRecord.MAX_EXTRA_COUNTERS || r.links.size() - counters > ShopRecord.MAX_FIXTURES) throw new IllegalArgumentException("Link bounds");
        unique(r.offers.stream().map(ShopOffer::id).toList(), "offer");
        unique(r.orders.stream().map(ShopOrder::id).toList(), "order");
        unique(r.transactions.stream().map(ShopTransaction::id).toList(), "transaction");
        unique(r.receipts.stream().map(ShopRecord.Receipt::transaction).toList(), "receipt");
        unique(r.links.stream().map(l -> l.dimension() + "@" + l.pos()).toList(), "link");
        unique(r.bans, "ban");
    }

    private static <T> void unique(java.util.List<T> values, String what) {
        if (new HashSet<>(values).size() != values.size()) throw new IllegalArgumentException("Duplicate " + what);
    }

    /** Depth 16, 2048 entries per collection, 4096-character strings, finite numbers. */
    private static void bounded(Tag tag, int depth) {
        if (depth > 16) throw new IllegalArgumentException("Shop NBT too deep");
        if (tag instanceof CompoundTag compound) {
            if (compound.size() > 512) throw new IllegalArgumentException("Too many fields");
            for (String key : compound.getAllKeys()) {
                if (key.length() > 256) throw new IllegalArgumentException("Key too long");
                bounded(compound.get(key), depth + 1);
            }
        } else if (tag instanceof CollectionTag<?> list) {
            if (list.size() > 2048) throw new IllegalArgumentException("Collection too long");
            for (Tag value : list) bounded(value, depth + 1);
        } else if (tag instanceof StringTag text && text.getAsString().length() > 4096) {
            throw new IllegalArgumentException("Text too long");
        } else if (tag instanceof NumericTag number && !Double.isFinite(number.getAsDouble())) {
            throw new IllegalArgumentException("Non-finite number");
        }
    }

    /** Identity of a shard file name. */
    public static String name(UUID shop) {
        return "herbalwitch_shop_" + shop;
    }
}
