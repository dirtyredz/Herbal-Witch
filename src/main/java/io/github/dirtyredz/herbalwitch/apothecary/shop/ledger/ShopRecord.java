package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction.ShopTransaction;
import io.github.dirtyredz.herbalwitch.apothecary.shop.order.ShopOrder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The canonical state of one shop, held in its own shard. Services mutate a {@link #copy()} and hand it
 * to {@link ShopStore#update}, which validates, bumps nothing itself and swaps it in only when the
 * revision advances and the encoded shard fits its bound. The owner and id never change.
 */
public final class ShopRecord {
    public enum Lifecycle { CLOSED, OPEN, DETACHED, QUARANTINED, RETIRED }

    /** Durable evidence that a transaction finished (or was retired) and must never run again. */
    public record Receipt(UUID transaction, UUID player, long sequence, boolean discarded) {
    }

    public static final int MAX_OFFERS = 12;
    public static final int MAX_BANS = 32;
    public static final int MAX_RECEIPTS = 64;
    public static final int MAX_EXTRA_COUNTERS = 8;
    public static final int MAX_FIXTURES = 16;
    public static final int MAX_NAME_CODE_POINTS = 32;
    public static final int MAX_NAME_BYTES = 128;

    public final UUID id;
    public final UUID owner;
    public String ownerName;
    public String name;
    public long revision;
    public Lifecycle lifecycle;
    public ShopBinding binding;
    public long generation;
    public List<ShopLink> links = new ArrayList<>();
    public ShopStock stock = new ShopStock();
    public List<ShopOffer> offers = new ArrayList<>();
    public long proceeds;
    public List<ShopOrder> orders = new ArrayList<>();
    public Deque<ShopHistoryEntry> history = new ArrayDeque<>();
    public List<ShopTransaction> transactions = new ArrayList<>();
    public Deque<Receipt> receipts = new ArrayDeque<>();
    public boolean publicBuying = true;
    public boolean npcService;
    public List<UUID> bans = new ArrayList<>();
    public long created;

    public ShopRecord(UUID id, UUID owner, String ownerName, String name, long created) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.name = name;
        this.created = created;
        this.revision = 1;
        this.lifecycle = Lifecycle.CLOSED;
    }

    public ShopRecord copy() {
        ShopRecord out = new ShopRecord(id, owner, ownerName, name, created);
        out.revision = revision;
        out.lifecycle = lifecycle;
        out.binding = binding;
        out.generation = generation;
        out.links = new ArrayList<>(links);
        out.stock = stock.copy();
        out.offers = new ArrayList<>(offers);
        out.proceeds = proceeds;
        out.orders = new ArrayList<>(orders);
        out.history = new ArrayDeque<>(history);
        out.transactions = new ArrayList<>(transactions);
        out.receipts = new ArrayDeque<>(receipts);
        out.publicBuying = publicBuying;
        out.npcService = npcService;
        out.bans = new ArrayList<>(bans);
        return out;
    }

    /** A copy with the next revision; every service mutation starts here. */
    public ShopRecord next() {
        ShopRecord out = copy();
        out.revision = Math.addExact(revision, 1);
        return out;
    }

    /** Unresolved transfers block every further economic operation of this shop. */
    public boolean blocked() {
        return !transactions.isEmpty() || lifecycle == Lifecycle.QUARANTINED;
    }

    public boolean retired() {
        return lifecycle == Lifecycle.RETIRED;
    }

    public Optional<ShopOffer> offer(UUID offer) {
        return offers.stream().filter(o -> o.id().equals(offer)).findFirst();
    }

    public Optional<ShopOrder> order(UUID order) {
        return orders.stream().filter(o -> o.id().equals(order)).findFirst();
    }

    public Optional<ShopTransaction> transaction(UUID transaction) {
        return transactions.stream().filter(t -> t.id().equals(transaction)).findFirst();
    }

    public boolean settled(UUID transaction) {
        return receipts.stream().anyMatch(r -> r.transaction().equals(transaction));
    }

    public void record(ShopHistoryEntry entry) {
        history.addLast(entry);
        while (history.size() > ShopHistoryEntry.CAPACITY) history.removeFirst();
    }

    public void receipt(Receipt receipt) {
        receipts.addLast(receipt);
        while (receipts.size() > MAX_RECEIPTS) receipts.removeFirst();
    }

    public Optional<ShopLink> link(String dimension, long pos) {
        return links.stream().filter(l -> l.at(dimension, pos)).findFirst();
    }

    /** Whether a shop name is bounded plain text: 1..32 code points, at most 128 UTF-8 bytes, no control or formatting characters. */
    public static boolean validName(String name) {
        if (name == null) return false;
        String trimmed = name.strip();
        if (trimmed.isEmpty() || !trimmed.equals(name)) return false;
        int points = name.codePointCount(0, name.length());
        if (points > MAX_NAME_CODE_POINTS || name.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_NAME_BYTES) return false;
        return name.codePoints().noneMatch(c -> Character.isISOControl(c) || c == '§' || Character.getType(c) == Character.FORMAT);
    }
}
