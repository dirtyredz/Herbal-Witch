package io.github.dirtyredz.herbalwitch.magic.ritual.network;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * What one player's open altar screen shows, privately and only when it changed (at most every few
 * ticks): public founding eligibility (no world records), the altar's current operation and its
 * progress, this player's own incoming and outgoing invitations, the nearby non-witches a witch could
 * invite (at most {@link #MAX_NEARBY}), have/need counts of each offering from this player's own
 * main inventory and offhand, and the last result or interruption. Never another player's energy.
 */
public record AltarUpdate(int containerId, long session, boolean foundingOpen, Optional<Activity> activity, Optional<Invitation> incoming,
                          Optional<Invitation> outgoing, List<Nearby> nearby, Map<AltarActionSpec, List<Count>> counts,
                          Optional<String> notice) {
    public static final int MAX_NEARBY = 16;
    private static final int MAX_NAME = 64;
    private static final int MAX_REQUIREMENTS = 8;

    /** The operation running at the altar. */
    public record Activity(AltarActionSpec action, int elapsed, int duration, boolean mine, String with) {
    }

    /** An invitation: its id, the other party's name (empty for a designation), time left, whether it was made here. */
    public record Invitation(long id, String name, boolean administrative, int ticksLeft, boolean here) {
    }

    public record Nearby(UUID id, String name) {
    }

    public record Count(int have, int need) {
    }

    public AltarUpdate {
        nearby = List.copyOf(nearby);
        counts = Map.copyOf(counts);
        if (nearby.size() > MAX_NEARBY) {
            throw new IllegalArgumentException(nearby.size() + " nearby players exceed " + MAX_NEARBY);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeLong(session);
        buf.writeBoolean(foundingOpen);
        buf.writeBoolean(activity.isPresent());
        activity.ifPresent(a -> {
            PayloadBounds.writeEnum(buf, a.action());
            buf.writeVarInt(a.elapsed());
            buf.writeVarInt(a.duration());
            buf.writeBoolean(a.mine());
            buf.writeUtf(a.with(), MAX_NAME);
        });
        for (Optional<Invitation> invitation : List.of(incoming, outgoing)) {
            buf.writeBoolean(invitation.isPresent());
            invitation.ifPresent(i -> {
                buf.writeLong(i.id());
                buf.writeUtf(i.name(), MAX_NAME);
                buf.writeBoolean(i.administrative());
                buf.writeVarInt(i.ticksLeft());
                buf.writeBoolean(i.here());
            });
        }
        buf.writeVarInt(nearby.size());
        nearby.forEach(n -> {
            buf.writeUUID(n.id());
            buf.writeUtf(n.name(), MAX_NAME);
        });
        buf.writeVarInt(counts.size());
        counts.forEach((action, list) -> {
            PayloadBounds.writeEnum(buf, action);
            buf.writeVarInt(list.size());
            list.forEach(c -> {
                buf.writeVarInt(c.have());
                buf.writeVarInt(c.need());
            });
        });
        buf.writeBoolean(notice.isPresent());
        notice.ifPresent(n -> buf.writeUtf(n, PayloadBounds.MAX_TEXT));
    }

    public static AltarUpdate decode(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        long session = buf.readLong();
        boolean foundingOpen = buf.readBoolean();
        Optional<Activity> activity = buf.readBoolean() ? Optional.of(new Activity(PayloadBounds.readEnum(buf, AltarActionSpec.class),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), PayloadBounds.readString(buf, MAX_NAME))) : Optional.empty();
        Optional<Invitation> incoming = readInvitation(buf);
        Optional<Invitation> outgoing = readInvitation(buf);
        int n = PayloadBounds.readCount(buf, MAX_NEARBY);
        List<Nearby> nearby = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nearby.add(new Nearby(buf.readUUID(), PayloadBounds.readString(buf, MAX_NAME)));
        }
        int actions = PayloadBounds.readCount(buf, AltarActionSpec.values().length);
        Map<AltarActionSpec, List<Count>> counts = new EnumMap<>(AltarActionSpec.class);
        for (int a = 0; a < actions; a++) {
            AltarActionSpec action = PayloadBounds.readEnum(buf, AltarActionSpec.class);
            int size = PayloadBounds.readCount(buf, MAX_REQUIREMENTS);
            List<Count> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(new Count(buf.readVarInt(), buf.readVarInt()));
            }
            counts.put(action, list);
        }
        Optional<String> notice = buf.readBoolean() ? Optional.of(PayloadBounds.readString(buf, PayloadBounds.MAX_TEXT)) : Optional.empty();
        return new AltarUpdate(containerId, session, foundingOpen, activity, incoming, outgoing, nearby, counts, notice);
    }

    private static Optional<Invitation> readInvitation(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return Optional.empty();
        }
        return Optional.of(new Invitation(buf.readLong(), PayloadBounds.readString(buf, MAX_NAME), buf.readBoolean(), buf.readVarInt(),
                buf.readBoolean()));
    }
}
