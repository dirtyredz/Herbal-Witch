package io.github.dirtyredz.herbalwitch.companions.familiars.network;

import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.network.FriendlyByteBuf;
import java.util.*;

/** Private, short-lived positions and opaque action tokens, never entity inventories or owner identities. */
public record FamiliarObservations(UUID bond, String dimension, List<Mark> marks) {
    public record Mark(UUID token, long position, String label, String kind) {}
    public FamiliarObservations {
        marks = List.copyOf(marks);
        if (marks.size() > 8 || dimension.length() > 96 || marks.stream().anyMatch(m -> m.label().length() > 64 || m.kind().length() > 16)) throw new IllegalArgumentException("Oversized familiar observations");
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(bond); buffer.writeUtf(dimension, 96); buffer.writeVarInt(marks.size());
        for (var mark : marks) { buffer.writeUUID(mark.token()); buffer.writeLong(mark.position()); buffer.writeUtf(mark.label(), 64); buffer.writeUtf(mark.kind(), 16); }
    }
    public static FamiliarObservations decode(FriendlyByteBuf buffer) {
        PayloadBounds.requireAtMost(buffer, 4096); UUID bond = buffer.readUUID(); String dimension = buffer.readUtf(96);
        int count = PayloadBounds.readCount(buffer, 8); var marks = new ArrayList<Mark>();
        for (int i = 0; i < count; i++) marks.add(new Mark(buffer.readUUID(), buffer.readLong(), buffer.readUtf(64), buffer.readUtf(16)));
        return new FamiliarObservations(bond, dimension, marks);
    }
}
