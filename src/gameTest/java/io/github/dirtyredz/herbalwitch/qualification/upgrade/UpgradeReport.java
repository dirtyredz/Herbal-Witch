package io.github.dirtyredz.herbalwitch.qualification.upgrade;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What one save-upgrade JVM observed: written as {@code evidence/<phase>.json} for compare.mjs. */
final class UpgradeReport {
    private final Map<String, Object> data = new LinkedHashMap<>();
    private final List<String> failures = new ArrayList<>();
    private final List<Map<String, Object>> created = new ArrayList<>();
    private final List<Map<String, Object>> players = new ArrayList<>();
    private final List<Map<String, Object>> jobs = new ArrayList<>();

    UpgradeReport(String phase, int chunks) {
        data.put("phase", phase);
        data.put("process", ProcessHandle.current().pid());
        data.put("chunks", chunks);
        data.put("createdBlockEntities", created);
        data.put("players", players);
        data.put("jobs", jobs);
        data.put("failures", failures);
    }

    void put(String key, Object value) {
        data.put(key, value);
    }

    void created(Map<String, Object> entry) {
        created.add(entry);
    }

    void player(Map<String, Object> entry) {
        players.add(entry);
    }

    void job(Map<String, Object> entry) {
        jobs.add(entry);
    }

    void fail(String failure) {
        failures.add(failure);
    }

    List<String> failures() {
        return failures;
    }

    void write(Path evidence) {
        try {
            Files.createDirectories(evidence);
            Files.writeString(evidence.resolve(data.get("phase") + ".json"), new GsonBuilder().setPrettyPrinting().create().toJson(data));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
