package io.github.dirtyredz.herbalwitch.qualification.performance;

import net.minecraft.server.MinecraftServer;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Full server tick durations, one sample per tick, read from vanilla's own tick-time ring
 * ({@code MinecraftServer.tickTimes}), so the measurement adds no instrumentation to the tick itself.
 * GC time is the JVM's collector total over the window.
 */
final class TickSampler {
    private final MinecraftServer server;
    private final List<Long> nanos = new ArrayList<>();
    private long lastTick = -1;
    private long gcStart;

    TickSampler(MinecraftServer server) {
        this.server = server;
    }

    void start() {
        nanos.clear();
        lastTick = server.getTickCount();
        gcStart = gcMillis();
    }

    /** Called once per server tick: records the previous, completed tick. */
    void sample() {
        int now = server.getTickCount();
        if (now == lastTick) return;
        lastTick = now;
        nanos.add(server.tickTimes[(now - 1) % server.tickTimes.length]);
    }

    int size() {
        return nanos.size();
    }

    /** p50/p95/p99/max in milliseconds, ticks over 50 ms, mean TPS-equivalent and GC milliseconds. */
    Map<String, Object> stats() {
        long[] sorted = nanos.stream().mapToLong(Long::longValue).sorted().toArray();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ticks", sorted.length);
        out.put("p50", ms(percentile(sorted, 50)));
        out.put("p95", ms(percentile(sorted, 95)));
        out.put("p99", ms(percentile(sorted, 99)));
        out.put("max", ms(sorted.length == 0 ? 0 : sorted[sorted.length - 1]));
        out.put("mean", ms((long) Arrays.stream(sorted).average().orElse(0)));
        out.put("over50ms", Arrays.stream(sorted).filter(n -> n > 50_000_000L).count());
        out.put("gcMillis", gcMillis() - gcStart);
        return out;
    }

    private static long percentile(long[] sorted, int p) {
        if (sorted.length == 0) return 0;
        int index = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(sorted.length - 1, index))];
    }

    private static double ms(long nanos) {
        return Math.round(nanos / 10_000.0) / 100.0;
    }

    private static long gcMillis() {
        long total = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) total += Math.max(0, gc.getCollectionTime());
        return total;
    }
}
