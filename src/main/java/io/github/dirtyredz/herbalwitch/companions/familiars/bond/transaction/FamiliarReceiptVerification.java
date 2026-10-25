package io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import net.minecraft.nbt.*;
import java.io.*;
import java.nio.file.*;
import io.github.dirtyredz.herbalwitch.platform.persistence.PlayerFileCheckpoint;

/** Startup check against an older/missing player save. Never grants, refunds or repairs inventory. */
public final class FamiliarReceiptVerification {
    private FamiliarReceiptVerification() {}
    public static boolean matches(CompoundTag expected, CompoundTag saved) {
        if (expected.isEmpty()) return true; // Pre-equipment records have no completed transfer watermark.
        if (expected.getInt("schema") != 1 || saved.getInt("schema") != 1
                || expected.getLong("sequence") < 1 || saved.getLong("sequence") < expected.getLong("sequence")) return false;
        if (saved.getLong("sequence") > expected.getLong("sequence")) return true;
        return !saved.getBoolean("pending") && expected.hasUUID("bond") && expected.hasUUID("transaction")
                && saved.hasUUID("bond") && saved.hasUUID("transaction")
                && expected.getUUID("bond").equals(saved.getUUID("bond"))
                && expected.getUUID("transaction").equals(saved.getUUID("transaction"));
    }
    public static boolean verify(FamiliarRecord record, Path worldData) {
        var expected = record.state().getCompound("settledReceipt");
        if (expected.isEmpty()) return true;
        try {
            var saved = PlayerFileCheckpoint.saved(worldData.getParent(), record.owner());
            return matches(expected, saved.getCompound("ForgeData").getCompound(FamiliarPlayerReceipt.KEY));
        } catch (IOException | RuntimeException unreadable) { return false; }
    }
}
