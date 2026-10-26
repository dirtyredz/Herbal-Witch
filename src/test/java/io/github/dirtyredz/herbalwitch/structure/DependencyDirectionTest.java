package io.github.dirtyredz.herbalwitch.structure;

import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.SleepRecovery;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Cheap guards for ownership contracts that ordinary compilation cannot enforce. */
class DependencyDirectionTest {
    private static final Path ROOT = Path.of("src/main/java/io/github/dirtyredz/herbalwitch");

    @Test
    void documentationLibrariesStayInClientPresentationAndCreateIsForbidden() throws IOException {
        var guide = Pattern.compile("\\bguideme\\.");
        var create = Pattern.compile("\\bcom\\.simibubi\\.create\\.");
        for (Path file : javaFiles(ROOT)) {
            reject(file, create);
            if (!file.startsWith(ROOT.resolve("client"))) reject(file, guide);
        }
    }

    /** Ponder, its bundled Catnip, and Flywheel (current and legacy packages). */
    private static final Pattern OPTIONAL_ANIMATION = Pattern.compile(
            "\\b(?:net\\.createmod\\.(?:ponder|catnip)|dev\\.engine_room\\.flywheel|com\\.jozufozu\\.flywheel)\\.");

    /**
     * Ponder and Flywheel are optional, compile-only libraries and the mod must load without them, so their types
     * appear only in the isolated integration package: never in bootstrap signatures, fields or annotations
     * elsewhere, and never in GameTests (which run without them).
     */
    @Test
    void optionalAnimationLibrariesStayInTheIsolatedPonderPackage() throws IOException {
        Path allowed = ROOT.resolve("client/grimoire/ponder");
        for (Path file : javaFiles(ROOT)) {
            if (!file.startsWith(allowed)) reject(file, OPTIONAL_ANIMATION);
        }
        for (Path file : javaFiles(Path.of("src/gameTest/java"))) reject(file, OPTIONAL_ANIMATION);
    }

    @Test
    void optionalAnimationGuardRecognisesEveryOptionalPackage() {
        for (String sample : List.of("import net.createmod.ponder.api.registration.PonderPlugin;",
                "import net.createmod.catnip.math.Pointing;", "dev.engine_room.flywheel.api.Flywheel.ID")) {
            assertTrue(OPTIONAL_ANIMATION.matcher(sample).find(), sample);
        }
        assertFalse(OPTIONAL_ANIMATION.matcher("import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons;").find());
    }

    @Test
    void familiarWorkCannotAcquireChunksOrTickets() throws IOException {
        var forbidden = Pattern.compile("\\b(?:getChunk|getChunkAt|getChunkFuture|addRegionTicket|registerTickingTicket|forceChunk|setChunkForced)\\s*\\(");
        for (Path file : javaFiles(ROOT.resolve("companions/familiars"))) reject(file, forbidden);
    }

    /** Commerce never loads a chunk or adds a ticket: unloaded shops simply do not sell (no catch-up). */
    @Test
    void commerceCannotAcquireChunksTicketsOrTeleport() throws IOException {
        var forbidden = Pattern.compile("\\b(?:getChunk|getChunkAt|getChunkFuture|addRegionTicket|registerTickingTicket|forceChunk|setChunkForced|teleportTo|randomTeleport)\\s*\\(");
        for (Path file : javaFiles(ROOT.resolve("apothecary/shop"))) reject(file, forbidden);
    }

    /** Stations and decor never import commerce; commerce contributes to them, never the reverse. */
    @Test
    void stationsAndDecorDoNotDependOnCommerce() throws IOException {
        var forbidden = Pattern.compile("import\\s+io\\.github\\.dirtyredz\\.herbalwitch\\.apothecary\\.shop\\.");
        for (String dir : List.of("apothecary/stations", "apothecary/brewing", "apothecary/botany", "decor")) {
            for (Path file : javaFiles(ROOT.resolve(dir))) reject(file, forbidden);
        }
    }

