package io.github.dirtyredz.herbalwitch.client.grimoire;

import io.github.dirtyredz.herbalwitch.client.ritual.AltarScreen;
import io.github.dirtyredz.herbalwitch.client.stations.HearthScreen;
import io.github.dirtyredz.herbalwitch.client.stations.ProcessStationScreen;
import io.github.dirtyredz.herbalwitch.client.ward.WardScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GrimoirePanelHostTest {
    private static final List<Class<?>> HOSTS = List.of(ProcessStationScreen.class, HearthScreen.class, AltarScreen.class, WardScreen.class);

    private static final class Panel implements GrimoirePanelHost.Panel {
        boolean open, searching, book = true, hovered, pointer;
        String query = "lavender", page = "herbalwitch:witchcraft/energy";
        int keys, layouts;
        final List<String> events = new ArrayList<>();

        @Override public Button init(int left, int top, int width, int height) { layouts++; return null; }
        @Override public void tick() { if (!book) open = false; events.add("tick"); }
        @Override public void render(GuiGraphics graphics, int x, int y, float partial) { events.add("panel"); }
        @Override public boolean tooltip(GuiGraphics graphics, Font font, int x, int y) {
            if (open && hovered) { events.add("page-tooltip"); return true; }
            return false;
        }
        @Override public boolean mouseClicked(double x, double y, int button) { return open && pointer; }
        @Override public boolean mouseScrolled(double x, double y, double delta) { return open && pointer; }
        @Override public boolean keyPressed(int key, int scan, int modifiers) { keys++; return open && searching; }
        @Override public boolean charTyped(char c, int modifiers) { if (open && searching) { query += c; return true; } return false; }
    }

    @Test
    void everyHostUsesTheSharedRoutingAndEscapeAlwaysReachesTheScreen() {
        for (var host : HOSTS) {
            assertEquals(GrimoirePanelHost.class, host.getSuperclass(), host.getName());
            for (boolean open : List.of(false, true)) for (boolean search : List.of(false, true)) {
                Panel panel = new Panel(); panel.open = open; panel.searching = search;
                var route = new GrimoirePanelHost.Routing(panel);
                AtomicInteger screen = new AtomicInteger();
                assertTrue(route.keyPressed(256, 0, 0, () -> { screen.incrementAndGet(); return true; }), host.getName());
                assertEquals(1, screen.get(), host.getName() + " open=" + open + " search=" + search);
                assertEquals(0, panel.keys, "Escape must bypass panel search");
            }
        }
    }

    @Test
    void focusedSearchConsumesInventoryTypingAndOtherStatesFallBack() {
        for (var host : HOSTS) for (boolean open : List.of(false, true)) for (boolean search : List.of(false, true)) {
            Panel panel = new Panel(); panel.open = open; panel.searching = search;
            var route = new GrimoirePanelHost.Routing(panel);
            AtomicInteger screen = new AtomicInteger();
            assertTrue(route.keyPressed(69, 0, 0, () -> { screen.incrementAndGet(); return true; }));
            assertEquals(open && search ? 0 : 1, screen.get(), host.getName());
            route.charTyped('e', 0, () -> { screen.incrementAndGet(); return true; });
            assertEquals(open && search ? "lavendere" : "lavender", panel.query);
        }
    }

    @Test
    void pointerEventsOnlyFallBackWhenThePanelDoesNotConsumeThem() {
        for (var host : HOSTS) for (boolean open : List.of(false, true)) for (boolean pointer : List.of(false, true)) {
            Panel panel = new Panel(); panel.open = open; panel.pointer = pointer;
            var route = new GrimoirePanelHost.Routing(panel);
            AtomicInteger screen = new AtomicInteger();
            assertTrue(route.mouseClicked(1, 2, 0, () -> { screen.incrementAndGet(); return true; }));
            assertTrue(route.mouseScrolled(1, 2, -1, () -> { screen.incrementAndGet(); return true; }));
            assertEquals(open && pointer ? 0 : 2, screen.get(), host.getName());
        }
    }

    @Test
    void resizeRetainsTheDelegateStateAndBookLossIsProcessedByItsTick() {
        for (var host : HOSTS) {
            Panel panel = new Panel(); panel.open = true; panel.searching = true;
            var route = new GrimoirePanelHost.Routing(panel);
            route.init(20, 30, 176, 166);
            route.init(50, 60, 176, 166);
            assertEquals(2, panel.layouts, host.getName());
            assertEquals("lavender", panel.query);
            assertEquals("herbalwitch:witchcraft/energy", panel.page);
            assertTrue(panel.open && panel.searching);
            panel.book = false;
            route.tick();
            assertFalse(panel.open);
            assertFalse(route.keyPressed(69, 0, 0, () -> false));
        }
    }

    @Test
    void pageTooltipTakesPrecedenceAfterPanelDrawing() {
        for (var host : HOSTS) for (boolean hovered : List.of(false, true)) {
            Panel panel = new Panel(); panel.open = true; panel.hovered = hovered;
            var route = new GrimoirePanelHost.Routing(panel);
            route.render(null, null, 1, 2, 0, () -> panel.events.add("host-tooltip"));
            assertEquals(List.of("panel", hovered ? "page-tooltip" : "host-tooltip"), panel.events, host.getName());
        }
    }
}
