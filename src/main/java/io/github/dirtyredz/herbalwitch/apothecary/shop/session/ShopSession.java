package io.github.dirtyredz.herbalwitch.apothecary.shop.session;

import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;

import java.util.UUID;

/**
 * One open commerce menu: who, which menu and server nonce, which shop (null for an unbound register),
 * where it was opened from, the binding generation and rules revision it was opened under, and request
 * sequencing. The session accepts only its next sequence number; a retransmission of the last one gets
 * the previous result again without another mutation, and anything else is stale. Closing the menu
 * ends the session. Pure apart from its identity values.
 */
public final class ShopSession {
    public enum Origin { REGISTER, COUNTER, DISPLAY, BOARD }

    public enum View { BROWSE, MANAGE, ORDERS, CREATE }

    public enum Arrival { NEW, REPLAY, STALE }

    private final UUID player;
    private final int container;
    private final UUID nonce;
    private final UUID shop;
    private final Origin origin;
    private final String dimension;
    private final long pos;
    private final long generation;
    private final long rulesRevision;
    private final boolean blocked;
    private final View view;
    private int next = 1;
    private ShopResult last;

    public ShopSession(UUID player, int container, UUID nonce, UUID shop, Origin origin, View view, String dimension, long pos,
                       long generation, long rulesRevision, boolean blocked) {
        this.player = player;
        this.container = container;
        this.nonce = nonce;
        this.shop = shop;
        this.origin = origin;
        this.view = view;
        this.dimension = dimension;
        this.pos = pos;
        this.generation = generation;
        this.rulesRevision = rulesRevision;
        this.blocked = blocked;
    }

    public Arrival arrive(int sequence) {
        if (sequence == next) return Arrival.NEW;
        if (sequence == next - 1 && last != null) return Arrival.REPLAY;
        return Arrival.STALE;
    }

    /** Records the result of the accepted request and advances to the next sequence. */
    public void answered(ShopResult result) {
        last = result;
        next++;
    }

    public ShopResult last() {
        return last;
    }

    public int expected() {
        return next;
    }

    public boolean matches(int container, UUID nonce) {
        return this.container == container && this.nonce.equals(nonce);
    }

    public UUID player() {
        return player;
    }

    public int container() {
        return container;
    }

    public UUID nonce() {
        return nonce;
    }

    public UUID shop() {
        return shop;
    }

    public Origin origin() {
        return origin;
    }

    public View view() {
        return view;
    }

    public String dimension() {
        return dimension;
    }

    public long pos() {
        return pos;
    }

    public long generation() {
        return generation;
    }

    public long rulesRevision() {
        return rulesRevision;
    }

    public boolean blocked() {
        return blocked;
    }
}
