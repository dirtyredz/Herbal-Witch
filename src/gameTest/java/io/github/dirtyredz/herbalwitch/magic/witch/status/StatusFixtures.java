package io.github.dirtyredz.herbalwitch.magic.witch.status;

/** Test-source access to setup durable state; never included in the release jar. */
public final class StatusFixtures {
    private StatusFixtures() { }
    public static void energy(WitchState state, int units) { state.setEnergyUnits(units); }
    public static void tea(WitchState state, int ticks) { state.setTeaCooldownTicks(ticks); }
    public static void restful(WitchState state, int ticks) { state.setRestfulTicks(ticks); }
    public static void debit(net.minecraft.server.level.ServerPlayer player, int units) {
        WitchCapability.of(player).filter(WitchCapability::writable).ifPresent(c -> {
            if (EnergyService.debit(c.state(), units) == EnergyService.Result.OK && units > 0) {
                c.disturb(); c.changed();
            }
        });
    }
    public static void practice(net.minecraft.server.level.ServerPlayer player, int units) {
        WitchCapability.of(player).filter(WitchCapability::writable).ifPresent(c -> {
            EnergyService.awardPractice(c.state(), io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules.server().energy(), units);
            c.changed();
        });
    }
}
