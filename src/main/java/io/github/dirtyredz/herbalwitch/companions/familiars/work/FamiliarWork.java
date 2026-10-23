package io.github.dirtyredz.herbalwitch.companions.familiars.work;

import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import java.util.*;

/** Shared per-dimension budgets for familiar AI and abilities. Tasks must revalidate when their turn arrives. */
public final class FamiliarWork {
    public interface Task { boolean valid(); void perform(); }
    private static final Map<ServerLevel, FamiliarWork> LEVELS = new WeakHashMap<>();
    private final FamiliarWorkQueue<Task> paths = new FamiliarWorkQueue<>(), scans = new FamiliarWorkQueue<>();
    private int lastPaths, lastScans;
    private FamiliarWork() {}
    public static FamiliarWork in(ServerLevel level) { return LEVELS.computeIfAbsent(level, ignored -> new FamiliarWork()); }
    public void path(Task task) { paths.offer(task); }
    public void scan(Task task) { scans.offer(task); }
    public void remove(Task task) { paths.remove(task); scans.remove(task); }
    public int lastPaths() { return lastPaths; }
    public int lastScans() { return lastScans; }
    private static void execute(Task task) { if (task.valid()) task.perform(); }
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        var work = LEVELS.get(level); if (work == null) return;
        var rules = FamiliarServerConfig.current();
        work.lastPaths = work.paths.run(rules.paths(), FamiliarWork::execute);
        work.lastScans = work.scans.run(rules.scans(), FamiliarWork::execute);
    }
    public static void stopped(ServerStoppedEvent event) { LEVELS.keySet().removeIf(level -> level.getServer() == event.getServer()); }
}
