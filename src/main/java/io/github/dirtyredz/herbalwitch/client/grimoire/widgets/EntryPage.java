package io.github.dirtyredz.herbalwitch.client.grimoire.widgets;

import io.github.dirtyredz.herbalwitch.client.compat.ViewerLayout;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireClientState;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireView;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ingredients.IngredientRequirements;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * One entry's page. A known recipe shows every requirement from the live recipe (accepted
 * alternatives cycle in the slot), the counts the server computed from the inventory and offhand,
 * the station, the result and its count, returned containers, time and heat. An unknown recipe
 * shows a teaser (title, result, station) and how to discover it; an unknown herb shows how to
 * find it. Guides, stations and herbs show their text. Links follow the preparation chain.
 */
public final class EntryPage {
    private static final int FOOTER = 14;
    private final GrimoireView view;
    private final Font font;
    private final List<TextButton> buttons = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();
    private ItemStack hovered = ItemStack.EMPTY;
    private int x;
    private int y;
    private int width;
    private int height;
    private int scroll;
    private int contentHeight;
    private int bodyTop;

    private record Link(int x, int y, int width, EntryKey key) {
        boolean contains(double mx, double my) {
            return mx >= x && mx < x + width && my >= y && my < y + 10;
        }
    }

    public EntryPage(GrimoireView view, Font font) {
        this.view = view;
        this.font = font;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void reset() {
        scroll = 0;
    }

    /** The stack under the mouse in the last frame, for the screen's tooltip. */
    public ItemStack hovered() {
        return hovered;
    }

    /** No entry chosen yet (the spread's right page): a prompt, and any invitation addressed to the player. */
    public void renderEmpty(GuiGraphics graphics, TeachingPanel teaching, int mouseX, int mouseY) {
        buttons.clear();
        links.clear();
        hovered = ItemStack.EMPTY;
        int top = y + teaching.renderIncoming(graphics, x, y, width, mouseX, mouseY, buttons);
        graphics.drawWordWrap(font, view.text("choose"), x + 2, top + 4, width - 4, TextButton.FAINT);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        buttons.clear();
        links.clear();
        hovered = ItemStack.EMPTY;
        Optional<GrimoireEntry> current = view.current();
        if (current.isEmpty()) {
            return;
        }
        GrimoireEntry entry = current.get();
        bodyTop = y + view.teaching().renderIncoming(graphics, x, y, width, mouseX, mouseY, buttons);
        int bottom = y + height - FOOTER;
        graphics.enableScissor(x, bodyTop, x + width, bottom);
        int start = bodyTop - scroll;
        int cy = header(graphics, entry, start, mouseX, mouseY);
        boolean readable = view.readable(entry);
        if (entry.recipe().isPresent()) {
            cy = readable ? recipe(graphics, entry, entry.recipe().get(), cy, mouseX, mouseY) : teaser(graphics, entry, cy);
        } else if (!readable) {
            cy = paragraph(graphics, entry.hint().orElse(view.text("unknown")), cy, TextButton.PLUM);
        }
        if (readable) {
            for (Component line : entry.body()) {
                cy = paragraph(graphics, line, cy, TextButton.INK);
            }
            cy = links(graphics, entry, cy, mouseX, mouseY);
            if (entry.recipe().isPresent()) {
                cy = view.teaching().renderInvite(graphics, entry, x, cy, width, mouseX, mouseY, buttons);
            }
        }
        contentHeight = cy + scroll - bodyTop;
        graphics.disableScissor();
        scroll = Math.max(0, Math.min(scroll, contentHeight - (bottom - bodyTop)));
        footer(graphics, entry, readable, bottom + 2, mouseX, mouseY);
        if (mouseY < bodyTop || mouseY >= bottom) {
            hovered = ItemStack.EMPTY;
        }
    }

    private int header(GuiGraphics graphics, GrimoireEntry entry, int cy, int mouseX, int mouseY) {
        item(graphics, entry.icon(), x, cy, mouseX, mouseY);
        List<FormattedCharSequence> title = font.split(entry.title(), width - 20);
        for (int i = 0; i < Math.min(2, title.size()); i++) {
            graphics.drawString(font, title.get(i), x + 19, cy + (title.size() == 1 ? 4 : i * 9), TextButton.PLUM, false);
        }
        return cy + 20;
    }

    private int teaser(GuiGraphics graphics, GrimoireEntry entry, int cy) {
        ProcessRecipe recipe = entry.recipe().orElseThrow();
        cy = station(graphics, recipe, cy);
        return paragraph(graphics, entry.hint().orElse(view.text("unknown")), cy + 2, TextButton.PLUM);
    }

    private int station(GuiGraphics graphics, ProcessRecipe recipe, int cy) {
        Component station = ProcessingCategories.byType(recipe.getType())
                .map(c -> StationBlocks.block(c.station()).getName()).orElse(Component.literal("?"));
        return paragraph(graphics, view.text("station", station), cy, TextButton.INK);
    }

    private int recipe(GuiGraphics graphics, GrimoireEntry entry, ProcessRecipe recipe, int cy, int mouseX, int mouseY) {
        ProcessRecipeDescription d = recipe.description();
        List<IngredientRequirements.Requirement> requirements = IngredientRequirements.of(d);
        Optional<ReaderUpdate> counts = view.reader().flatMap(r ->
                GrimoireClientState.INSTANCE.availability(r.containerId(), r.nonce(), entry.key()));
        for (int i = 0; i < requirements.size(); i++) {
            IngredientRequirements.Requirement req = requirements.get(i);
            ItemStack[] options = req.ingredient().getItems();
            ItemStack shown = options.length == 0 ? ItemStack.EMPTY
                    : options[(int) (Util.getMillis() / 1000 % options.length)].copyWithCount(req.count());
            graphics.blit(GrimoireView.ATLAS, x, cy, 0, 184, 18, 18);
            item(graphics, shown, x + 1, cy + 1, mouseX, mouseY);
            graphics.renderItemDecorations(font, shown, x + 1, cy + 1);
            Component role = view.text("role." + req.role().name().toLowerCase(java.util.Locale.ROOT));
            graphics.drawString(font, role, x + 21, cy + 1, TextButton.FAINT, false);
            if (options.length > 1) {
                graphics.drawString(font, view.text("alternatives", options.length), x + 21, cy + 10, TextButton.FAINT, false);
            }
            if (counts.isPresent() && i < counts.get().lines().size()) {
                ReaderUpdate.Line line = counts.get().lines().get(i);
                boolean enough = line.missing() == 0;
                Component have = view.text(enough ? "have" : "missing", line.available(), line.required(), line.missing());
                int w = font.width(have);
                graphics.drawString(font, have, x + width - w - 11, cy + 5, enough ? TextButton.GREEN : TextButton.PINK, false);
                graphics.blit(GrimoireView.ATLAS, x + width - 10, cy + 4, enough ? 20 : 60, 184, 9, 9);
            }
            cy += 20;
        }
        graphics.blit(GrimoireView.ATLAS, x, cy + 1, 72, 184, 22, 15);
        ItemStack result = d.result();
        graphics.blit(GrimoireView.ATLAS, x + 26, cy, 0, 184, 18, 18);
        item(graphics, result, x + 27, cy + 1, mouseX, mouseY);
        graphics.renderItemDecorations(font, result, x + 27, cy + 1);
        int rx = x + 48;
        for (ItemStack back : d.returns()) {
            graphics.drawString(font, "+", rx, cy + 5, TextButton.FAINT, false);
            item(graphics, back, rx + 7, cy + 1, mouseX, mouseY);
            rx += 25;
        }
        cy += 21;
        cy = station(graphics, recipe, cy);
        graphics.blit(GrimoireView.ATLAS, x, cy, 50, 184, 9, 9);
        graphics.drawString(font, Component.translatable(ProcessingCategories.footerKey(false),
                ProcessingCategories.seconds(d.duration())), x + 12, cy + 1, TextButton.INK, false);
        if (d.heated()) {
            graphics.blit(GrimoireView.ATLAS, x + 50, cy, 40, 184, 9, 9);
            graphics.drawString(font, view.text("heat"), x + 62, cy + 1, TextButton.PINK, false);
        }
        cy += 12;
        java.util.Optional<Component> witch = ViewerLayout.requirements(d);
        if (witch.isPresent()) {
            cy = paragraph(graphics, witch.get(), cy, TextButton.PINK);
        }
        if (!d.returns().isEmpty()) {
            cy = paragraph(graphics, view.text("returns"), cy, TextButton.FAINT);
        }
        if (view.reader().isPresent()) {
            cy = paragraph(graphics, view.text(counts.isPresent() ? "scope" : "counting"), cy, TextButton.FAINT);
            if (counts.isPresent() && counts.get().lines().stream().allMatch(l -> l.missing() == 0)) {
                cy = paragraph(graphics, view.text("ready"), cy, TextButton.GREEN);
            }
        } else {
            cy = paragraph(graphics, view.text("open_for_counts"), cy, TextButton.FAINT);
        }
        return cy + 2;
    }

    private int links(GuiGraphics graphics, GrimoireEntry entry, int cy, int mouseX, int mouseY) {
        if (entry.links().isEmpty()) {
            return cy;
        }
        graphics.drawString(font, view.text("see_also"), x, cy, TextButton.PLUM, false);
        cy += 10;
        for (EntryKey key : entry.links()) {
            Optional<GrimoireEntry> target = view.content().get(key);
            if (target.isEmpty()) {
                continue;
            }
            Component title = target.get().title();
            String text = font.plainSubstrByWidth(title.getString(), width - 8);
            int w = font.width(text);
            Link link = new Link(x + 6, cy, w, key);
            boolean hover = link.contains(mouseX, mouseY) && mouseY >= bodyTop;
            graphics.drawString(font, "-", x, cy, TextButton.FAINT, false);
            graphics.drawString(font, text, x + 6, cy, hover ? TextButton.PINK : TextButton.GREEN, false);
            links.add(link);
            cy += 10;
        }
        return cy + 2;
    }

    private int paragraph(GuiGraphics graphics, Component text, int cy, int colour) {
        for (FormattedCharSequence line : font.split(text, width - 2)) {
            graphics.drawString(font, line, x, cy, colour, false);
            cy += 9;
        }
        return cy + 2;
    }

    private void item(GuiGraphics graphics, ItemStack stack, int ix, int iy, int mouseX, int mouseY) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderItem(stack, ix, iy);
        if (mouseX >= ix && mouseX < ix + 16 && mouseY >= iy && mouseY < iy + 16) {
            hovered = stack;
        }
    }

