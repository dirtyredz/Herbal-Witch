package io.github.dirtyredz.herbalwitch.grimoire.content;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GrimoireEntryTest {
    private static final EntryKey KEY = EntryKey.of(EntryKey.Kind.GUIDE, "test:page");
    private static final EntryKey FIRST = EntryKey.of(EntryKey.Kind.GUIDE, "test:first");
    private static final EntryKey SECOND = EntryKey.of(EntryKey.Kind.GUIDE, "test:second");


    @GameTest(template = TEMPLATE)
    public static void constructorCopiesTheIconAndBodyList(GameTestHelper helper) {
        ItemStack icon = new ItemStack(Items.PAPER, 3);
        icon.getOrCreateTag().putString("origin", "before");
        List<Component> body = new ArrayList<>(List.of(Component.literal("body")));
        GrimoireEntry entry = GrimoireEntry.page(KEY, "guide", Component.literal("title"), icon, body, List.of());
        assertTrue(entry.always());
        assertTrue(entry.hint().isEmpty());
        assertTrue(entry.recipe().isEmpty());
        icon.setCount(1);
        icon.getOrCreateTag().putString("origin", "after");
        body.clear();
        assertEquals(3, entry.icon().getCount());
        assertEquals("before", entry.icon().getTag().getString("origin"));
        assertEquals(List.of(Component.literal("body")), entry.body());
        entry.icon().setCount(8);
        assertEquals(3, entry.icon().getCount());
        try {
            entry.body().clear();
            throw new AssertionError("body must be immutable");
        } catch (UnsupportedOperationException expected) {
            // The original constructor freezes the body list.
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void linksDeduplicateInFirstOccurrenceOrder(GameTestHelper helper) {
        GrimoireEntry entry = GrimoireEntry.page(KEY, "guide", Component.literal("title"), ItemStack.EMPTY, List.of(),
                List.of(SECOND, FIRST, SECOND, FIRST));
        assertEquals(List.of(SECOND, FIRST), entry.links());
        assertEquals(List.of(FIRST), entry.withLinksIn(FIRST::equals).links());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void hintedPrivateEntriesRetainTheirDistinctFlags(GameTestHelper helper) {
        Component hint = Component.literal("find this");
        GrimoireEntry entry = new GrimoireEntry(KEY, "guide", Component.literal("title"), ItemStack.EMPTY, List.of(),
                List.of(), Optional.of(hint), false, Optional.empty());
        assertFalse(entry.always());
        assertEquals(Optional.of(hint), entry.hint());
        assertTrue(entry.recipe().isEmpty());
        helper.succeed();
    }

    private static void assertEquals(Object expected, Object actual) {
        check(java.util.Objects.equals(expected, actual), "expected " + expected + ", got " + actual);
    }

    private static void assertTrue(boolean actual) { check(actual, "expected true"); }
    private static void assertFalse(boolean actual) { check(!actual, "expected false"); }
}
