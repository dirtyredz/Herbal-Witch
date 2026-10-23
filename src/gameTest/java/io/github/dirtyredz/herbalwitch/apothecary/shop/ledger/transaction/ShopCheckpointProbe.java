package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

/** Test-source-only: makes the next player checkpoints on this thread fail after the save call. Never shipped. */
public final class ShopCheckpointProbe implements AutoCloseable {
    public ShopCheckpointProbe() {
        this(stage -> {
            if (stage.equals("after_save")) throw new IllegalStateException("simulated player save failure");
        });
    }

    /** Any observer of the two checkpoint stages; it may throw to simulate a failure or a crash. */
    public ShopCheckpointProbe(java.util.function.Consumer<String> observer) {
        if (ShopPlayerCheckpoint.FAULT.get() != null) throw new IllegalStateException("Checkpoint probe already installed");
        ShopPlayerCheckpoint.FAULT.set(observer);
    }

    @Override
    public void close() {
        ShopPlayerCheckpoint.FAULT.remove();
    }
}