    private void footer(GuiGraphics graphics, GrimoireEntry entry, boolean readable, int fy, int mouseX, int mouseY) {
        if (view.canGoBack()) {
            buttons.add(view.button(x, fy, view.text("back"), true, view::back));
        }
        boolean atBlock = view.reader().map(r -> r.access().atResearchBlock()).orElse(false);
        if (entry.recipe().isPresent() && !readable && atBlock) {
            TextButton research = view.button(x, fy, view.text("research"), true, () -> view.research(entry.key()));
            buttons.add(new TextButton(x + width - research.width(), fy, research.width(), research.label(), true, research.action()));
        }
        buttons.forEach(b -> b.render(graphics, font, mouseX, mouseY));
        if (contentHeight > height - FOOTER - (bodyTop - y)) {
            graphics.drawString(font, scroll > 0 ? "^" : "v", x + width / 2 - 2, fy + 2, TextButton.FAINT, false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (TextButton b : buttons) {
            if (b.click(mouseX, mouseY)) {
                return true;
            }
        }
        if (mouseY >= bodyTop && mouseY < y + height - FOOTER) {
            for (Link link : links) {
                if (link.contains(mouseX, mouseY)) {
                    view.open(link.key());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height && view.current().isPresent()) {
            scroll = Math.max(0, scroll - (int) Math.signum(delta) * 12);
            return true;
        }
        return false;
    }
}
