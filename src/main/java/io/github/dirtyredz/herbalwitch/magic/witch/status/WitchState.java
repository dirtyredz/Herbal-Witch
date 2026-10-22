package io.github.dirtyredz.herbalwitch.magic.witch.status;

import java.util.Optional;
import java.util.UUID;

/**
 * One player's durable witch state, schema 1. Plain Java (unit-tested without Minecraft); mutated
 * only by the server's witch services, never from a packet. Capacity is not stored: it is derived
 * from {@link #practiceSpent()} and the server's energy balance. Discoveries stay in the grimoire's
 * own capability; familiars and wards will keep their own records.
 *
 * <ul>
 *   <li>{@code initiated}: authoritative witch status (fresh players are not witches);</li>
 *   <li>{@code initiationId}: the completed conversion's identity, absent before it;</li>
 *   <li>{@code energyUnits}: energy in hundredths, kept within the derived capacity;</li>
 *   <li>{@code practiceSpent}: successful qualifying expenditure in hundredths, bounded at the final threshold;</li>
 *   <li>{@code teaCooldownTicks}: remaining energy-tea cooldown (online ticks);</li>
 *   <li>{@code restfulTicks}: remaining restful-infusion preparation, 0 when absent;</li>
 *   <li>{@code lastSleepRewardDay}: the last rewarded Overworld day, {@link #NEVER} before any.</li>
 * </ul>
 */
public final class WitchState {
    public static final long NEVER = -1L;

    private boolean initiated;
    private UUID initiationId;
    private int energyUnits;
    private int practiceSpent;
    private int teaCooldownTicks;
    private int restfulTicks;
    private long lastSleepRewardDay = NEVER;

    public boolean initiated() {
        return initiated;
    }

    public Optional<UUID> initiationId() {
        return Optional.ofNullable(initiationId);
    }

    public int energyUnits() {
        return energyUnits;
    }

    public int practiceSpent() {
        return practiceSpent;
    }

    public int teaCooldownTicks() {
        return teaCooldownTicks;
    }

    public int restfulTicks() {
        return restfulTicks;
    }

    public long lastSleepRewardDay() {
        return lastSleepRewardDay;
    }

    /** Conversion: the player becomes a witch with this identity (services only). */
    void initiate(UUID id) {
        initiated = true;
        initiationId = id;
    }

    void setEnergyUnits(int units) {
        energyUnits = Math.max(0, units);
    }

    void setPracticeSpent(int units) {
        practiceSpent = Math.max(0, units);
    }

    void setTeaCooldownTicks(int ticks) {
        teaCooldownTicks = Math.max(0, ticks);
    }

    void setRestfulTicks(int ticks) {
        restfulTicks = Math.max(0, ticks);
    }

    void setLastSleepRewardDay(long day) {
        lastSleepRewardDay = day;
    }

    /** An independent copy (player cloning, operation snapshots). */
    public WitchState copy() {
        WitchState c = new WitchState();
        c.restoreFrom(this);
        return c;
    }

    /** Takes every field from a snapshot (a failed operation's rollback). */
    void restoreFrom(WitchState other) {
        initiated = other.initiated;
        initiationId = other.initiationId;
        energyUnits = other.energyUnits;
        practiceSpent = other.practiceSpent;
        teaCooldownTicks = other.teaCooldownTicks;
        restfulTicks = other.restfulTicks;
        lastSleepRewardDay = other.lastSleepRewardDay;
    }

    /** Field-wise equality, for tests and change detection. */
    public boolean sameAs(WitchState o) {
        return initiated == o.initiated && java.util.Objects.equals(initiationId, o.initiationId) && energyUnits == o.energyUnits
                && practiceSpent == o.practiceSpent && teaCooldownTicks == o.teaCooldownTicks && restfulTicks == o.restfulTicks
                && lastSleepRewardDay == o.lastSleepRewardDay;
    }

    @Override
    public String toString() {
        return "WitchState{initiated=" + initiated + ", energy=" + energyUnits + ", practice=" + practiceSpent + ", tea=" + teaCooldownTicks
                + ", restful=" + restfulTicks + ", lastSleepDay=" + lastSleepRewardDay + "}";
    }
}
