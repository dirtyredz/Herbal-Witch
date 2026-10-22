package io.github.dirtyredz.herbalwitch.client.grimoire.widgets;





import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireView;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
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
    private final RecipeBody recipeBody;
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
        this.recipeBody = new RecipeBody(view, font, this);
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
            cy = readable ? recipeBody.render(graphics, entry, entry.recipe().get(), cy, x, width, mouseX, mouseY) : recipeBody.teaser(graphics, entry, cy);
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

    int paragraph(GuiGraphics graphics, Component text, int cy, int colour) {
        for (FormattedCharSequence line : font.split(text, width - 2)) {
            graphics.drawString(font, line, x, cy, colour, false);
            cy += 9;
        }
        return cy + 2;
    }

    void item(GuiGraphics graphics, ItemStack stack, int ix, int iy, int mouseX, int mouseY) {
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
