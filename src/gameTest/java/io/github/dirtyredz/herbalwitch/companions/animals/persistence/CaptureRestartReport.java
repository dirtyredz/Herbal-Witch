package io.github.dirtyredz.herbalwitch.companions.animals.persistence;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The two JVMs' evidence files; tools/qualification/restart/capture/verify-report.mjs judges them. */
final class CaptureRestartReport {
    private CaptureRestartReport() {
    }

    static Map<String, Object> prepared(int scenarios, int stopped) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("process", ProcessHandle.current().pid());
        data.put("scenarios", scenarios);
        data.put("stopped", stopped);
        return data;
    }

    static Map<String, Object> verified(List<Map<String, Object>> results, List<String> failures) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("process", ProcessHandle.current().pid());
        data.put("results", results);
        data.put("failures", failures);
        return data;
    }

    static void write(Path file, Map<String, Object> data) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(data));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
