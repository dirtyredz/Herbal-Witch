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

    /**
     * Bootstrap reaches the integration package only behind the loaded-mod guard, and nothing else outside the
     * package reaches it at all: with Ponder absent, no class that links to it is ever resolved.
     */
    @Test
    void bootstrapReachesThePonderPackageOnlyBehindTheLoadedGuard() throws IOException {
        Path allowed = ROOT.resolve("client/grimoire/ponder");
        Path bootstrap = ROOT.resolve("client/HerbalWitchClient.java");
        var reference = Pattern.compile("\\bherbalwitch\\.client\\.grimoire\\.ponder\\.");
        for (Path file : javaFiles(ROOT)) {
            if (!file.startsWith(allowed) && !file.equals(bootstrap)) reject(file, reference);
        }
        String client = Files.readString(bootstrap);
        var matches = reference.matcher(client);
        int count = 0;
        while (matches.find()) count++;
        assertEquals(1, count, "HerbalWitchClient must reference the Ponder package exactly once, inside the guard");
        assertTrue(Pattern.compile("if \\(ModList\\.get\\(\\)\\.isLoaded\\(PONDER_MOD_ID\\)\\) \\{\\s*"
                        + "event\\.enqueueWork\\(io\\.github\\.dirtyredz\\.herbalwitch\\.client\\.grimoire\\.ponder\\.PonderIntegration::setup\\);")
                .matcher(client).find(), "The Ponder setup call must sit inside the isLoaded guard");
        assertTrue(client.contains("PONDER_MOD_ID = \"ponder\""), "The guard must check Ponder's mod id");
    }

    /** Gameplay a lesson must never execute: station, inventory, processing, fuel, payment or player services. */
    private static final Pattern LESSON_GAMEPLAY = Pattern.compile(
            "(?m)^import (?:static )?io\\.github\\.dirtyredz\\.herbalwitch\\.(?:apothecary\\.(?:shop|stations\\.(?:process|block|everyday|advanced|heat|registry|grimoire))"
                    + "|magic|companions|platform\\.(?:permission|persistence|player))\\.[^;]+;"
                    + "|\\b(?:getCapability|insertItem|extractItem|setStackInSlot|setItem|modifyBlockEntity(?:NBT)?|ServerLevel|ServerPlayer|ShopTransfer|\\w+Service)\\b");

    /** Lessons are inert illustrations: block states, item entities, overlays and particles only. */
    @Test
    void ponderLessonsNeverReachGameplayServicesOrStationContents() throws IOException {
        for (Path file : javaFiles(ROOT.resolve("client/grimoire/ponder"))) reject(file, LESSON_GAMEPLAY);
    }

    @Test
    void lessonGameplayGuardRecognisesServiceCallsAndContents() {
        for (String sample : List.of("import io.github.dirtyredz.herbalwitch.apothecary.stations.process.StationProcessor;",
                "import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;",
                "scene.world().modifyBlockEntityNBT(sel, Entity.class, tag -> {})", "WitchStatusService.spend(x)",
                "handler.insertItem(0, stack, false)")) {
            assertTrue(LESSON_GAMEPLAY.matcher(sample).find(), sample);
        }
        assertFalse(LESSON_GAMEPLAY.matcher("import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;").find());
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
