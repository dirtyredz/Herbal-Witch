package io.github.dirtyredz.herbalwitch.magic.witch.network;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * The server's active witch rules, sent to each client on login and after every accepted change,
 * so the grimoire and the altar describe the mechanics this server really uses (costs, durations,
 * thresholds, recovery). Public numbers only; no player data.
 */
public record WitchRulesUpdate(WitchRules rules) {
    private static final int MAX_THRESHOLDS = 32;

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(rules.foundingEnabled());
        buf.writeBoolean(rules.initiationEnabled());
        for (int v : new int[] {rules.foundingTicks(), rules.initiationTicks(), rules.attunementTicks(), rules.inviteExpiry(), rules.inviteCooldown()}) {
            buf.writeVarInt(v);
        }
        EnergyBalance e = rules.energy();
        buf.writeVarInt(e.thresholds().size());
        e.thresholds().forEach(buf::writeVarInt);
        for (int v : numbers(e)) {
            buf.writeVarInt(v);
        }
    }

    private static int[] numbers(EnergyBalance e) {
        return new int[] {e.initialCapacity(), e.capacityPerThreshold(), e.initiationCost(), e.attunementCost(), e.restInterval(),
                e.restAmount(), e.meditationSettle(), e.meditationInterval(), e.meditationAmount(), e.sleepAmount(), e.teaAmount(),
                e.teaCooldown(), e.restfulBonus(), e.restfulDuration(), e.natureBonus(), e.moonBonus(), e.bonusCap()};
    }

    public static WitchRulesUpdate decode(FriendlyByteBuf buf) {
        boolean founding = buf.readBoolean();
        boolean initiation = buf.readBoolean();
        int[] t = new int[5];
        for (int i = 0; i < t.length; i++) {
            t[i] = buf.readVarInt();
        }
        int n = PayloadBounds.readCount(buf, MAX_THRESHOLDS);
        List<Integer> thresholds = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            thresholds.add(buf.readVarInt());
        }
        int[] v = new int[17];
        for (int i = 0; i < v.length; i++) {
            v[i] = buf.readVarInt();
        }
        EnergyBalance energy = new EnergyBalance(v[0], thresholds, v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11],
                v[12], v[13], v[14], v[15], v[16]);
        WitchRules rules = new WitchRules(founding, initiation, t[0], t[1], t[2], t[3], t[4], energy);
        if (!rules.problems().isEmpty()) {
            throw new DecoderException("invalid witch rules: " + rules.problems());
        }
        return new WitchRulesUpdate(rules);
    }
}
