package io.github.dirtyredz.herbalwitch.magic.witch.status;

import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WitchSpendTest {
    private static final EnergyBalance BALANCE = EnergyBalance.DEFAULTS;
    private static final Runnable NOTHING = () -> { };

    private static WitchState witch() {
        WitchState state = new WitchState();
        EnergyService.initiate(state, BALANCE, UUID.nameUUIDFromBytes(new byte[]{1}));
        state.setTeaCooldownTicks(33);
        state.setRestfulTicks(44);
        state.setLastSleepRewardDay(5);
        return state;
    }

    private static boolean spend(WitchState state, int units, WitchStatusService.Change change) {
        return WitchSpend.run(state, true, BALANCE, units, change, NOTHING, NOTHING, NOTHING);
    }

    @Test
    void passiveMaintenancePaysWithoutPractice() {
        var state=witch();
        assertTrue(spend(state,250,new WitchStatusService.Change(){
            public boolean apply(){return true;}
            public boolean awardsPractice(){return false;}
        }));
        assertEquals(9750,state.energyUnits());
        assertEquals(0,state.practiceSpent());
    }

    @Test
    void refusalNeverInvokesAnyParticipantOrNotification() {
        Runnable unexpected = () -> fail("callback on refused spend");
        WitchStatusService.Change change = new WitchStatusService.Change() {
            public boolean prepare() { unexpected.run(); return true; }
            public boolean apply() { unexpected.run(); return true; }
            public void rollback() { unexpected.run(); }
        };
        for (int units : new int[]{-1, 10_001, Integer.MAX_VALUE}) {
            WitchState state = witch(), before = state.copy();
            assertFalse(WitchSpend.run(state, true, BALANCE, units, change, unexpected, unexpected, unexpected));
            assertTrue(before.sameAs(state));
        }
        assertFalse(WitchSpend.run(witch(), false, BALANCE, 1, change, unexpected, unexpected, unexpected));
        assertFalse(WitchSpend.run(new WitchState(), true, BALANCE, 0, change, unexpected, unexpected, unexpected));
    }

    @Test
    void preparationPrecedesDebitAndSuccessfulPracticeAndSyncHappenOnce() {
        WitchState state = witch();
        List<String> order = new ArrayList<>();
        assertTrue(WitchSpend.run(state, true, BALANCE, 250, new WitchStatusService.Change() {
            public boolean prepare() {
                assertEquals(10_000, state.energyUnits());
                order.add("prepare");
                return true;
            }
            public boolean apply() {
                assertEquals(9_750, state.energyUnits());
                assertEquals(0, state.practiceSpent());
                order.add("apply");
                return true;
            }
            public void rollback() { fail("successful spend rolled back"); }
        }, () -> order.add("debit"), () -> fail("rollback notification"), () -> {
            assertEquals(250, state.practiceSpent());
            order.add("sync");
        }));
        assertEquals(List.of("prepare", "debit", "apply", "sync"), order);
    }

    @Test
    void zeroCostWitchOperationSucceedsWithoutDisturbingOrPracticing() {
        WitchState state = witch(), before = state.copy();
        List<String> events = new ArrayList<>();
        assertTrue(WitchSpend.run(state, true, BALANCE, 0, () -> {
            events.add("apply"); return true;
        }, () -> fail("zero spend disturbed rest"), () -> fail("rollback"), () -> events.add("sync")));
        assertTrue(before.sameAs(state));
        assertEquals(List.of("apply", "sync"), events);
    }

    @Test
    void refusedPreparationRestoresEvenFieldsItChangedAndRunsRollback() {
        WitchState state = witch(), before = state.copy();
        int[] rollback = {0};
        assertFalse(spend(state, 250, new WitchStatusService.Change() {
            public boolean prepare() { state.setRestfulTicks(1); return false; }
            public boolean apply() { fail("applied after preparation refused"); return true; }
            public void rollback() { rollback[0]++; state.setTeaCooldownTicks(1); }
        }));
        assertTrue(before.sameAs(state));
        assertEquals(1, rollback[0]);
    }

    @Test
    void falseChangeRollsBackWithoutPracticeOrSuccessSync() {
        WitchState state = witch(), before = state.copy();
        List<String> events = new ArrayList<>();
        assertFalse(WitchSpend.run(state, true, BALANCE, 250, new WitchStatusService.Change() {
            public boolean apply() { state.setLastSleepRewardDay(99); return false; }
            public void rollback() { events.add("participant"); }
        }, () -> events.add("disturbed"), () -> events.add("restored"), () -> fail("failure synchronized as success")));
        assertTrue(before.sameAs(state));
        assertEquals(List.of("disturbed", "participant", "restored"), events);
    }

    @Test
    void thrownChangeRestoresDurableStateAndPropagatesTheOriginalException() {
        WitchState state = witch(), before = state.copy();
        RuntimeException failure = new IllegalStateException("charge failed after debit");
        int[] rollbacks = {0};
        assertSame(failure, assertThrows(RuntimeException.class, () -> spend(state, 250, new WitchStatusService.Change() {
            public boolean apply() { state.setPracticeSpent(999); throw failure; }
            public void rollback() { rollbacks[0]++; }
        })));
        assertTrue(before.sameAs(state), "failed spend leaked durable state");
        assertEquals(1, rollbacks[0]);
    }

    @Test
    void thrownPreparationAlsoRollsBack() {
        WitchState state = witch(), before = state.copy();
        RuntimeException failure = new IllegalArgumentException("inventory preparation failed");
        int[] rollbacks = {0};
        assertSame(failure, assertThrows(RuntimeException.class, () -> spend(state, 250, new WitchStatusService.Change() {
            public boolean prepare() { state.setEnergyUnits(1); throw failure; }
            public boolean apply() { fail("applied after exception"); return true; }
            public void rollback() { rollbacks[0]++; }
        })));
        assertTrue(before.sameAs(state));
        assertEquals(1, rollbacks[0]);
    }

    @Test
    void rollbackFailureCannotReplaceTheOriginalOrPreventDurableRestoration() {
        WitchState state = witch(), before = state.copy();
        RuntimeException failure = new IllegalStateException("original"), rollback = new IllegalStateException("rollback");
        assertSame(failure, assertThrows(RuntimeException.class, () -> spend(state, 250, new WitchStatusService.Change() {
            public boolean apply() { throw failure; }
            public void rollback() { throw rollback; }
        })));
        assertArrayEquals(new Throwable[]{rollback}, failure.getSuppressed());
        assertTrue(before.sameAs(state));
    }

    @Test
    void rollbackFailureOnRefusalStillRestoresDurableState() {
        WitchState state = witch(), before = state.copy();
        RuntimeException failure = new IllegalStateException("rollback");
        assertSame(failure, assertThrows(RuntimeException.class, () -> spend(state, 250, new WitchStatusService.Change() {
            public boolean apply() { return false; }
            public void rollback() { throw failure; }
        })));
        assertTrue(before.sameAs(state));
    }

    @Test
    void exceptionDuringSuccessfulNotificationRestoresPracticeToo() {
        WitchState state = witch(), before = state.copy();
        RuntimeException failure = new IllegalStateException("notification");
        assertSame(failure, assertThrows(RuntimeException.class, () -> WitchSpend.run(state, true, BALANCE, 250,
                () -> true, NOTHING, NOTHING, () -> { throw failure; })));
        assertTrue(before.sameAs(state));
    }
}
