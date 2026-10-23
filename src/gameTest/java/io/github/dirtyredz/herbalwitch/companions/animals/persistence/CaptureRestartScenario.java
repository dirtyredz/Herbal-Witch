package io.github.dirtyredz.herbalwitch.companions.animals.persistence;

import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter.Stage;

/**
 * Where a real capture or release is stopped, and what a restarted server must then make of it.
 * {@code write} counts the ledger writes of that one operation (capture: 1 PREPARED, 2 STORED;
 * release: 1 RELEASE_PREPARED, 2 RELEASED); 0 runs the operation to completion.
 */
enum CaptureRestartScenario {
    CAPTURE_UNWRITTEN(Op.CAPTURE, 1, Stage.BEFORE_WRITE, Expect.ABSENT),
    CAPTURE_UNREPLACED(Op.CAPTURE, 1, Stage.BEFORE_REPLACE, Expect.ABSENT),
    CAPTURE_PREPARED(Op.CAPTURE, 1, Stage.BEFORE_VERIFY, Expect.QUARANTINED),
    CAPTURE_DISCARDED(Op.CAPTURE, 2, Stage.BEFORE_WRITE, Expect.QUARANTINED),
    CAPTURE_STORED_NO_ITEM(Op.CAPTURE, 2, Stage.BEFORE_VERIFY, Expect.STORED_REISSUE),
    CAPTURE_COMPLETE(Op.CAPTURE, 0, null, Expect.STORED_TOKEN),
    RELEASE_UNREPLACED(Op.RELEASE, 1, Stage.BEFORE_REPLACE, Expect.STORED_TOKEN),
    RELEASE_PREPARED(Op.RELEASE, 1, Stage.BEFORE_VERIFY, Expect.QUARANTINED),
    RELEASE_INSERTED(Op.RELEASE, 2, Stage.BEFORE_WRITE, Expect.QUARANTINED),
    RELEASE_UNCOMPACTED(Op.RELEASE, 2, Stage.BEFORE_VERIFY, Expect.QUARANTINED),
    RELEASE_COMPLETE(Op.RELEASE, 0, null, Expect.RELEASED);

    enum Op { CAPTURE, RELEASE }

    /**
     * ABSENT: nothing durable, the source animal simply returns. QUARANTINED: payload retained, every
     * stale incarnation refused, operator recovery restores exactly one. STORED_REISSUE: stored but the
     * item never reached the player; reissue restores it. STORED_TOKEN: the held token releases once.
     * RELEASED: the compact tombstone admits only the released incarnation.
     */
    enum Expect { ABSENT, QUARANTINED, STORED_REISSUE, STORED_TOKEN, RELEASED }

    final Op op;
    final int write;
    final Stage stage;
    final Expect expect;

    CaptureRestartScenario(Op op, int write, Stage stage, Expect expect) {
        this.op = op;
        this.write = write;
        this.stage = stage;
        this.expect = expect;
    }

    String dir() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    String player() {
        return "capture_restart_" + dir();
    }
}
