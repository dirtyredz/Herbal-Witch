package io.github.dirtyredz.herbalwitch.companions.familiars.ability;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPaidStateChange;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarMenuAccess;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

public final class FamiliarReserve {
    private FamiliarReserve() {}
    public static int capacity(FamiliarRecord record) { return record.state().getBoolean("spiritWell") ? 3000 : 2000; }
    public static String charge(ServerPlayer owner, UUID bond) {
        var record = FamiliarStore.get(owner.server).record(bond).orElse(null);
        if (!FamiliarMenuAccess.detail(owner, record)) return "nearby_only";
        if (record.state().getInt("reserve") > capacity(record) - 500) return "reserve_full";
        return FamiliarPaidStateChange.commit(owner, bond, 500, AltarOffering.none(), r -> r.change(s -> s.putInt("reserve", s.getInt("reserve") + 500)))
                ? "reserve_charged" : "unavailable";
    }
}
