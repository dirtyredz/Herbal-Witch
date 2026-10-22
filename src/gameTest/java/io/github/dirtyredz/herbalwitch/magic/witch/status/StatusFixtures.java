package io.github.dirtyredz.herbalwitch.magic.witch.status;

/** Test-source access to setup durable state; never included in the release jar. */
public final class StatusFixtures {
    private StatusFixtures() { }
    public static void energy(WitchState state, int units) { state.setEnergyUnits(units); }
    public static void tea(WitchState state, int ticks) { state.setTeaCooldownTicks(ticks); }
    public static void restful(WitchState state, int ticks) { state.setRestfulTicks(ticks); }
}
