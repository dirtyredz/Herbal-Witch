package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import net.minecraft.world.entity.player.Player;

/** Test-source-only access to package-level transfer steps for restart and recovery fixtures. Never shipped. */
public final class ShopTestAccess {
    private ShopTestAccess() {
    }

    public static ShopRecord prepare(ShopRecord record, ShopTransaction transaction) {
        return ShopTransfer.prepare(record, transaction);
    }

    public static void receipt(Player player, ShopPlayerReceipt.Marker marker) {
        ShopPlayerReceipt.set(player, marker);
    }
}
