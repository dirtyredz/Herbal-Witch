package io.github.dirtyredz.herbalwitch.client.grimoire.widgets;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;

import io.github.dirtyredz.herbalwitch.client.recipe.RecipeText;



import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireClientState;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireView;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ingredients.IngredientRequirements;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/** Live recipe rendering, using the enclosing page's clipping and item/paragraph drawing. */
final class RecipeBody {
    private final GrimoireView view;
    private final Font font;
    private final EntryPage page;

    RecipeBody(GrimoireView view, Font font, EntryPage page) {
        this.view = view;
        this.font = font;
        this.page = page;
    }

    int teaser(GuiGraphics graphics, GrimoireEntry entry, int cy) {
        cy = station(graphics, entry, cy);
        return page.paragraph(graphics, entry.hint().orElse(view.text("unknown")), cy + 2, TextButton.PLUM);
    }

    private int station(GuiGraphics graphics, GrimoireEntry entry, int cy) {
        Component station = ProcessingCategories.ALL.stream().filter(c -> c.id().equals(entry.category())).findFirst()
                .map(c -> StationBlocks.block(c.station()).getName()).orElse(Component.literal("?"));
        return page.paragraph(graphics, view.text("station", station), cy, TextButton.INK);
    }

    int render(GuiGraphics graphics, GrimoireEntry entry, RecipeView recipe, int cy, int x, int width, int mouseX, int mouseY) {
        ProcessRecipeDescription d = recipe.description();
        List<IngredientRequirements.Requirement> requirements = IngredientRequirements.of(d);
        Optional<ReaderUpdate> counts = view.reader().flatMap(r ->
                GrimoireClientState.INSTANCE.availability(r.containerId(), r.nonce(), entry.key()));
        for (int i = 0; i < requirements.size(); i++) {
            IngredientRequirements.Requirement req = requirements.get(i);
            List<ItemStack> options = RecipeText.stacks(req.ingredient(), req.count());
            ItemStack shown = options.isEmpty() ? ItemStack.EMPTY
                    : options.get((int) (Util.getMillis() / 1000 % options.size()));
            graphics.blit(GrimoireView.ATLAS, x, cy, 0, 184, 18, 18);
            page.item(graphics, shown, x + 1, cy + 1, mouseX, mouseY);
            graphics.renderItemDecorations(font, shown, x + 1, cy + 1);
            Component role = view.text("role." + req.role().name().toLowerCase(java.util.Locale.ROOT));
            graphics.drawString(font, role, x + 21, cy + 1, TextButton.FAINT, false);
            if (options.size() > 1) {
                graphics.drawString(font, view.text("alternatives", options.size()), x + 21, cy + 10, TextButton.FAINT, false);
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
        page.item(graphics, result, x + 27, cy + 1, mouseX, mouseY);
        graphics.renderItemDecorations(font, result, x + 27, cy + 1);
        int rx = x + 48;
        for (ItemStack back : d.returns()) {
            graphics.drawString(font, "+", rx, cy + 5, TextButton.FAINT, false);
            page.item(graphics, back, rx + 7, cy + 1, mouseX, mouseY);
            rx += 25;
        }
        cy += 21;
        cy = station(graphics, entry, cy);
        graphics.blit(GrimoireView.ATLAS, x, cy, 50, 184, 9, 9);
        graphics.drawString(font, RecipeText.footer(d.duration(), false), x + 12, cy + 1, TextButton.INK, false);
        if (d.heated()) {
            graphics.blit(GrimoireView.ATLAS, x + 50, cy, 40, 184, 9, 9);
            graphics.drawString(font, view.text("heat"), x + 62, cy + 1, TextButton.PINK, false);
        }
        cy += 12;
        java.util.Optional<Component> witch = RecipeText.requirements(d.requirements(), WitchRules.current().workings().processingPercent());
        if (witch.isPresent()) {
            cy = page.paragraph(graphics, witch.get(), cy, TextButton.PINK);
        }
        if (!d.returns().isEmpty()) {
            cy = page.paragraph(graphics, view.text("returns"), cy, TextButton.FAINT);
        }
        if (view.reader().isPresent()) {
            cy = page.paragraph(graphics, view.text(counts.isPresent() ? "scope" : "counting"), cy, TextButton.FAINT);
            if (counts.isPresent() && counts.get().lines().stream().allMatch(l -> l.missing() == 0)) {
                cy = page.paragraph(graphics, view.text("ready"), cy, TextButton.GREEN);
            }
        } else {
            cy = page.paragraph(graphics, view.text("open_for_counts"), cy, TextButton.FAINT);
        }
        return cy + 2;
    }

}