    private static List<Path> javaFiles(Path root) throws IOException {
        try (var files = Files.walk(root)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static void reject(Path file, Pattern forbidden) throws IOException {
        var match = forbidden.matcher(Files.readString(file));
        assertFalse(match.find(), () -> file + " imports/calls a forbidden implementation: " + match.group());
    }

    @Test
    void grimoireCoreUsesContractsAndNeutralFacts() throws IOException {
        var forbidden = Pattern.compile("(?m)^import (?:static )?io\\.github\\.dirtyredz\\.herbalwitch\\.(?:apothecary|magic|decor|client|compat)\\.[^;]+;");
        for (String area : List.of("content", "knowledge", "reading", "network")) {
            for (Path file : javaFiles(ROOT.resolve("grimoire/" + area))) reject(file, forbidden);
        }
    }

    @Test
    void recipeContractsDoNotImportStationExecutionOrRegistries() throws IOException {
        var forbidden = Pattern.compile("(?m)^import (?:static )?io\\.github\\.dirtyredz\\.herbalwitch\\.apothecary\\.stations\\.[^;]+;");
        for (Path file : javaFiles(ROOT.resolve("platform/recipe"))) reject(file, forbidden);
    }

    @Test
    void sharedCompanionPrerequisitesDoNotImportTheirFeatureConsumers() throws IOException {
        var lifecycle = Pattern.compile("io\\.github\\.dirtyredz\\.herbalwitch\\.companions\\.(?:animals|familiars)\\.");
        for (Path file : javaFiles(ROOT.resolve("companions/lifecycle"))) reject(file, lifecycle);
        var feature = Pattern.compile("io\\.github\\.dirtyredz\\.herbalwitch\\.(?:companions|magic|apothecary)\\.");
        for (Path file : javaFiles(ROOT.resolve("platform/persistence"))) reject(file, feature);
        var companion = Pattern.compile("io\\.github\\.dirtyredz\\.herbalwitch\\.companions\\.");
        for (Path file : javaFiles(ROOT.resolve("apothecary/brewing"))) reject(file, companion);
    }

    @Test
    void animalsAndLifecycleNeverDependOnFamiliars() throws IOException {
        var familiar = Pattern.compile("io\\.github\\.dirtyredz\\.herbalwitch\\.companions\\.familiars\\.");
        for (String area : List.of("animals", "lifecycle")) {
            for (Path file : javaFiles(ROOT.resolve("companions/" + area))) reject(file, familiar);
        }
    }

    @Test
    void onlyWandMayImportCompanionsFromMagic() throws IOException {
        var companion = Pattern.compile("io\\.github\\.dirtyredz\\.herbalwitch\\.companions\\.");
        for (Path file : javaFiles(ROOT.resolve("magic"))) {
            if (!file.startsWith(ROOT.resolve("magic/wand"))) reject(file, companion);
        }
    }

    @Test
    void wardServicePackageDependsDownwardsOnly() throws IOException {
        // WardService (ticking, server lifecycle) sits on top; its siblings use WardIndex/WardContext/WardLifecycle.
        var sibling = Pattern.compile("\\bWardService\\b");
        for (Path file : javaFiles(ROOT.resolve("magic/ward/service"))) {
            if (!file.getFileName().toString().equals("WardService.java")) reject(file, sibling);
        }
    }

    @Test
    void fakePlayerRegistrationIsGameTestOnly() throws IOException {
        var forbidden = Pattern.compile("OnlinePlayers\\s*\\.\\s*(?:register|Registration)\\b|import static [\\w.]*OnlinePlayers\\.register");
        for (Path file : javaFiles(ROOT)) reject(file, forbidden);
    }

    @Test
    void durableMutatorsStayInsideTheStatusPackage() {
        for (var method : WitchState.class.getDeclaredMethods()) {
            if (method.getName().startsWith("set") || Set.of("initiate", "restoreFrom").contains(method.getName())) {
                assertFalse(Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers()), method.toString());
            }
        }
        for (var type : List.of(EnergyService.class, SleepRecovery.class)) {
            for (var method : type.getDeclaredMethods()) {
                if (Set.of("debit", "recover", "awardPractice", "initiate", "clamp", "completed").contains(method.getName())) {
                    assertFalse(Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers()), method.toString());
                }
            }
        }
    }

    @Test
    void gameplayCannotReintroduceDirectDebitPracticeOrStateSetters() throws IOException {
        var forbidden = Pattern.compile("(?:EnergyService\\.(?:debit|recover|awardPractice|initiate|clamp)|SleepRecovery\\.completed|WitchStatusService\\.(?:debit|awardPractice)|\\.set(?:EnergyUnits|PracticeSpent|TeaCooldownTicks|RestfulTicks|LastSleepRewardDay))\\s*\\(");
        for (Path file : javaFiles(ROOT)) {
            if (!file.startsWith(ROOT.resolve("magic/witch/status"))) reject(file, forbidden);
        }
    }
}
