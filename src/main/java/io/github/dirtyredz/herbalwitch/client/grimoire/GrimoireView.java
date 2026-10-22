package io.github.dirtyredz.herbalwitch.client.grimoire;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.grimoire.widgets.EntryList;
import io.github.dirtyredz.herbalwitch.client.grimoire.widgets.EntryPage;
import io.github.dirtyredz.herbalwitch.client.grimoire.widgets.TeachingPanel;
import io.github.dirtyredz.herbalwitch.client.grimoire.widgets.TextButton;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireAccess;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.network.GrimoirePackets;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderRequest;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * The reusable book: an entry list (search, category and knowledge filters) and an entry page with
 * back navigation, laid out either as a two-page spread (the reader) or as one column that shows
 * the list or a page (the panel beside a station). Reads the client's content and discoveries; when
 * it belongs to an open reader it also asks the server for counts and offers research and teaching.
 */
public final class GrimoireView {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "textures/gui/grimoire/reader.png");
    public static final String KEY = "grimoire." + HerbalWitch.MOD_ID + ".ui.";

    /** The open reader this view belongs to, for requests to the server. */
    public record ReaderLink(int containerId, long nonce, GrimoireAccess access) {
    }

    public enum Layout {
        SPREAD,
        COLUMN
    }

    private final Font font;
    private final Layout layout;
    private final Optional<ReaderLink> reader;
    private final EntryList list;
    private final EntryPage page;
    private final TeachingPanel teaching;
    private final Deque<EntryKey> history = new ArrayDeque<>();
    private Optional<EntryKey> current = Optional.empty();
    private int x;
    private int y;
    private int width;
    private int height;

    public GrimoireView(Font font, Layout layout, Optional<ReaderLink> reader, EntryList.Filter initialFilter, Optional<String> category) {
        this.font = font;
        this.layout = layout;
        this.reader = reader;
        this.list = new EntryList(this, font, initialFilter, category);
        this.page = new EntryPage(this, font);
        this.teaching = new TeachingPanel(this, font);
    }

    /** Places the view; in a spread the left half lists and the right half shows the page. */
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        if (layout == Layout.SPREAD) {
            int half = (width - 12) / 2;
            list.setBounds(x, y, half, height);
            page.setBounds(x + half + 12, y, half, height);
        } else {
            list.setBounds(x, y, width, height);
            page.setBounds(x, y, width, height);
        }
    }

    public GrimoireContent content() {
        return GrimoireClientState.INSTANCE.content();
    }

    /** Readable in full: public guidance or a discovery of this player. */
    public boolean readable(GrimoireEntry entry) {
        return entry.always() || GrimoireClientState.INSTANCE.knows(entry.key());
    }

    public Optional<ReaderLink> reader() {
        return reader;
    }

    public Optional<GrimoireEntry> current() {
        return current.flatMap(k -> content().get(k));
    }

    public Font font() {
        return font;
    }

    public TeachingPanel teaching() {
        return teaching;
    }

    /** Opens an entry, remembering the one it came from. */
    public void open(EntryKey key) {
        current.filter(k -> !k.equals(key)).ifPresent(history::push);
        current = Optional.of(key);
        page.reset();
        select(Optional.of(key));
    }

    public void back() {
        if (!history.isEmpty()) {
            current = Optional.of(history.pop());
        } else {
            current = Optional.empty();
        }
        page.reset();
        select(current);
    }

    public boolean canGoBack() {
        return current.isPresent() && (layout == Layout.COLUMN || !history.isEmpty());
    }

    /** Tells the server which recipe the counts are for (readers only). */
    private void select(Optional<EntryKey> key) {
        reader.ifPresent(r -> GrimoirePackets.sendToServer(new ReaderRequest(r.containerId(), r.nonce(),
                GrimoireClientState.INSTANCE.revision(), ReaderRequest.Op.SELECT,
                key.filter(k -> k.kind() == EntryKey.Kind.RECIPE))));
    }

    /** Asks the server to research a recipe (readers at a research block only). */
    public void research(EntryKey key) {
        reader.ifPresent(r -> GrimoirePackets.sendToServer(new ReaderRequest(r.containerId(), r.nonce(),
                GrimoireClientState.INSTANCE.revision(), ReaderRequest.Op.RESEARCH, Optional.of(key))));
    }

    private boolean showingPage() {
        return layout == Layout.SPREAD || current.isPresent();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (current.isPresent() && current().isEmpty()) {
            current = Optional.empty(); // its content was removed by a reload
        }
        if (layout == Layout.SPREAD || current.isEmpty()) {
            list.render(graphics, mouseX, mouseY);
        }
        if (showingPage()) {
            if (current.isEmpty() && layout == Layout.SPREAD) {
                page.renderEmpty(graphics, teaching, mouseX, mouseY);
            } else {
                page.render(graphics, mouseX, mouseY);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (showingPage() && page.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return (layout == Layout.SPREAD || current.isEmpty()) && list.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (showingPage() && page.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return (layout == Layout.SPREAD || current.isEmpty()) && list.mouseScrolled(mouseX, mouseY, delta);
    }

    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return (layout == Layout.SPREAD || current.isEmpty()) && list.keyPressed(key, scanCode, modifiers);
    }

    public boolean charTyped(char c, int modifiers) {
        return (layout == Layout.SPREAD || current.isEmpty()) && list.charTyped(c, modifiers);
    }

    /** Whether typed keys go to the search box (so the inventory key does not close the screen). */
    public boolean searching() {
        return list.searching();
    }

    public void tick() {
        list.tick();
    }

    /** The item under the mouse on the page, for the screen to show its tooltip last. */
    public ItemStack hovered() {
        return showingPage() && current.isPresent() ? page.hovered() : ItemStack.EMPTY;
    }

    public MutableComponent text(String suffix, Object... args) {
        return textOf(suffix, args);
    }

    /** {@code grimoire.herbalwitch.ui.<suffix>}. */
    public static MutableComponent textOf(String suffix, Object... args) {
        return Component.translatable(KEY + suffix, args);
    }

    /** A small button in the view's style. */
    public TextButton button(int x, int y, Component label, boolean enabled, Runnable action) {
        return TextButton.fitted(font, x, y, label, enabled, action);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
