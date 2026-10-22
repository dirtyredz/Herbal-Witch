package io.github.dirtyredz.herbalwitch.client.grimoire.widgets;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireClientState;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireView;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.research.Familiarity;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.research.ResearchService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The index page: a search box, a category filter (all, guide, stations, herbs, each processing
 * category), a knowledge filter (everything, what this player knows, or what they could research
 * now) and a scrolling list of titles marked known or unknown. Unknown entries stay listed with
 * their titles, so discovery never hides where to look next.
 */
public final class EntryList {
    /** Which entries the list shows. */
    public enum Filter {
        ALL,
        KNOWN,
        RESEARCH
    }

    private static final int ROW = 11;
    private static final int HEADER = 30;
    private final GrimoireView view;
    private final Font font;
    private Filter filter;
    private Optional<String> category;
    private EditBox search;
    private int scroll;
    private int x;
    private int y;
    private int width;
    private int height;
    private final List<TextButton> buttons = new ArrayList<>();
    private List<GrimoireEntry> rows = List.of();
    private int researchGeneration = -1;
    private long researchFingerprint;
    private List<GrimoireEntry> researchable = List.of();

    public EntryList(GrimoireView view, Font font, Filter filter, Optional<String> category) {
        this.view = view;
        this.font = font;
        this.filter = filter;
        this.category = category;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        String text = search == null ? "" : search.getValue();
        search = new EditBox(font, x + 2, y + 2, width - 4, 10, view.text("search"));
        search.setBordered(false);
        search.setMaxLength(48);
        search.setTextColor(0x3B2416);
        search.setValue(text);
        search.setHint(view.text("search").withStyle(s -> s.withColor(0x8E7A56)));
    }

    private List<String> categories() {
        List<String> out = new ArrayList<>();
        for (GrimoireEntry entry : view.content().entries()) {
            if (!out.contains(entry.category())) {
                out.add(entry.category());
            }
        }
        return out;
    }

    private Component categoryName(Optional<String> id) {
        if (id.isEmpty()) {
            return view.text("category.all");
        }
        return switch (id.get()) {
            case GrimoireContent.GUIDE, GrimoireContent.STATIONS, GrimoireContent.HERBS, GrimoireContent.FURNITURE -> view.text("category." + id.get());
            default -> Component.translatable("category." + HerbalWitch.MOD_ID + ".processing." + id.get());
        };
    }

    private void cycleCategory() {
        List<String> all = categories();
        int i = category.map(all::indexOf).orElse(-1) + 1;
        category = i < all.size() ? Optional.of(all.get(i)) : Optional.empty();
        scroll = 0;
    }

    private void cycleFilter() {
        Filter[] values = Filter.values();
        filter = values[(filter.ordinal() + 1) % values.length];
        scroll = 0;
    }

    private List<GrimoireEntry> visible() {
        String query = search.getValue().trim().toLowerCase(Locale.ROOT);
        List<GrimoireEntry> research = filter == Filter.RESEARCH ? researchable() : List.of();
        return view.content().entries().stream()
                .filter(e -> category.map(e.category()::equals).orElse(true))
                .filter(e -> switch (filter) {
                    case ALL -> true;
                    case KNOWN -> view.readable(e);
                    case RESEARCH -> research.contains(e);
                })
                .filter(e -> query.isEmpty() || e.title().getString().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    /** Unknown recipes the player could research now, by the same rule the server applies. */
    private List<GrimoireEntry> researchable() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of();
        }
        List<ItemStack> carried = Familiarity.carried(player);
        long fingerprint = Familiarity.fingerprint(carried);
        GrimoireClientState state = GrimoireClientState.INSTANCE;
        if (researchGeneration != state.generation() || researchFingerprint != fingerprint) {
            Familiarity familiarity = new Familiarity(carried, state::knows);
            GrimoireContent content = view.content();
            researchable = content.recipes().stream()
                    .filter(e -> ResearchService.researchable(content, e.key(), familiarity, state::knows)).toList();
            researchGeneration = state.generation();
            researchFingerprint = fingerprint;
        }
        return researchable;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(x, y, x + width, y + 13, 0xFF8E7A56);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 12, 0xFFF2E8CF);
        search.render(graphics, mouseX, mouseY, 0);
        buttons.clear();
        int half = (width - 2) / 2;
        buttons.add(new TextButton(x, y + 15, half, clip(categoryName(category), half - 6), true, this::cycleCategory));
        buttons.add(new TextButton(x + half + 2, y + 15, width - half - 2, clip(view.text("filter." + filter.name()
                .toLowerCase(Locale.ROOT)), width - half - 8), true, this::cycleFilter));
        buttons.forEach(b -> b.render(graphics, font, mouseX, mouseY));
        rows = visible();
        int capacity = Math.max(1, (height - HEADER) / ROW);
        scroll = Math.max(0, Math.min(scroll, rows.size() - capacity));
        if (rows.isEmpty()) {
            graphics.drawWordWrap(font, view.text("empty"), x + 2, y + HEADER + 2, width - 4, TextButton.FAINT);
        }
        for (int i = 0; i < capacity && scroll + i < rows.size(); i++) {
            GrimoireEntry entry = rows.get(scroll + i);
            int rowY = y + HEADER + i * ROW;
            boolean hover = mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ROW;
            boolean selected = view.current().map(c -> c.key().equals(entry.key())).orElse(false);
            if (hover || selected) {
                graphics.fill(x, rowY, x + width, rowY + ROW, selected ? 0x40B0305A : 0x304A2A55);
            }
            boolean readable = view.readable(entry);
            graphics.blit(GrimoireView.ATLAS, x + 1, rowY + 1, readable ? 20 : 30, 184, 9, 9);
            graphics.drawString(font, clip(entry.title(), width - 14), x + 12, rowY + 2,
                    readable ? TextButton.INK : TextButton.PLUM, false);
        }
        if (rows.size() > capacity) {
            int track = height - HEADER;
            int thumb = Math.max(8, track * capacity / rows.size());
            int top = y + HEADER + (track - thumb) * scroll / Math.max(1, rows.size() - capacity);
            graphics.fill(x + width - 2, top, x + width, top + thumb, 0xFF4A2A55);
        }
    }

    private Component clip(Component text, int max) {
        if (font.width(text) <= max) {
            return text;
        }
        return Component.literal(font.plainSubstrByWidth(text.getString(), max - font.width("..")) + "..");
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inSearch = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 13;
        search.setFocused(inSearch);
        if (inSearch) {
            search.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        for (TextButton b : buttons) {
            if (b.click(mouseX, mouseY)) {
                return true;
            }
        }
        if (mouseX >= x && mouseX < x + width && mouseY >= y + HEADER && mouseY < y + height) {
            int index = scroll + (int) ((mouseY - y - HEADER) / ROW);
            if (index >= 0 && index < rows.size()) {
                view.open(rows.get(index).key());
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            scroll = Math.max(0, scroll - (int) Math.signum(delta) * 3);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return search.isFocused() && search.keyPressed(key, scanCode, modifiers);
    }

    public boolean charTyped(char c, int modifiers) {
        if (search.isFocused() && search.charTyped(c, modifiers)) {
            scroll = 0;
            return true;
        }
        return false;
    }

    public boolean searching() {
        return search != null && search.isFocused();
    }

    public void tick() {
        if (search != null) {
            search.tick();
        }
    }
}
