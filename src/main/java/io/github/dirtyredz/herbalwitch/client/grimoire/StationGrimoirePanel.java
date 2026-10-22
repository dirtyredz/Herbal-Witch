package io.github.dirtyredz.herbalwitch.client.grimoire;

import io.github.dirtyredz.herbalwitch.client.grimoire.widgets.EntryList;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * The grimoire beside a station's own screen: a "Grimoire" button (shown while the player carries a
 * grimoire) toggles a one-column book panel left of the station. It only reads the client's
 * content and discoveries; the station's menu, slots and processing are untouched and stay the
 * server's. A processing station opens on its category; the hearth and stove on heat.
 */
public final class StationGrimoirePanel implements GrimoirePanelHost.Panel {
    private static final int WIDTH = 124;
    private final GrimoireView view;
    private final Optional<EntryKey> start;
    private Button toggle;
    private boolean open;
    private int x;
    private int y;
    private int height;

    /**
     * @param category the processing category to list, if any
     * @param start    an entry to open first (heat guidance for heat sources)
     */
    public StationGrimoirePanel(Font font, Optional<String> category, Optional<EntryKey> start) {
        this.view = new GrimoireView(font, GrimoireView.Layout.COLUMN, Optional.empty(), EntryList.Filter.ALL, category);
        this.start = start;
    }

    /** Lays the panel out beside the station GUI and returns its toggle button for the screen to add. */
    public Button init(int leftPos, int topPos, int imageWidth, int imageHeight) {
        x = Math.max(2, leftPos - WIDTH - 4);
        y = topPos;
        height = imageHeight;
        view.setBounds(x + 4, y + 4, WIDTH - 8, height - 8);
        toggle = Button.builder(GrimoireView.textOf("open"), b -> setOpen(!open))
                .bounds(leftPos + imageWidth - 58, topPos - 16, 58, 14)
                .tooltip(Tooltip.create(GrimoireView.textOf("open.tooltip"))).build();
        toggle.visible = carriesGrimoire();
        return toggle;
    }

    private void setOpen(boolean value) {
        open = value;
        if (open && start.isPresent() && view.current().isEmpty()) {
            view.open(start.get());
        }
    }

    private static boolean carriesGrimoire() {
        var player = Minecraft.getInstance().player;
        return player != null && player.getInventory().hasAnyMatching(GrimoireItems::isGrimoire);
    }

    public void tick() {
        if (toggle != null) {
            toggle.visible = carriesGrimoire();
            if (!toggle.visible) {
                open = false;
            }
        }
        view.tick();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!open) {
            return;
        }
        graphics.fill(x, y, x + WIDTH, y + height, 0xFF4A2A55);
        graphics.fill(x + 2, y + 2, x + WIDTH - 2, y + height - 2, 0xFFD9C7A0);
        view.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean tooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        ItemStack stack = hovered();
        if (stack.isEmpty()) return false;
        graphics.renderTooltip(font, stack, mouseX, mouseY);
        return true;
    }

    /** The page item under the mouse, for the screen's tooltip. */
    public ItemStack hovered() {
        return open ? view.hovered() : ItemStack.EMPTY;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return open && view.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return open && view.mouseScrolled(mouseX, mouseY, delta);
    }

    /** Whether the panel's search box takes the keyboard. */
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (open && view.searching()) {
            view.keyPressed(key, scanCode, modifiers);
            return true;
        }
        return false;
    }

    public boolean charTyped(char c, int modifiers) {
        return open && view.charTyped(c, modifiers);
    }
}
