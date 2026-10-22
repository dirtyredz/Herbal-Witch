package io.github.dirtyredz.herbalwitch.data.fixture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every literal fake-player name in the GameTests is used by one test file only. Fake players are
 * cached by name, so two tests sharing one (once they land in the same batch) change each other's
 * inventory mid-test: the 0.5.0 lantern failure, where a light test reused "lamplighter".
 */
class GameTestPlayerNamesTest {
    private static final Path GAME_TESTS = Path.of("src/gameTest/java");
    /** A fixture call whose second argument is a complete literal name: {@code (helper, "name",} or {@code (helper, "name")}. */
    private static final Pattern NAME = Pattern.compile("\\(helper, \"([a-z0-9_]+)\"[,)]");

    @Test
    void literalFakePlayerNamesAreUnique() throws IOException {
        Map<String, List<String>> uses = new HashMap<>();
        try (Stream<Path> files = Files.walk(GAME_TESTS)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher m = NAME.matcher(Files.readString(file));
                while (m.find()) {
                    uses.computeIfAbsent(m.group(1), k -> new ArrayList<>()).add(file.getFileName().toString());
                }
            }
        }
        assertTrue(uses.size() > 50, "names found: " + uses.size());
        Map<String, List<String>> shared = new HashMap<>();
        uses.forEach((name, where) -> {
            if (where.size() > 1) {
                shared.put(name, where);
            }
        });
        assertEquals(Map.of(), shared, "fake-player names used more than once");
    }
}
