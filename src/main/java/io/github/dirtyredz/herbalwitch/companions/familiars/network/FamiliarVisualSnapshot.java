package io.github.dirtyredz.herbalwitch.companions.familiars.network;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarEquipmentCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;

/** Tracker-visible appearance only: no owner, bond UUID, inventory NBT, cargo, recovery data or receipts. */
public record FamiliarVisualSnapshot(int entity, UUID creature, long generation, String gear, String charm, String command, boolean resting) {
    public static final int MAX_BYTES = 192;
    public FamiliarVisualSnapshot {
        if (entity < 0 || generation < 1 || !gear.isEmpty() && FamiliarEquipmentCatalog.find(gear).isEmpty()
                || !charm.isEmpty() && CharmCatalog.ALL.stream().noneMatch(c -> c.id().equals(charm))) throw new IllegalArgumentException("Invalid familiar appearance");
        FamiliarCommand.parse(command);
    }
    public void encode(FriendlyByteBuf buffer) {
        int start = buffer.writerIndex(); buffer.writeVarInt(entity); buffer.writeUUID(creature); buffer.writeLong(generation);
        buffer.writeUtf(gear, 24); buffer.writeUtf(charm, 24); buffer.writeUtf(command, 16); buffer.writeBoolean(resting);
        if (buffer.writerIndex() - start > MAX_BYTES) throw new IllegalArgumentException("Familiar appearance too large");
    }
    public static FamiliarVisualSnapshot decode(FriendlyByteBuf buffer) {
        if (buffer.readableBytes() > MAX_BYTES) throw new IllegalArgumentException("Familiar appearance too large");
        var result = new FamiliarVisualSnapshot(buffer.readVarInt(), buffer.readUUID(), buffer.readLong(), buffer.readUtf(24), buffer.readUtf(24), buffer.readUtf(16), buffer.readBoolean());
        if (buffer.isReadable()) throw new IllegalArgumentException("Trailing familiar appearance bytes"); return result;
    }
}
